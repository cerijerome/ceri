package ceri.serial.ftdi;

import static ceri.common.math.MathUtil.ubyte;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import ceri.common.collection.ArrayUtil;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;

/**
 * Sub-set of FTDI operations for device controllers.
 */
public interface FtdiConnector extends Closeable, Listenable.Indirect<StateChange> {
	/**
	 * For condition-aware serial connectors, notify that it is broken. Useful if the connector
	 * itself cannot determine it is broken.
	 */
	default void broken() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempts to open the ftdi device. If it fails, self-healing will kick in.
	 */
	void open() throws IOException;

	/**
	 * Set FTDI bit mode.
	 */
	void bitmode(FtdiBitMode bitmode) throws IOException;

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
	 * Read FTDI pins (b-bit).
	 */
	int readPins() throws IOException;

	/**
	 * Read one byte. -1 if not available.
	 */
	default int read() throws IOException {
		byte[] data = read(1);
		return data.length == 0 ? -1 : ubyte(data[0]);
	}

	/**
	 * Read byte array of given size.
	 */
	default byte[] read(int size) throws IOException {
		byte[] bytes = new byte[size];
		int n = read(bytes);
		return n == size ? bytes : Arrays.copyOf(bytes, n);
	}

	/**
	 * Read into byte array.
	 */
	default int read(byte[] buffer) throws IOException {
		return read(buffer, 0);
	}

	/**
	 * Read into byte array.
	 */
	default int read(byte[] buffer, int offset) throws IOException {
		return read(buffer, offset, buffer.length - offset);
	}

	/**
	 * Read into byte array.
	 */
	int read(byte[] buffer, int offset, int length) throws IOException;

	/**
	 * Write bytes.
	 */
	default int write(int... data) throws IOException {
		return write(ArrayUtil.bytes(data));
	}

	/**
	 * Write bytes.
	 */
	default int write(byte[] data) throws IOException {
		return write(data, 0);
	}

	/**
	 * Write bytes.
	 */
	default int write(byte[] data, int offset) throws IOException {
		return write(data, offset, data.length - offset);
	}

	/**
	 * Write bytes.
	 */
	int write(byte[] data, int offset, int length) throws IOException;

	/**
	 * A stateless, no-op instance.
	 */
	Null NULL = new Null();
	
	static class Null implements FtdiConnector {
		Null() {}

		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public void open() {}

		@Override
		public void bitmode(FtdiBitMode bitmode) {}

		@Override
		public void flowControl(FtdiFlowControl flowControl) {}

		@Override
		public void dtr(boolean state) {}

		@Override
		public void rts(boolean state) {}

		@Override
		public int readPins() {
			return 0;
		}

		@Override
		public int read(byte[] buffer, int offset, int length) {
			return length;
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			return length;
		}

		@Override
		public void close() {}
	}
}
