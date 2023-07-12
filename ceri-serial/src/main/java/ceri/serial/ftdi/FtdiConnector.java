package ceri.serial.ftdi;

import java.io.IOException;
import java.nio.ByteBuffer;
import ceri.common.io.Connector;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Sub-set of FTDI operations for device controllers.
 */
public interface FtdiConnector extends Connector {
	/** A stateless, no-op instance. */
	Null NULL = new Null();

	@Override
	FtdiInputStream in();

	@Override
	FtdiOutputStream out();

	/**
	 * Get device descriptor strings: manufacturer, description and serial code.
	 */
	FtdiDescriptor descriptor() throws LibUsbException;

	/**
	 * Send reset request to device.
	 */
	void usbReset() throws LibUsbException;

	/**
	 * Set pin bit mode.
	 */
	void bitMode(FtdiBitMode bitMode) throws LibUsbException;

	/**
	 * Convenience method to turn bitbang mode on or off.
	 */
	default void bitBang(boolean on) throws LibUsbException {
		if (on) bitMode(FtdiBitMode.BITBANG);
		else bitMode(FtdiBitMode.OFF);
	}

	/**
	 * Set serial baud rate.
	 */
	void baudRate(int baudRate) throws LibUsbException;

	/**
	 * Sets data bits, stop bits, parity, and break.
	 */
	void lineParams(FtdiLineParams properties) throws LibUsbException;

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
	int pollModemStatus() throws LibUsbException;

	/**
	 * Set chip internal buffer latency.
	 */
	void latencyTimer(int latency) throws LibUsbException;

	/**
	 * Get chip internal buffer latency.
	 */
	int latencyTimer() throws LibUsbException;

	/**
	 * Purge read and writes buffers on the chip.
	 */
	@SuppressWarnings("resource")
	default void purgeBuffers() throws LibUsbException {
		in().purgeBuffer();
		out().purgeBuffer();
	}

	/**
	 * A state-aware extension.
	 */
	interface Fixable extends FtdiConnector, Connector.Fixable {}

	/**
	 * Callback to register for streaming events.
	 */
	public static interface StreamCallback {
		boolean invoke(FtdiProgressInfo progress, ByteBuffer buffer);
	}

	/**
	 * A stateless, no-op implementation.
	 */
	static class Null extends Connector.Null implements FtdiConnector.Fixable {
		Null() {}

		@Override
		public FtdiInputStream in() {
			return FtdiInputStream.NULL;
		}

		@Override
		public FtdiOutputStream out() {
			return FtdiOutputStream.NULL;
		}

		@Override
		public FtdiDescriptor descriptor() throws LibUsbException {
			return FtdiDescriptor.NULL;
		}

		@Override
		public void usbReset() throws LibUsbException {}

		@Override
		public void bitMode(FtdiBitMode bitmode) throws LibUsbException {}

		@Override
		public void baudRate(int baudRate) throws LibUsbException {}

		@Override
		public void lineParams(FtdiLineParams properties) throws LibUsbException {}

		@Override
		public void flowControl(FtdiFlowControl flowControl) throws LibUsbException {}

		@Override
		public void dtr(boolean state) throws LibUsbException {}

		@Override
		public void rts(boolean state) throws LibUsbException {}

		@Override
		public int readPins() throws LibUsbException {
			return 0;
		}

		@Override
		public int pollModemStatus() throws LibUsbException {
			return 0;
		}

		@Override
		public void latencyTimer(int latency) throws LibUsbException {}

		@Override
		public int latencyTimer() throws LibUsbException {
			return 0;
		}
	}
}
