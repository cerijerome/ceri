package ceri.x10.cm11a.device;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.Enclosed;
import ceri.log.util.LogUtil;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandDispatcher;
import ceri.x10.command.CommandListener;
import ceri.x10.util.X10Controller;

public class Cm11aDevice implements X10Controller, Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_QUEUE_SIZE_DEF = 100;
	private final BlockingQueue<BaseCommand<?>> inQueue;
	private final Processor processor;
	private final CommandDispatcher dispatcher;

	public static Cm11aDevice of(Cm11aDeviceConfig config, Cm11aConnector connector) {
		return new Cm11aDevice(config, connector);
	}

	private Cm11aDevice(Cm11aDeviceConfig config, Cm11aConnector connector) {
		inQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE_DEF);
		BlockingQueue<BaseCommand<?>> outQueue = new LinkedBlockingQueue<>();
		processor = new Processor(config, connector, inQueue, outQueue);
		dispatcher = new CommandDispatcher(outQueue, config.pollTimeoutMs);
	}

	@Override
	public void command(BaseCommand<?> command) {
		logger.info("Command: {}", command);
		inQueue.add(command);
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
