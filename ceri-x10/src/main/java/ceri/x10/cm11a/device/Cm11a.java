package ceri.x10.cm11a.device;

import java.io.Closeable;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.util.Enclosed;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;
import ceri.x10.util.X10Controller;

public interface Cm11a extends X10Controller, Listenable.Indirect<StateChange>, Closeable {
	static Cm11a NULL = new Cm11a() {
		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}

		@Override
		public Enclosed<CommandListener> listen(CommandListener listener) {
			return Enclosed.noOp(listener);
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
