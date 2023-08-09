package ceri.x10.cm17a.device;

import static ceri.x10.util.X10Controller.verifySupported;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.LogUtil;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;
import ceri.x10.command.FunctionGroup;

public class Cm17aDevice implements Cm17a {
	private static final Logger logger = LogManager.getLogger();
	static final List<FunctionGroup> supportedGroups =
		List.of(FunctionGroup.unit, FunctionGroup.dim);
	private final Cm17aConnector connector;
	private final Processor processor;
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static Cm17aDevice of(Cm17aDeviceConfig config, Cm17aConnector connector) {
		return new Cm17aDevice(config, connector);
	}

	private Cm17aDevice(Cm17aDeviceConfig config, Cm17aConnector connector) {
		this.connector = connector;
		dispatcher = Dispatcher.of(config.queuePollTimeoutMs, CommandListener::dispatcher);
		processor = new Processor(config, connector);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return connector.listeners();
	}

	@Override
	public Enclosed<RuntimeException, CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public boolean supports(Command command) {
		return supportsCommand(command);
	}

	@Override
	public void command(Command command) throws IOException {
		verifySupported(this, command);
		logger.info("Command: {}", command);
		processor.command(command);
		dispatcher.dispatch(command);
	}

	@Override
	public void close() {
		LogUtil.close(processor, dispatcher);
	}

	static boolean supportsCommand(Command command) {
		return supportedGroups.contains(command.group());
	}
}
