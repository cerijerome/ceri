package ceri.serial.ftdi;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.serial.ftdi.jna.LibFtdiStream.PROGRESS_INTERVAL_SEC;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.io.Connector;
import ceri.jna.util.JnaUtil;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_usb_strings;

/**
 * Sub-set of FTDI operations for device controllers.
 */
public interface FtdiConnector extends Connector {
	/** A stateless, no-op instance. */
	Null NULL = new Null();

	/**
	 * Get device descriptor strings: manufacturer, description and serial code.
	 */
	ftdi_usb_strings descriptor() throws IOException;

	/**
	 * Send reset request to device.
	 */
	void usbReset() throws IOException;

	/**
	 * Set pin bit mode.
	 */
	void bitMode(FtdiBitMode bitMode) throws IOException;

	/**
	 * Convenience method to turn bitbang mode on or off.
	 */
	default void bitBang(boolean on) throws IOException {
		if (on) bitMode(FtdiBitMode.BITBANG);
		else bitMode(FtdiBitMode.OFF);
	}

	/**
	 * Set serial baud rate.
	 */
	void baud(int baud) throws IOException;

	/**
	 * Sets data bits, stop bits, parity, and break.
	 */
	void lineParams(FtdiLineParams params) throws IOException;

	/**
	 * Set flow control handshaking.
	 */
	void flowControl(FtdiFlowControl flowControl) throws IOException;

	/**
	 * Enable/disable DTR.
	 */
	void dtr(boolean state) throws IOException;

	/**
	 * Enable/disable RTS.
	 */
	void rts(boolean state) throws IOException;

	/**
	 * Reads 8-bit pin status.
	 */
	int readPins() throws IOException;

	/**
	 * Get the 16-bit modem status.
	 */
	int pollModemStatus() throws IOException;

	/**
	 * Set chip internal buffer latency.
	 */
	void latencyTimer(int latency) throws IOException;

	/**
	 * Get chip internal buffer latency.
	 */
	int latencyTimer() throws IOException;

	/**
	 * Configures the read buffer chunk size.
	 */
	void readChunkSize(int size) throws IOException;

	/**
	 * Returns the read buffer chunk size.
	 */
	int readChunkSize() throws IOException;

	/**
	 * Configures the write buffer chunk size.
	 */
	void writeChunkSize(int size) throws IOException;

	/**
	 * Returns the write buffer chunk size.
	 */
	int writeChunkSize() throws IOException;

	/**
	 * Purge read and writes buffers on the chip.
	 */
	default void purgeBuffers() throws IOException {
		purgeReadBuffer();
		purgeWriteBuffer();
	}

	/**
	 * Purges the read buffer on the chip.
	 */
	void purgeReadBuffer() throws IOException;

	/**
	 * Purges the write buffer on the chip.
	 */
	void purgeWriteBuffer() throws IOException;

	/**
	 * Reads data from the chip in asynchronous mode.
	 */
	FtdiTransferControl readSubmit(Pointer buffer, int len) throws IOException;

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	default FtdiTransferControl writeSubmit(int... bytes) throws IOException {
		return writeSubmit(bytes(bytes));
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	default FtdiTransferControl writeSubmit(byte[] data) throws IOException {
		return writeSubmit(data, 0);
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	default FtdiTransferControl writeSubmit(byte[] data, int offset) throws IOException {
		return writeSubmit(data, offset, data.length - offset);
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	@SuppressWarnings("resource")
	default FtdiTransferControl writeSubmit(byte[] data, int offset, int len) throws IOException {
		ArrayUtil.validateSlice(data.length, offset, len);
		var m = JnaUtil.mallocBytes(data, offset, len);
		return writeSubmit(m, len);
	}

	/**
	 * Writes data to the chip in asynchronous mode.
	 */
	FtdiTransferControl writeSubmit(Pointer buffer, int len) throws IOException;

	/**
	 * Reads data from the chip in asynchronous streaming mode.
	 */
	default void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers)
		throws IOException {
		readStream(callback, packetsPerTransfer, numTransfers, PROGRESS_INTERVAL_SEC);
	}

	/**
	 * Reads data from the chip in asynchronous streaming mode.
	 */
	void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
		double progressIntervalSec) throws IOException;

	/**
	 * A state-aware extension.
	 */
	interface Fixable extends FtdiConnector, Connector.Fixable {}

	/**
	 * Callback to register for streaming events.
	 */
	interface StreamCallback {
		boolean invoke(FtdiProgressInfo progress, ByteBuffer buffer);
	}

	/**
	 * A stateless, no-op implementation.
	 */
	static class Null extends Connector.Null implements FtdiConnector.Fixable {
		Null() {}

		@Override
		public ftdi_usb_strings descriptor() throws IOException {
			return ftdi_usb_strings.NULL;
		}

		@Override
		public void usbReset() throws IOException {}

		@Override
		public void bitMode(FtdiBitMode bitmode) throws IOException {}

		@Override
		public void baud(int baud) throws IOException {}

		@Override
		public void lineParams(FtdiLineParams properties) throws IOException {}

		@Override
		public void flowControl(FtdiFlowControl flowControl) throws IOException {}

		@Override
		public void dtr(boolean state) throws IOException {}

		@Override
		public void rts(boolean state) throws IOException {}

		@Override
		public int readPins() throws IOException {
			return 0;
		}

		@Override
		public int pollModemStatus() throws IOException {
			return 0;
		}

		@Override
		public void latencyTimer(int latency) throws IOException {}

		@Override
		public int latencyTimer() throws IOException {
			return 0;
		}

		@Override
		public void readChunkSize(int size) throws IOException {}

		@Override
		public int readChunkSize() throws IOException {
			return 0;
		}

		@Override
		public void writeChunkSize(int size) throws IOException {}

		@Override
		public int writeChunkSize() throws IOException {
			return 0;
		}

		@Override
		public void purgeReadBuffer() throws IOException {}

		@Override
		public void purgeWriteBuffer() throws IOException {}

		@Override
		public FtdiTransferControl readSubmit(Pointer buffer, int len) throws IOException {
			return FtdiTransferControl.NULL;
		}

		@Override
		public FtdiTransferControl writeSubmit(Pointer buf, int size) throws IOException {
			return FtdiTransferControl.NULL;
		}

		@Override
		public void readStream(StreamCallback callback, int packetsPerTransfer, int numTransfers,
			double progressIntervalSec) throws IOException {}
	}
}
