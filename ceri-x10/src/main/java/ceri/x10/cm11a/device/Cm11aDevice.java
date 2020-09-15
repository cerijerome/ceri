package ceri.x10.cm11a.device;

import static ceri.x10.util.X10Controller.verifySupported;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.LogUtil;
import ceri.x10.cm11a.protocol.Status;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;
import ceri.x10.command.FunctionGroup;
import ceri.x10.util.X10Controller;

public class Cm11aDevice implements X10Controller, Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Processor processor;
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static Cm11aDevice of(Cm11aDeviceConfig config, Cm11aConnector connector) {
		return new Cm11aDevice(config, connector);
	}

	private Cm11aDevice(Cm11aDeviceConfig config, Cm11aConnector connector) {
		BlockingQueue<Command> outQueue = new LinkedBlockingQueue<>();
		processor = new Processor(config, connector, outQueue);
		dispatcher =
			Dispatcher.of(outQueue, config.queuePollTimeoutMs, CommandListener::dispatcher);
	}

	@Override
	public boolean supports(Command command) {
		return command.group() != FunctionGroup.unsupported;
	}

	@Override
	public void command(Command command) throws IOException {
		verifySupported(this, command);
		logger.info("Command: {}", command);
		processor.command(command);
	}

	public Status requestStatus() throws IOException {
		logger.info("Request: status");
		return processor.requestStatus();
	}

	@Override
	public Enclosed<CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}

	@Override
	public void close() {
		LogUtil.close(logger, dispatcher, processor);
	}

}
