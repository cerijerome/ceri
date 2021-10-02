package ceri.serial.ftdi.jna;

import static ceri.serial.ftdi.jna.LibFtdi.ftdi_set_bitmode;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_purge_buffers;
import static ceri.serial.ftdi.jna.LibFtdiUtil.requireDev;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
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

	public static class FTDIProgressInfo {
		public final size_and_time first = new size_and_time();
		public final size_and_time prev = new size_and_time();
		public final size_and_time current = new size_and_time();
		public double totalTime;
		public double totalRate;
		public double currentRate;

		public void update(Instant now) {
			current.time = now;
			totalTime = secDiff(now, first.time);
			if (prev.totalBytes == 0) return;
			double currentTime = secDiff(now, prev.time);
			totalRate = current.totalBytes / totalTime;
			currentRate = (current.totalBytes - prev.totalBytes) / currentTime;
		}

	}

	// typedef int (FTDIStreamCallback)(uint8_t *buffer, int length, FTDIProgressInfo *progress,
	// void *userdata);
	public interface FTDIStreamCallback<T> {
		void invoke(Pointer buffer, int length, FTDIProgressInfo progress, T userdata)
			throws LibUsbException;
	}

	private static class FTDIStreamState<T> {
		private FTDIStreamCallback<T> callback;
		private libusb_transfer[] transfers;
		private T userdata;
		private int packetsize;
		private int activity = 0;
		private int result = 0;
		private Exception error = null;
		private final FTDIProgressInfo progress = new FTDIProgressInfo();

		private FTDIStreamState(FTDIStreamCallback<T> callback, T userdata, int numTransfers,
			int packetSize) {
			this.transfers = new libusb_transfer[numTransfers];
			this.callback = callback;
			this.userdata = userdata;
			this.packetsize = packetSize;
		}
	}

	/**
	 * Streaming read of data from the device. Uses asynchronous transfers in libusb-1.0 for
	 * high-performance streaming of data from a device interface. This function continuously
	 * transfers data until an error occurs, or the callback returns a nonzero value. For every
	 * contiguous block of received data, the callback will be invoked.
	 */
	public static <T> void ftdi_readstream(ftdi_context ftdi, FTDIStreamCallback<T> callback,
		T userData, int packetsPerTransfer, int numTransfers) throws LibUsbException {
		requireDev(ftdi);
		requireFifo(ftdi);
		ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_RESET);
		ftdi_usb_purge_buffers(ftdi);
		var state = new FTDIStreamState<>(callback, userData, numTransfers, ftdi.max_packet_size);
		submitTransfers(ftdi, state, packetsPerTransfer);
		setSyncBitMode(ftdi, state);
		completeStreamEvents(ftdi, state);
		free(state);
		if (state.error != null) throw LibUsbException.ADAPTER.apply(state.error);
	}

	private static <T> void submitTransfers(ftdi_context ftdi, FTDIStreamState<T> state,
		int packetsPerTransfer) {
		int bufferSize = packetsPerTransfer * ftdi.max_packet_size;
		for (int i = 0; i < state.transfers.length; i++) {
			try {
				state.transfers[i] = LibUsb.libusb_alloc_transfer(0);
				Memory m = new Memory(bufferSize);
				LibUsb.libusb_fill_bulk_transfer(state.transfers[i], ftdi.usb_dev, ftdi.out_ep, m,
					bufferSize, callback(i, state), null, 0);
				LibUsb.libusb_submit_transfer(Struct.write(state.transfers[i]));
			} catch (LibUsbException | RuntimeException e) {
				error(state, e);
				freeTransfer(i, state);
				return; // Need to wait for completion of submitted transfers
			}
		}
	}

	private static void setSyncBitMode(ftdi_context ftdi, FTDIStreamState<?> state) {
		try {
			ftdi_set_bitmode(ftdi, 0xff, ftdi_mpsse_mode.BITMODE_SYNCFF);
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
		}
	}

	private static <T> void completeStreamEvents(ftdi_context ftdi, FTDIStreamState<T> state) {
		try {
			state.progress.first.time = Instant.now();
			timeval timeout = timeout(ftdi);
			while (state.result == 0) {
				state.activity = 0;
				handleUsbEvents(ftdi, timeout);
				if (state.activity == 0) state.result = 1;
				if (state.error != null) updateProgress(state);
			}
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
		}
	}

	private static void handleUsbEvents(ftdi_context ftdi, timeval timeout) throws LibUsbException {
		try {
			LibUsb.libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		} catch (LibUsbException e) {
			// Retry once if interrupted
			if (e.error != LIBUSB_ERROR_INTERRUPTED) throw e;
			LibUsb.libusb_handle_events_timeout(ftdi.usb_ctx, timeout);
		}
	}

	private static <T> void updateProgress(FTDIStreamState<T> state) {
		try {
			Instant now = Instant.now();
			if (secDiff(now, state.progress.current.time) < PROGRESS_INTERVAL_MIN_SEC) return;
			state.progress.update(now);
			state.callback.invoke(null, 0, state.progress, state.userdata);
			state.progress.prev.set(state.progress.current);
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
		}
	}

	private static <T> libusb_transfer_cb_fn callback(int i, FTDIStreamState<T> state) {
		return p -> ftdi_readstream_cb(i, state);
	}

	/**
	 * Callback function for each libusb_transfer from libusb_handle_events_timeout call.
	 */
	private static <T> void ftdi_readstream_cb(int i, FTDIStreamState<T> state) {
		try {
			if (stateComplete(i, state)) return;
			libusb_transfer transfer = libusb_transfer_cb_fn.read(state.transfers[i]);
			if (!transferComplete(transfer)) return;
			readStream(transfer, state);
			LibUsb.libusb_submit_transfer(transfer);
			state.activity++;
		} catch (LibUsbException | RuntimeException e) {
			error(state, e);
			freeTransfer(i, state);
		}
	}

	private static boolean stateComplete(int i, FTDIStreamState<?> state) {
		if (state.transfers[i] != null && state.result == 0) return false;
		freeTransfer(i, state);
		return true;
	}

	private static boolean transferComplete(libusb_transfer transfer) {
		if (transfer.status == LIBUSB_TRANSFER_COMPLETED.value) return true;
		logger.warn("Unknown status: {} ({})", transfer.status, transfer.status());
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

	private static void error(FTDIStreamState<?> state, Exception e) {
		if (state.error == null) state.error = e;
		else logger.catching(e);
	}

	private static void free(FTDIStreamState<?> state) {
		for (int i = 0; i < state.transfers.length; i++)
			freeTransfer(i, state);
	}

	private static void freeTransfer(int i, FTDIStreamState<?> state) {
		free(state.transfers[i]);
		state.transfers[i] = null;
	}

	private static boolean free(libusb_transfer transfer) {
		return LogUtil.execute(logger, () -> LibUsb.libusb_free_transfer(transfer));
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
