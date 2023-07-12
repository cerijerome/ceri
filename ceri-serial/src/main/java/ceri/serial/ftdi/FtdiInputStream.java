package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdiStream.PROGRESS_INTERVAL_SEC;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import java.io.IOException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.jna.io.JnaInputStream;
import ceri.serial.ftdi.FtdiConnector.StreamCallback;
import ceri.serial.libusb.jna.LibUsbException;

public abstract class FtdiInputStream extends JnaInputStream {
	/** A no-op, stateless instance. */
	public static final Null NULL = new Null();

	/**
	 * Configure read buffer chunk size.
	 */
	public abstract void chunkSize(int size) throws LibUsbException;

	/**
	 * Get the read buffer chunk size.
	 */
	public abstract int chunkSize() throws LibUsbException;

	/**
	 * Purges the read buffer on the chip.
	 */
	public abstract void purgeBuffer() throws LibUsbException;

	/**
	 * Reads data from the chip in asynchronous mode.
	 */
	public FtdiTransferControl readSubmit(Pointer buf, int size) throws LibUsbException {
		ensureOpen();
		return submit(buf, size);
	}

	/**
	 * Reads data from the chip in asynchronous streaming mode.
	 */
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers)
		throws LibUsbException {
		readStream(callback, packetsPerTransfer, numTransfers, PROGRESS_INTERVAL_SEC);
	}

	/**
	 * Reads data from the chip in asynchronous streaming mode.
	 */
	public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws LibUsbException {
		ensureOpen();
		stream(callback, packetsPerTransfer, numTransfers, progressIntervalSec);
	}

	/**
	 * Submits an asynchronous read.
	 */
	protected abstract FtdiTransferControl submit(Pointer buf, int size) throws LibUsbException;

	/**
	 * Starts an asynchronous streaming read.
	 */
	protected abstract void stream(StreamCallback callback, int packetsPerTransfer,
		int numTransfers, double progressIntervalSec) throws LibUsbException;

	@Override
	protected void ensureOpen() throws LibUsbException {
		if (closed()) throw LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "Device is closed");
	}

	/**
	 * A no-op, stateless implementation.
	 */
	public static class Null extends FtdiInputStream {
		@Override
		public void chunkSize(int size) {}

		@Override
		public int chunkSize() {
			return 0;
		}

		@Override
		public void purgeBuffer() throws LibUsbException {}

		@Override
		protected int read(Memory buffer, int len) throws IOException {
			return len;
		}

		@Override
		protected FtdiTransferControl submit(Pointer buf, int size) throws LibUsbException {
			return FtdiTransferControl.NULL;
		}

		@Override
		protected void stream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
			double progressIntervalSec) throws LibUsbException {}
	}
}
