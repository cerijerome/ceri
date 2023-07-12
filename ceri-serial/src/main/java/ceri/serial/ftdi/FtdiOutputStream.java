package ceri.serial.ftdi;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import java.io.IOException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.jna.io.JnaOutputStream;
import ceri.jna.util.JnaUtil;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * OutputStream for FTDI transfers.
 */
public abstract class FtdiOutputStream extends JnaOutputStream {
	/** A no-op, stateless instance. */
	public static final Null NULL = new Null();

	/**
	 * Configure write buffer chunk size.
	 */
	public abstract void chunkSize(int size) throws LibUsbException;

	/**
	 * Get the write buffer chunk size.
	 */
	public abstract int chunkSize() throws LibUsbException;

	/**
	 * Purges the write buffer on the chip.
	 */
	public abstract void purgeBuffer() throws LibUsbException;

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	public FtdiTransferControl writeSubmit(int... bytes) throws LibUsbException {
		return writeSubmit(bytes(bytes));
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	public FtdiTransferControl writeSubmit(byte[] data) throws LibUsbException {
		return writeSubmit(data, 0);
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	public FtdiTransferControl writeSubmit(byte[] data, int offset) throws LibUsbException {
		return writeSubmit(data, offset, data.length - offset);
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	@SuppressWarnings("resource")
	public FtdiTransferControl writeSubmit(byte[] data, int offset, int len)
		throws LibUsbException {
		ArrayUtil.validateSlice(data.length, offset, len);
		var m = JnaUtil.mallocBytes(data, offset, len);
		return writeSubmit(m, len);
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	public FtdiTransferControl writeSubmit(Pointer buf, int size) throws LibUsbException {
		ensureOpen();
		return submit(buf, size);
	}

	/**
	 * Submits an asynchronous write.
	 */
	protected abstract FtdiTransferControl submit(Pointer buf, int size) throws LibUsbException;

	@Override
	protected void ensureOpen() throws LibUsbException {
		if (closed()) throw LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "Device is closed");
	}

	/**
	 * A no-op, stateless implementation.
	 */
	public static class Null extends FtdiOutputStream {
		@Override
		public void chunkSize(int size) throws LibUsbException {}

		@Override
		public int chunkSize() throws LibUsbException {
			return 0;
		}

		@Override
		public void purgeBuffer() throws LibUsbException {}

		@Override
		protected FtdiTransferControl submit(Pointer buf, int size) throws LibUsbException {
			return FtdiTransferControl.NULL;
		}

		@Override
		protected int write(Memory buffer, int len) throws IOException {
			return len;
		}
	}
}
