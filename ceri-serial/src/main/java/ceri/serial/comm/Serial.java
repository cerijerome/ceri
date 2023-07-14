package ceri.serial.comm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.io.Connector;
import ceri.common.text.StringUtil;

/**
 * Interface for serial connector functionality.
 */
public interface Serial extends Connector {
	/** No-op, stateless, serial instance. */
	Null NULL = new Null();

	@Override
	default java.lang.String name() {
		return port();
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
	 * An extension of Serial that is aware of state.
	 */
	interface Fixable extends Serial, Connector.Fixable {}

	/**
	 * A no-op, stateless, serial implementation.
	 */
	static class Null extends Connector.Null implements Serial.Fixable {

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

}
