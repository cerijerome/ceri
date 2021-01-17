package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_bitmode;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_buffers;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireDev;
import static ceri.serial.libusb.jna.LibUsb.libusb_alloc_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_fill_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_handle_events_timeout;
import static ceri.serial.libusb.jna.LibUsb.libusb_submit_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static java.lang.Math.min;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.IntAccessor;
import ceri.log.util.LogUtil;
import ceri.serial.clib.jna.Time;
import ceri.serial.clib.jna.Time.timeval;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.size_and_time;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_error;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiStream {
	private static final Logger logger = LogManager.getLogger();
	private static final int READ_STATUS_BYTES = 2;
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
		int invoke(Pointer buffer, int length, ftdi_progress_info progress, Pointer userdata)
			throws LibUsbException;
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

	/**
	 * Streaming read of data from the device. Uses asynchronous transfers in libusb-1.0 for
	 * high-performance streaming of data from a device interface. This function continuously
	 * transfers data until an error occurs, or the callback returns a nonzero value. For every
	 * contiguous block of received data, the callback will be invoked.
	 */
	public static void ftdi_read_stream(ftdi_context ftdi, ftdi_stream_cb callback,
		Pointer userdata, int packetsPerTransfer, int numTransfers) throws LibUsbException {
		requireDev(ftdi);
		requireFifo(ftdi);
		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_RESET);
		ftdi_usb_purge_buffers(ftdi);
		ftdi_stream_state state = ftdi_stream_state.of(callback, userdata, ftdi.max_packet_size, 1);
		submitTransfers(ftdi, state, numTransfers, packetsPerTransfer);
		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_SYNCFF);
		Time.gettimeofday(state.progress.first.time);
		completeStreamEvents(ftdi, state);
		LibUsbException.verify(state.result, "ftdi_read_stream failed");
	}

	/**
	 * Callback function for stream read. Frees memory and sets state.
	 */
	private static void ftdi_read_stream_cb(libusb_transfer transfer) {
		ftdi_stream_state state = new ftdi_stream_state(transfer.user_data);
		state.activity++;
		try {
			if (!transferComplete(transfer, state)) return;
			readStream(transfer, state);
			transfer.status = -1;
			libusb_submit_transfer(transfer);
		} catch (LibUsbException e) {
			logger.catching(e);
			close(transfer, state, e.code);
		}
	}

	private static void requireFifo(ftdi_context ftdi) throws LibUsbException {
		ftdi_chip_type type = ftdi.type().get();
		if (ftdi_chip_type.isSyncFifoType(type)) return;
		throw LibUsbException.of(LIBUSB_ERROR_NOT_SUPPORTED,
			"Synchronous FIFO mode not supported by type: " + type);
	}

	private static void submitTransfers(ftdi_context ftdi, ftdi_stream_state state,
		int numTransfers, int packetsPerTransfer) throws LibUsbException {
		int bufferSize = packetsPerTransfer * ftdi.max_packet_size;
		libusb_transfer[] transfers = new libusb_transfer[numTransfers];
		for (int i = 0; i < numTransfers; i++) {
			transfers[i] = libusb_alloc_transfer(0);
			Memory m = new Memory(bufferSize);
			libusb_fill_bulk_transfer(transfers[i], ftdi.usb_dev, ftdi.out_ep, m, bufferSize,
				ftdi_read_stream_cb, state.getPointer(), 0);
			transfers[i].status = -1;
			libusb_submit_transfer(transfers[i]);
		}
	}

	private static void completeStreamEvents(ftdi_context ftdi, ftdi_stream_state state)
		throws LibUsbException {
		timeval timeoutMicros = new timeval(0, ftdi.usb_read_timeout * 1000);
		do {
			handleStreamEvents(ftdi, state, timeoutMicros);
		} while (state.result == 0);
	}

	private static void handleStreamEvents(ftdi_context ftdi, ftdi_stream_state state,
		timeval timeout) throws LibUsbException {
		handleUsbEvents(ftdi, timeout);
		if (state.activity == 0) state.result = 1;
		state.activity = 0;
		// If enough time has elapsed, update the progress
		ftdi_progress_info progress = state.progress;
		timeval now = timeval.now();
		if (timeDiffSec(now, progress.current.time) < PROGRESS_INTERVAL_MIN_SEC) return;
		updateProgress(progress, now);
		state.callback.invoke(null, 0, progress, state.userdata);
		progress.prev = progress.current;
	}

	private static void handleUsbEvents(ftdi_context ftdi, timeval timeout) throws LibUsbException {
		try {
			libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		} catch (LibUsbException e) {
			// Retry if interrupted
			if (e.error != LIBUSB_ERROR_INTERRUPTED) throw e;
			libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		}
	}

	private static void updateProgress(ftdi_progress_info progress, timeval now) {
		progress.current.time = now;
		progress.totalTime = timeDiffSec(progress.current.time, progress.first.time);
		if (progress.prev.totalBytes == 0) return;
		// We have enough information to calculate rates
		double currentTime = timeDiffSec(progress.current.time, progress.prev.time);
		progress.totalRate = progress.current.totalBytes / progress.totalTime;
		progress.currentRate =
			(progress.current.totalBytes - progress.prev.totalBytes) / currentTime;
	}

	private static boolean transferComplete(libusb_transfer transfer, ftdi_stream_state state) {
		if (transfer.status() == LIBUSB_TRANSFER_COMPLETED) return true;
		logger.warn("Unknown status: {} ({})", transfer.status, transfer.status());
		close(transfer, state, LIBUSB_ERROR_IO.value);
		return false;
	}

	private static void readStream(libusb_transfer transfer, ftdi_stream_state state)
		throws LibUsbException {
		for (int i = 0;; i++) {
			int position = (i * state.packetsize) + READ_STATUS_BYTES;
			int len = min(transfer.actual_length - position, state.packetsize - READ_STATUS_BYTES);
			if (len <= 0) break;
			state.progress.current.totalBytes += len;
			LibUsbException.verify(state.callback.invoke(transfer.buffer.share(position), //
				len, null, state.userdata), "Stream read callback invocation");
		}
	}

	private static void close(libusb_transfer transfer, ftdi_stream_state state, int code) {
		state.result = code;
		LogUtil.execute(logger, () -> libusb_free_transfer(transfer));
	}

	private static double timeDiffSec(timeval a, timeval b) {
		return a.diffInSec(b);
	}

}
