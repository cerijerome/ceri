package ceri.x10.cm17a.device;

import java.io.Closeable;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.util.Enclosure;
import ceri.x10.command.Command;
import ceri.x10.util.X10Controller;

public interface Cm17a extends X10Controller, Listenable.Indirect<StateChange>, Closeable {
	/**
	 * A stateless, no-op instance.
	 */
	Cm17a NULL = new Cm17a() {
		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public Enclosure<Command.Listener> listen(Command.Listener listener) {
			return Enclosure.noOp(listener);
		}

		@Override
		public boolean supports(Command command) {
			return true;
		}

		@Override
		public void command(Command command) {}

		@Override
		public void close() {}
	};
}
