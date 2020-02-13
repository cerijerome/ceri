package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_bitmode;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_buffers;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireDev;
import static ceri.serial.jna.clib.Time.gettimeofday;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout;
import static ceri.serial.libusb.jna.LibUsb.libusb_submit_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.size_and_time;
import ceri.serial.jna.Struct;
import ceri.serial.jna.clib.Time.timeval;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiStream {
	private static final Logger logger = LogManager.getLogger();
	private static final double PROGRESS_INTERVAL_MIN_SEC = 1.0;
	private static final libusb_transfer_cb_fn ftdi_read_stream_cb =
		LibFtdiStream::ftdi_read_stream_cb;

	public static class ftdi_progress_info extends Struct {
		private static final List<String> FIELDS = List.of( //
			"first", "prev", "current", "totalTime", "totalRate", "currentRate");

		public static class ByValue extends ftdi_progress_info //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_progress_info //
			implements Structure.ByReference {}

		public size_and_time first;
		public size_and_time prev;
		public size_and_time current;
		public double totalTime;
		public double totalRate;
		public double currentRate;

		public ftdi_progress_info() {}

		public ftdi_progress_info(Pointer p) {
			super(p);
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}
	}

	// typedef int (FTDIStreamCallback)(uint8_t *buffer, int length, FTDIProgressInfo *progress,
	// void *userdata);
	public interface ftdi_stream_cb extends Callback {
		int invoke(Pointer buffer, int length, ftdi_progress_info progress, Pointer userdata) throws LibUsbException;
	}

	public static class ftdi_stream_state extends Struct {
		private static final List<String> FIELDS = List.of( //
			"callback", "userdata", "packetsize", "activity", "result", "progress");
		private static final IntAccessor.Typed<ftdi_stream_state> resultAccessor =
			IntAccessor.typed(t -> t.result, (t, i) -> t.result = i);

		public static class ByValue extends ftdi_stream_state //
			implements Structure.ByValue {}

		public static class ByReference extends ftdi_stream_state //
			implements Structure.ByReference {}

		public static ftdi_stream_state of(ftdi_stream_cb callback, Pointer userdata,
			int packetsize, int activity) {
			ftdi_stream_state state = new ftdi_stream_state();
			state.callback = callback;
			state.userdata = userdata;
			state.packetsize = packetsize;
			state.activity = activity;
			state.progress = new ftdi_progress_info();
			return state;
		}

		public ftdi_stream_cb callback;
		public Pointer userdata;
		public int packetsize;
		public int activity;
		public int result;
		public ftdi_progress_info progress;

		public ftdi_stream_state() {}

		public ftdi_stream_state(Pointer p) {
			super(p);
		}

		public FieldTranscoder<libusb_error> result() {
			return libusb_error.xcoder.field(resultAccessor.from(this));
		}

		@Override
		protected List<String> getFieldOrder() {
			return FIELDS;
		}

	}

	/*
	 * Handle callbacks With Exit request, free memory and release the transfer state->result is
	 * only set when some error happens
	 */
	private static void ftdi_read_stream_cb(libusb_transfer transfer) {
		ftdi_stream_state state = new ftdi_stream_state(transfer.user_data);
		state.activity++;

		if (transfer.status().get() != LIBUSB_TRANSFER_COMPLETED) {
			logger.warn("Unknown status: {} ({})", transfer.status, transfer.status().get());
			state.result().set(LIBUSB_ERROR_IO);
			return;
		}
		try {
			for (int i = 0;; i++) {
				int position = (i * state.packetsize) + LibFtdiUtil.READ_STATUS_BYTES;
				int len = Math.min(transfer.actual_length - position,
					state.packetsize - LibFtdiUtil.READ_STATUS_BYTES);
				if (len <= 0) break;
				state.progress.current.totalBytes += len;
				state.callback.invoke(transfer.buffer.share(position), len, null, state.userdata);
			}
		} catch (LibUsbException e) {
			logger.catching(e);
			libusb_free_transfer(transfer);
			return;
		}

		try {
			transfer.status = -1;
			libusb_submit_transfer(transfer);
		} catch (LibUsbException e) {
			logger.catching(e);
			state.result = e.code;
		}
	}

	/**
	 * Streaming reading of data from the device
	 * 
	 * Use asynchronous transfers in libusb-1.0 for high-performance streaming of data from a device
	 * interface back to the PC. This function continuously transfers data until either an error
	 * occurs or the callback returns a nonzero value. This function returns a libusb error code or
	 * the callback's return value.
	 * 
	 * For every contiguous block of received data, the callback will be invoked.
	 */
	public static void ftdi_read_stream(ftdi_context ftdi, ftdi_stream_cb callback,
		Pointer userdata, int packetsPerTransfer, int numTransfers) throws LibUsbException {
		requireDev(ftdi);
		if (!ftdi.type().get().isSyncFifoType()) throw new LibUsbException( //
			"Synchronous FIFO mode not supported: " + ftdi.type().get(),
			libusb_error.LIBUSB_ERROR_NOT_SUPPORTED);

		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_RESET);
		ftdi_usb_purge_buffers(ftdi);
		ftdi_stream_state state = ftdi_stream_state.of(callback, userdata, ftdi.max_packet_size, 1);
		int bufferSize = packetsPerTransfer * ftdi.max_packet_size;

		libusb_transfer[] transfers = new libusb_transfer[numTransfers];
		for (int i = 0; i < numTransfers; i++) {
			transfers[i] = LibUsb.libusb_alloc_transfer(0);
			Memory m = new Memory(bufferSize);
			LibUsb.libusb_fill_bulk_transfer(transfers[i], ftdi.usb_dev, ftdi.out_ep, m, bufferSize,
				ftdi_read_stream_cb, state.getPointer(), 0);
			transfers[i].status = -1;
			LibUsb.libusb_submit_transfer(transfers[i]);
		}

		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_SYNCFF);
		gettimeofday(state.progress.first.time);
		timeval timeout = new timeval(0, ftdi.usb_read_timeout * 1000);

		do {
			handleStreamEvents(ftdi, state, timeout);
		} while (state.result == 0);

		if (state.result < 0)
			throw LibUsbException.fullMessage("ftdi_read_stream failed", state.result);
	}

	private static void handleStreamEvents(ftdi_context ftdi, ftdi_stream_state state,
		timeval timeout) throws LibUsbException {
		try {
			libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		} catch (LibUsbException e) {
			if (e.error != LIBUSB_ERROR_INTERRUPTED) throw e;
			libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		}
		if (state.activity == 0) state.result = 1;
		else state.activity = 0;

		// If enough time has elapsed, update the progress
		timeval now = timeval.now();
		ftdi_progress_info progress = state.progress;
		if (timeval_diff(now, progress.current.time) < PROGRESS_INTERVAL_MIN_SEC) return;

		progress.current.time = now;
		progress.totalTime = timeval_diff(progress.current.time, progress.first.time);
		if (progress.prev.totalBytes != 0) {
			// We have enough information to calculate rates
			double currentTime = timeval_diff(progress.current.time, progress.prev.time);
			progress.totalRate = progress.current.totalBytes / progress.totalTime;
			progress.currentRate =
				(progress.current.totalBytes - progress.prev.totalBytes) / currentTime;
		}
		state.callback.invoke(null, 0, progress, state.userdata);
		progress.prev = progress.current;
	}

	private static double timeval_diff(timeval a, timeval b) {
		return a.diffInSec(b);
	}

}
