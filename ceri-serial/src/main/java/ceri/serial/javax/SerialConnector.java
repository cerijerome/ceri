package ceri.serial.javax;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;

/**
 * Interface for serial connector functional layers on top of SerialPort. For example
 * SelfHealingSerialConnector, which detects serial port errors and attempts to reconnect. Only
 * covers SerialPort features currently in use; other methods may be added later.
 */
public interface SerialConnector extends Closeable {

	static enum State {
		fixed,
		broken;
	}

	/**
	 * For condition-aware serial connectors, notify that it is broken. Useful if the connector
	 * itself cannot determine it is broken.
	 */
	default void broken() {
		throw new UnsupportedOperationException();
	}

	/**
	 * For condition-aware serial connectors, used to listen for fixed and broken events.
	 */
	default Listenable<State> listeners() {
		throw new UnsupportedOperationException();
	}

	void connect() throws IOException;

	InputStream in();

	OutputStream out();

	void setDtr(boolean on) throws IOException;

	void setRts(boolean on) throws IOException;

	void setFlowControl(FlowControl flowControl) throws IOException;

	void setBreakBit(boolean on) throws IOException;

}
