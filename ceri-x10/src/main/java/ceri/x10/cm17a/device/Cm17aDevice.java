package ceri.x10.cm17a.device;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.Enclosed;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandDispatcher;
import ceri.x10.command.CommandListener;
import ceri.x10.util.X10Controller;

public class Cm17aDevice implements X10Controller, Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final BlockingQueue<BaseCommand<?>> inQueue;
	private final Processor processor;
	private final CommandDispatcher dispatcher;

	public static Cm17aDevice of(Cm17aDeviceConfig config, Cm17aConnector connector) {
		return new Cm17aDevice(config, connector);
	}

	private Cm17aDevice(Cm17aDeviceConfig config, Cm17aConnector connector) {
		inQueue = new ArrayBlockingQueue<>(config.queueSize);
		BlockingQueue<BaseCommand<?>> outQueue = new LinkedBlockingQueue<>();
		processor = new Processor(config, connector, inQueue, outQueue);
		dispatcher = new CommandDispatcher(outQueue, config.pollTimeoutMs);
	}

	@Override
	public Enclosed<CommandListener> listen(CommandListener listener) {
		return dispatcher.listen(listener);
	}
	
	@Override
	public void command(BaseCommand<?> command) {
		logger.info("Command: {}", command);
		if (!Processor.supported(command.type))
			throw new UnsupportedOperationException("Function not supported: " + command.type);
		inQueue.add(command);
	}

	@Override
	public void close() {
		processor.close();
		dispatcher.close();
	}

}
