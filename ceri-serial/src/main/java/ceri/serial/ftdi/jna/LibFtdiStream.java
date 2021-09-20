package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_bitmode;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_buffers;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireDev;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static java.lang.Math.min;
import java.time.Duration;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.time.DateUtil;
import ceri.log.util.LogUtil;
import ceri.serial.clib.jna.CTime.timeval;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.size_and_time;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsbException;

public class LibFtdiStream {
	private static final Logger logger = LogManager.getLogger();
	private static final int READ_STATUS_BYTES = 2;
	private static final double PROGRESS_INTERVAL_MIN_SEC = 1.0;
	// private static final libusb_transfer_cb_fn ftdi_read_stream_cb =
	// LibFtdiStream::ftdi_read_stream_cb;

	public static class FTDIProgressInfo {
		public size_and_time first = new size_and_time();
		public size_and_time prev = new size_and_time();
		public size_and_time current = new size_and_time();
		public double totalTime;
		public double totalRate;
		public double currentRate;
	}

	// typedef int (FTDIStreamCallback)(uint8_t *buffer, int length, FTDIProgressInfo *progress,
	// void *userdata);
	public interface FTDIStreamCallback<T> {
		void invoke(Pointer buffer, int length, FTDIProgressInfo progress, T userdata)
			throws LibUsbException;
	}

	private static class FTDIStreamState<T> {
		public FTDIStreamCallback<T> callback;
		public libusb_transfer_cb_fn transferCb;
		public T userdata;
		public int packetsize;
		public int activity;
		public int result;
		public FTDIProgressInfo progress;
	}

	/**
	 * Callback function for stream read. Frees memory and sets state.
	 */
	private static <T> void ftdi_readstream_cb(libusb_transfer transfer, FTDIStreamState<T> state) {
		state.activity++;
		try {
			if (!transferComplete(transfer, state)) return;
			readStream(transfer, state);
			transfer.status = -1;
			LibUsb.libusb_submit_transfer(transfer);
		} catch (LibUsbException e) {
			logger.catching(e);
			state.result = e.code;
			LogUtil.execute(logger, () -> LibUsb.libusb_free_transfer(transfer));
		}
	}

	/**
	 * Streaming read of data from the device. Uses asynchronous transfers in libusb-1.0 for
	 * high-performance streaming of data from a device interface. This function continuously
	 * transfers data until an error occurs, or the callback returns a nonzero value. For every
	 * contiguous block of received data, the callback will be invoked.
	 */
	public static <T> void ftdi_readstream(ftdi_context ftdi, FTDIStreamCallback<T> callback,
		T userdata, int packetsPerTransfer, int numTransfers) throws LibUsbException {
		requireDev(ftdi);
		requireFifo(ftdi);
		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_RESET);
		ftdi_usb_purge_buffers(ftdi);
		FTDIStreamState<T> state = state(callback, userdata, ftdi.max_packet_size, 1);
		submitTransfers(ftdi, state, numTransfers, packetsPerTransfer);
		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_SYNCFF);
		state.progress.first.time = Instant.now();
		completeStreamEvents(ftdi, state);
		LibUsb.caller.verifyInt(state.result, "ftdi_read_stream", ftdi, callback, userdata,
			packetsPerTransfer, numTransfers);
	}

	private static <T> FTDIStreamState<T> state(FTDIStreamCallback<T> callback, T userdata,
		int packetSize, int activity) {
		FTDIStreamState<T> state = new FTDIStreamState<>();
		state.callback = callback;
		state.transferCb = t -> ftdi_readstream_cb(t, state); // ref to prevent gc
		state.userdata = userdata;
		state.packetsize = packetSize;
		state.activity = activity;
		return state;
	}

	private static void requireFifo(ftdi_context ftdi) throws LibUsbException {
		if (ftdi_chip_type.isSyncFifoType(ftdi.type)) return;
		throw LibUsbException.of(LIBUSB_ERROR_NOT_SUPPORTED,
			"Synchronous FIFO mode not supported by type: " + ftdi.type);
	}

	private static <T> void submitTransfers(ftdi_context ftdi, FTDIStreamState<T> state,
		int numTransfers, int packetsPerTransfer) throws LibUsbException {
		int bufferSize = packetsPerTransfer * ftdi.max_packet_size;
		for (int i = 0; i < numTransfers; i++) {
			libusb_transfer transfer = LibUsb.libusb_alloc_transfer(0);
			Memory m = new Memory(bufferSize);
			LibUsb.libusb_fill_bulk_transfer(transfer, ftdi.usb_dev, ftdi.out_ep, m, bufferSize,
				state.transferCb, null, 0);
			transfer.status = -1;
			LibUsb.libusb_submit_transfer(transfer);
		}
	}

	private static <T> void completeStreamEvents(ftdi_context ftdi, FTDIStreamState<T> state)
		throws LibUsbException {
		timeval timeout = Struct.write(new timeval().set(0, ftdi.usb_read_timeout * 1000));
		do {
			handleStreamEvents(ftdi, state, timeout);
		} while (state.result == 0);
	}

	private static <T> void handleStreamEvents(ftdi_context ftdi, FTDIStreamState<T> state,
		timeval timeout) throws LibUsbException {
		handleUsbEvents(ftdi, timeout);
		if (state.activity == 0) state.result = 1;
		state.activity = 0;
		// If enough time has elapsed, update the progress
		FTDIProgressInfo progress = state.progress;
		Instant now = Instant.now();
		if (secDiff(now, progress.current.time) < PROGRESS_INTERVAL_MIN_SEC) return;
		updateProgress(progress, now);
		state.callback.invoke(null, 0, progress, state.userdata);
		progress.prev = progress.current;
	}

	private static void handleUsbEvents(ftdi_context ftdi, timeval timeout) throws LibUsbException {
		try {
			LibUsb.libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		} catch (LibUsbException e) {
			// Retry if interrupted
			if (e.error != LIBUSB_ERROR_INTERRUPTED) throw e;
			LibUsb.libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		}
	}

	private static void updateProgress(FTDIProgressInfo progress, Instant now) {
		progress.current.time = now;
		progress.totalTime = secDiff(now, progress.first.time);
		if (progress.prev.totalBytes == 0) return;
		// We have enough information to calculate rates
		double currentTime = secDiff(now, progress.prev.time);
		progress.totalRate = progress.current.totalBytes / progress.totalTime;
		progress.currentRate =
			(progress.current.totalBytes - progress.prev.totalBytes) / currentTime;
	}

	private static <T> boolean transferComplete(libusb_transfer transfer,
		FTDIStreamState<T> state) {
		if (transfer.status == LIBUSB_TRANSFER_COMPLETED.value) return true;
		logger.warn("Unknown status: {} ({})", transfer.status, transfer.status());
		state.result = LIBUSB_ERROR_IO.value;
		return false;
	}

	private static <T> void readStream(libusb_transfer transfer, FTDIStreamState<T> state)
		throws LibUsbException {
		for (int i = 0;; i++) {
			int position = (i * state.packetsize) + READ_STATUS_BYTES;
			int len = min(transfer.actual_length - position, state.packetsize - READ_STATUS_BYTES);
			if (len <= 0) break;
			state.progress.current.totalBytes += len;
			state.callback.invoke(transfer.buffer.share(position), len, null, state.userdata);
		}
	}

	private static double secDiff(Instant instant1, Instant instant2) {
		return DateUtil.seconds(Duration.between(instant1, instant2));
	}
}
