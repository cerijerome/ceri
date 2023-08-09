package ceri.x10.cm17a.device;

import java.io.IOException;
import ceri.common.io.Fixable;

public interface Cm17aConnector extends Fixable {
	/** A stateless, no-op instance. */
	Cm17aConnector NULL = new Null() {};

	void rts(boolean on) throws IOException;

	void dtr(boolean on) throws IOException;

	/**
	 * Create an instance that delegates to a serial port.
	 */
	static Cm17aConnector of(ceri.serial.comm.Serial.Fixable serial) {
		return new Serial(serial);
	}

	/**
	 * Wrapper that delegates to a serial port.
	 */
	class Serial extends Fixable.Wrapper<ceri.serial.comm.Serial.Fixable>
		implements Cm17aConnector {
		private Serial(ceri.serial.comm.Serial.Fixable serial) {
			super(serial);
		}

		@Override
		public void rts(boolean on) throws IOException {
			delegate.rts(on);
		}

		@Override
		public void dtr(boolean on) throws IOException {
			delegate.dtr(on);
		}
	}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Fixable.Null, Cm17aConnector {
		@Override
		default void rts(boolean on) throws IOException {}

		@Override
		default void dtr(boolean on) throws IOException {}
	}
}
