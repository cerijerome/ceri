package ceri.serial.javax;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;

/**
 * Interface for serial connector functional layers on top of SerialPort. For example
 * SelfHealingSerialConnector, which detects serial port errors and attempts to reconnect. Only
 * covers SerialPort features currently in use; other methods may be added later.
 */
public interface SerialConnector extends Closeable, Listenable.Indirect<StateChange> {

	/**
	 * For condition-aware serial connectors, notify that it is broken. Useful if the connector
	 * itself cannot determine it is broken.
	 */
	default void broken() {
		throw new UnsupportedOperationException();
	}

	void connect() throws IOException;

	InputStream in();

	OutputStream out();

	void dtr(boolean on) throws IOException;

	void rts(boolean on) throws IOException;

	void flowControl(FlowControl flowControl) throws IOException;

	void breakBit(boolean on) throws IOException;

	/**
	 * Creates a no-op instance.
	 */
	static SerialConnector ofNull() {
		return new Null();
	}

	static class Null implements SerialConnector {
		private final Listenable<StateChange> listenable = Listenable.ofNull();
		private final InputStream in = IoStreamUtil.nullIn();
		private final OutputStream out = IoStreamUtil.nullOut();

		private Null() {}

		@Override
		public Listenable<StateChange> listeners() {
			return listenable;
		}

		@Override
		public void broken() {}

		@Override
		public void connect() {}

		@Override
		public void close() {}

		@Override
		public InputStream in() {
			return in;
		}

		@Override
		public OutputStream out() {
			return out;
		}

		@Override
		public void breakBit(boolean on) {}

		@Override
		public void dtr(boolean on) {}

		@Override
		public void flowControl(FlowControl flowControl) {}

		@Override
		public void rts(boolean on) {}
	}
}
