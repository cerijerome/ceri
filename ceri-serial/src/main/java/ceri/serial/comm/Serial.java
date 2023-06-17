package ceri.serial.comm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.event.Listenable;
import ceri.common.io.Connector;
import ceri.common.io.StateChange;
import ceri.common.text.StringUtil;

/**
 * Interface for serial connector functional layers on top of SerialPort. For example
 * SelfHealingSerialConnector, which detects serial port errors and attempts to reconnect. Only
 * covers SerialPort features currently in use; other methods may be added later.
 */
public interface Serial extends Connector {
	String PORT_INVALID = "<invalid>";
	/** No-op serial connector instance */
	Null NULL = new Null();

	static String port(Serial serial) {
		return serial != null ? serial.port() : PORT_INVALID;
	}

	@Override
	default java.lang.String name() {
		return Connector.super.name() + ":" + port();
	}

	String port();

	void inBufferSize(int size);

	int inBufferSize();

	void outBufferSize(int size);

	int outBufferSize();

	void params(SerialParams params) throws IOException;

	SerialParams params();

	default void flowControl(FlowControl... flowControl) throws IOException {
		flowControl(Arrays.asList(flowControl));
	}

	void flowControl(Collection<FlowControl> flowControl) throws IOException;

	Set<FlowControl> flowControl();

	void brk(boolean on) throws IOException;

	void rts(boolean on) throws IOException;

	void dtr(boolean on) throws IOException;

	boolean rts() throws IOException;

	boolean dtr() throws IOException;

	boolean cd() throws IOException;

	boolean cts() throws IOException;

	boolean dsr() throws IOException;

	boolean ri() throws IOException;

	/**
	 * A stateless, no-op implementation.
	 */
	static class Null extends Connector.Null implements Serial {

		@Override
		public String port() {
			return StringUtil.NULL_STRING;
		}

		@Override
		public void inBufferSize(int size) {}

		@Override
		public int inBufferSize() {
			return 0;
		}

		@Override
		public void outBufferSize(int size) {}

		@Override
		public int outBufferSize() {
			return 0;
		}

		@Override
		public void params(SerialParams params) throws IOException {}

		@Override
		public SerialParams params() {
			return SerialParams.DEFAULT;
		}

		@Override
		public void flowControl(Collection<FlowControl> flowControl) throws IOException {}

		@Override
		public Set<FlowControl> flowControl() {
			return FlowControl.NONE;
		}

		@Override
		public void brk(boolean on) throws IOException {}

		@Override
		public void rts(boolean on) throws IOException {}

		@Override
		public void dtr(boolean on) throws IOException {}

		@Override
		public boolean rts() throws IOException {
			return false;
		}

		@Override
		public boolean dtr() throws IOException {
			return false;
		}

		@Override
		public boolean cd() throws IOException {
			return false;
		}

		@Override
		public boolean cts() throws IOException {
			return false;
		}

		@Override
		public boolean dsr() throws IOException {
			return false;
		}

		@Override
		public boolean ri() throws IOException {
			return false;
		}
	}

	/**
	 * An extension of Serial that is aware of state.
	 */
	interface Fixable extends Serial, Connector.Fixable {
		/** No-op, fixable, serial connector instance */
		Null NULL = new Null();

		static class Null extends Serial.Null implements Serial.Fixable {
			@Override
			public Listenable<StateChange> listeners() {
				return Listenable.ofNull();
			}

			@Override
			public void open() throws IOException {}
		}
	}

}
