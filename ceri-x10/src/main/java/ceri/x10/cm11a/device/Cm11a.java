package ceri.x10.cm11a.device;

import java.io.Closeable;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.serial.comm.SerialParams;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionGroup;
import ceri.x10.util.X10Controller;

public interface Cm11a extends X10Controller, Listenable.Indirect<StateChange>, Closeable {
	/** A stateless, no-op instance. */
	Cm11a NULL = new Null() {};
	/** Serial port settings. */
	SerialParams SERIAL = SerialParams.of(4800);

	@Override
	default boolean supports(Command command) {
		return command.group() != FunctionGroup.unsupported;
	}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Cm11a {
		@Override
		default Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		default void close() {}
	}
}
