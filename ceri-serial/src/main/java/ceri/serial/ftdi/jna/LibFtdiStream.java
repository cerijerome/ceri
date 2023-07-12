package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_bitmode;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_buffers;
import static ceri.serial.ftdi.jna.LibFtdiUtil.isError;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireDev;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_SUPPORTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_CANCELLED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static java.lang.Math.min;
import java.time.Duration;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import ceri.common.time.DateUtil;
import ceri.jna.clib.jna.CTime.timeval;
import ceri.jna.util.GcMemory;
import ceri.jna.util.Struct;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_chip_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.ftdi.jna.LibFtdi.size_and_time;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_cb_fn;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbUtil;

public class LibFtdiStream {
	private static final Logger logger = LogManager.getLogger();
	private static final int READ_STATUS_BYTES = 2;
	public static final double PROGRESS_INTERVAL_SEC = 1.0;

	public static class FTDIProgressInfo {
		public final size_and_time first = new size_and_time();
		public final size_and_time prev = new size_and_time();
		public final size_and_time current = new size_and_time();
		public double totalTime;
		public double totalRate;
		public double currentRate;

		private void update(Instant now) {
			current.time = now;
			totalTime = secDiff(first.time, now);
			if (prev.totalBytes == 0) return;
			double currentTime = secDiff(now, prev.time);
			totalRate = current.totalBytes / totalTime;
			currentRate = (current.totalBytes - prev.totalBytes) / currentTime;
		}
	}

	// typedef int (FTDIStreamCallback)(uint8_t *buffer, int length, FTDIProgressInfo *progress,
	// void *userdata);
	public interface FTDIStreamCallback<T> {
		boolean invoke(Pointer buffer, int length, FTDIProgressInfo progress, T userdata)
			throws LibUsbException;
	}

	private static enum CancelState {
		none,
		requested,
		completed;
	}

	private static class FTDIStreamState<T> {
		private final FTDIProgressInfo progress = new FTDIProgressInfo();
		private final FTDIStreamCallback<T> callback;
		private final T userdata;
		private final int packetsize;
		private final libusb_transfer[] transfers;
		private final IntByReference completed = new IntByReference();
		private volatile int activeTransfers = 0;
		private volatile CancelState cancel = CancelState.none;
		private volatile Exception error = null;

		private FTDIStreamState(FTDIStreamCallback<T> callback, T userdata, int numTransfers,
			int packetSize) {
			this.transfers = new libusb_transfer[numTransfers];
			this.callback = callback;
			this.userdata = userdata;
			this.packetsize = packetSize;
		}
	}

	private LibFtdiStream() {}

	/**
	 * Streaming read of data from the device. Uses asynchronous transfers in libusb-1.0 for
	 * high-performance streaming of data from a device interface. This function continuously
	 * transfers data until an error occurs, or the callback returns false. For every contiguous
	 * block of received data, the callback will be invoked.
	 */
	public static <T> void ftdi_readstream(ftdi_context ftdi, FTDIStreamCallback<T> callback,
		T userData, int packetsPerTransfer, int numTransfers) throws LibUsbException {
		ftdi_readstream(ftdi, callback, userData, packetsPerTransfer, numTransfers,
			PROGRESS_INTERVAL_SEC);
	}

	/**
	 * Streaming read of data from the device. Uses asynchronous transfers in libusb-1.0 for
	 * high-performance streaming of data from a device interface. This function continuously
	 * transfers data until an error occurs, or the callback returns false. For every contiguous
	 * block of received data, the callback will be invoked.
	 */
	public static <T> void ftdi_readstream(ftdi_context ftdi, FTDIStreamCallback<T> callback,
		T userData, int packetsPerTransfer, int numTransfers, double progressIntervalSec)
		throws LibUsbException {
		requireDev(ftdi);
		requireFifo(ftdi);
		var state = new FTDIStreamState<>(callback, userData, numTransfers, ftdi.max_packet_size);
		submitTransfers(ftdi, state, packetsPerTransfer);
		completeTransfers(ftdi, state, progressIntervalSec);
		if (state.error != null) throw LibUsbException.ADAPTER.apply(state.error);
	}

