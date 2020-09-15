package ceri.x10.cm17a.device;

import static ceri.x10.util.X10Controller.verifySupported;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.io.StateChange;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.Dispatcher;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;
import ceri.x10.command.FunctionType;
import ceri.x10.util.X10Controller;

public class Cm17aDevice implements X10Controller, Listenable.Indirect<StateChange>, Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final List<FunctionType> supportedFns =
		List.of(FunctionType.off, FunctionType.on, FunctionType.dim, FunctionType.bright);
	private final Cm17aConnector connector;
	private final Processor processor;
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static Cm17aDevice of(Cm17aDeviceConfig config, Cm17aConnector connector) {
		return new Cm17aDevice(config, connector);
	}

	private Cm17aDevice(Cm17aDeviceConfig config, Cm17aConnector connector) {
		this.connector = connector;
		BlockingQueue<Command> outQueue = new LinkedBlockingQueue<>();
		processor = new Processor(config, connector, outQueue);
		dispatcher =
			Dispatcher.of(outQueue, config.queuePollTimeoutMs, CommandListener::dispatcher);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return connector.listeners();
	}

	@Override
	public Enclosed<CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public boolean supports(Command command) {
		return supportedFns.contains(command.type());
	}

	@Override
	public void command(Command command) throws IOException {
		verifySupported(this, command);
		logger.info("Command: {}", command);
		processor.command(command);
	}

	@Override
	public void close() {
		processor.close();
		dispatcher.close();
	}

}
