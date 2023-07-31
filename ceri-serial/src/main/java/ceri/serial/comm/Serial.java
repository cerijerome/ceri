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
	/** A stateless, no-op instance. */
	Serial NULL = new Null() {};

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
	 * A stateless, no-op implementation.
	 */
	interface Null extends Connector.Null, Serial.Fixable {

		@Override
		default String port() {
			return StringUtil.NULL_STRING;
		}

		@Override
		default void inBufferSize(int size) {}

		@Override
		default int inBufferSize() {
			return 0;
		}

		@Override
		default void outBufferSize(int size) {}

		@Override
		default int outBufferSize() {
			return 0;
		}

		@Override
		default void params(SerialParams params) throws IOException {}

		@Override
		default SerialParams params() {
			return SerialParams.DEFAULT;
		}

		@Override
		default void flowControl(Collection<FlowControl> flowControl) throws IOException {}

		@Override
		default Set<FlowControl> flowControl() {
			return FlowControl.NONE;
		}

		@Override
		default void brk(boolean on) throws IOException {}

		@Override
		default void rts(boolean on) throws IOException {}

		@Override
		default void dtr(boolean on) throws IOException {}

		@Override
		default boolean rts() throws IOException {
			return false;
		}

		@Override
		default boolean dtr() throws IOException {
			return false;
		}

		@Override
		default boolean cd() throws IOException {
			return false;
		}

		@Override
		default boolean cts() throws IOException {
			return false;
		}

		@Override
		default boolean dsr() throws IOException {
			return false;
		}

		@Override
		default boolean ri() throws IOException {
			return false;
		}
	}
}
