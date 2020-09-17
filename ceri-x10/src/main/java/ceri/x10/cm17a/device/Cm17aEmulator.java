package ceri.x10.cm17a.device;

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

public class Cm17aEmulator implements Cm17a {
	private static final Logger logger = LogManager.getLogger();
	public final Listeners<StateChange> listeners = Listeners.of();
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static Cm17aEmulator of(int queuePollTimeoutMs) {
		return new Cm17aEmulator(queuePollTimeoutMs);
	}

	private Cm17aEmulator(int queuePollTimeoutMs) {
		dispatcher = Dispatcher.of(queuePollTimeoutMs, CommandListener::dispatcher);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public Enclosed<CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public boolean supports(Command command) {
		return Cm17aDevice.supportsCommand(command);
	}

	@Override
	public void command(Command command) throws IOException {
		verifySupported(this, command);
		CommandListener.dispatcher(command);
	}

	@Override
	public void close() {
		LogUtil.close(logger, dispatcher);
	}
}
