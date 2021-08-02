package ceri.x10.cm11a.device;

import static ceri.x10.util.X10Controller.verifySupported;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.io.StateChange;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.LogUtil;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;

public class Cm11aEmulator implements Cm11a {
	private static final Logger logger = LogManager.getLogger();
	public final Listeners<StateChange> listeners = Listeners.of();
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static Cm11aEmulator of(int queuePollTimeoutMs) {
		return new Cm11aEmulator(queuePollTimeoutMs);
	}

	private Cm11aEmulator(int queuePollTimeoutMs) {
		dispatcher = Dispatcher.of(queuePollTimeoutMs, CommandListener::dispatcher);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public Enclosed<RuntimeException, CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public void command(Command command) throws IOException {
		verifySupported(this, command);
		dispatcher.dispatch(command);
	}

	@Override
	public void close() {
		LogUtil.close(logger, dispatcher);
	}
}