	private static void submitTransfers(ftdi_context ftdi, FTDIStreamState<?> state,
		int packetsPerTransfer) {
		try {
			ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_RESET);
			ftdi_usb_purge_buffers(ftdi);
			int bufferSize = packetsPerTransfer * ftdi.max_packet_size;
			for (int i = 0; i < state.transfers.length; i++) {
				state.transfers[i] = submitTransfer(ftdi, callback(state, i), bufferSize);
				state.activeTransfers++;
			}
			ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_SYNCFF);
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
			requestCancel(state);
		}
	}

	private static libusb_transfer submitTransfer(ftdi_context ftdi, libusb_transfer_cb_fn callback,
		int bufferSize) throws LibUsbException {
		var transfer = LibUsb.libusb_alloc_transfer(0);
		try {
			var m = GcMemory.malloc(bufferSize);
			LibUsb.libusb_fill_bulk_transfer(transfer, ftdi.usb_dev, ftdi.out_ep, m.m, bufferSize,
				callback, null, 0);
			LibUsb.libusb_submit_transfer(Struct.write(transfer));
			return transfer;
		} catch (LibUsbException | RuntimeException e) {
			LibUsb.libusb_free_transfer(transfer);
			throw e;
		}
	}

	private static <T> void completeTransfers(ftdi_context ftdi, FTDIStreamState<T> state,
		double progressIntervalSec) {
		state.progress.first.time = Instant.now();
		timeval timeout = timeout(ftdi);
		checkCompleted(state);
		int completed = state.completed.getValue();
		while (completed == 0) {
			try {
				if (state.cancel == CancelState.requested) cancelTransfers(state);
				completed = LibUsb.libusb_handle_events_timeout_completed(ftdi.usb_ctx, timeout,
					state.completed); // blocks
				if (state.error == null) updateProgress(state, progressIntervalSec);
			} catch (LibUsbException | RuntimeException e) {
				if (isError(e, LIBUSB_ERROR_INTERRUPTED)) continue;
				error(state, e);
				return;
			}
		}
	}

	private static <T> void updateProgress(FTDIStreamState<T> state, double progressIntervalSec) {
		try {
			Instant now = Instant.now();
			if (secDiff(state.progress.current.time, now) < progressIntervalSec) return;
			state.progress.update(now);
			if (!state.callback.invoke(null, 0, state.progress, state.userdata))
				requestCancel(state);
			state.progress.prev.set(state.progress.current);
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
			requestCancel(state);
		}
	}

	private static <T> libusb_transfer_cb_fn callback(FTDIStreamState<T> state, int i) {
		return p -> ftdi_readstream_cb(state, i);
	}

	/**
	 * Callback function for each libusb transfer from libusb_handle_events_timeout call.
	 */
	private static <T> void ftdi_readstream_cb(FTDIStreamState<T> state, int i) {
		try {
			var transfer = libusb_transfer_cb_fn.read(state.transfers[i]);
			if (validStatus(transfer, i) && notifyPackets(state, transfer))
				LibUsb.libusb_submit_transfer(transfer);
			else freeTransfer(state, i);
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
			freeTransfer(state, i);
			requestCancel(state);
		}
	}

	private static boolean validStatus(libusb_transfer transfer, int i) throws LibUsbException {
		var status = transfer.status();
		if (status == LIBUSB_TRANSFER_COMPLETED) return true;
		if (status == LIBUSB_TRANSFER_CANCELLED) return false;
		throw LibUsbException.of(LibUsbUtil.statusError(status),
			"Transfer index %d failed with status: %d (%s)", i, transfer.status, status);
	}

	private static <T> boolean notifyPackets(FTDIStreamState<T> state, libusb_transfer transfer)
		throws LibUsbException {
		for (int pos = READ_STATUS_BYTES; pos < transfer.actual_length; pos += state.packetsize) {
			int len = min(transfer.actual_length - pos, state.packetsize - READ_STATUS_BYTES);
			state.progress.current.totalBytes += len;
			Pointer buffer = transfer.buffer.share(pos);
			if (!state.callback.invoke(buffer, len, null, state.userdata)) {
				requestCancel(state);
				return false;
			}
		}
		return true;
	}

	private static void error(FTDIStreamState<?> state, Exception e) {
		if (state.error == null) state.error = e;
		else logger.catching(e);
	}

	private static void requestCancel(FTDIStreamState<?> state) {
		if (state.cancel == CancelState.none) state.cancel = CancelState.requested;
	}

	private static void cancelTransfers(FTDIStreamState<?> state) {
		LogUtil.execute(logger, () -> {
			for (int i = 0; i < state.transfers.length; i++)
				LibUsb.libusb_cancel_transfer(state.transfers[i]);
		});
		state.cancel = CancelState.completed;
	}

	/**
	 * Free transfer and decrement the active transfer count.
	 */
	private static void freeTransfer(FTDIStreamState<?> state, int i) {
		LogUtil.execute(logger, () -> LibUsb.libusb_free_transfer(state.transfers[i]));
		state.transfers[i] = null;
		state.activeTransfers--;
		checkCompleted(state);
	}

	private static void checkCompleted(FTDIStreamState<?> state) {
		if (state.activeTransfers <= 0) state.completed.setValue(1);
	}

	private static void requireFifo(ftdi_context ftdi) throws LibUsbException {
		if (ftdi_chip_type.isSyncFifoType(ftdi.type)) return;
		throw LibUsbException.of(LIBUSB_ERROR_NOT_SUPPORTED,
			"Device doesn't support synchronous FIFO mode: " + ftdi.type);
	}

	private static timeval timeout(ftdi_context ftdi) {
		return Struct.write(new timeval().set(0, ftdi.usb_read_timeout * 1000));
	}

	private static double secDiff(Instant instant1, Instant instant2) {
		return DateUtil.seconds(Duration.between(instant1, instant2));
	}
}
