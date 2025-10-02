package ceri.x10.cm11a.device;

import java.io.IOException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.Enclosure;
import ceri.common.io.StateChange;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.LogUtil;
import ceri.x10.command.Command;
import ceri.x10.util.X10Controller;

public class Cm11aEmulator implements Cm11a {
	public final Listeners<StateChange> listeners = Listeners.of();
	private final Dispatcher<Command.Listener, Command> dispatcher;

	public static Cm11aEmulator of(int queuePollTimeoutMs) {
		return new Cm11aEmulator(queuePollTimeoutMs);
	}

	private Cm11aEmulator(int queuePollTimeoutMs) {
		dispatcher = Dispatcher.of(queuePollTimeoutMs, Command.Listener::dispatcher);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public Enclosure<Command.Listener> listen(Command.Listener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public void command(Command command) throws IOException {
		X10Controller.verifySupported(this, command);
		dispatcher.dispatch(command);
	}

	@Override
	public void close() {
		LogUtil.close(dispatcher);
	}
}
