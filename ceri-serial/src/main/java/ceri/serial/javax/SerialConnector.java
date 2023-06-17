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

	void open() throws IOException;

	InputStream in();

	OutputStream out();

	void dtr(boolean on) throws IOException;

	void rts(boolean on) throws IOException;

	void flowControl(FlowControl flowControl) throws IOException;

	void breakBit(boolean on) throws IOException;

	/**
	 * A stateless, no-op instance.
	 */
	SerialConnector NULL = new SerialConnector() {
		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public void broken() {}

		@Override
		public void open() {}

		@Override
		public void close() {}

		@Override
		public InputStream in() {
			return IoStreamUtil.nullIn;
		}

		@Override
		public OutputStream out() {
			return IoStreamUtil.nullOut;
		}

		@Override
		public void breakBit(boolean on) {}

		@Override
		public void dtr(boolean on) {}

		@Override
		public void flowControl(FlowControl flowControl) {}

		@Override
		public void rts(boolean on) {}
	};
}
