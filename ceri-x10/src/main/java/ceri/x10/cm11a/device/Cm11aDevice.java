package ceri.x10.cm11a.device;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.Dispatcher;
import ceri.log.util.LogUtil;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;
import ceri.x10.command.FunctionGroup;
import ceri.x10.util.X10Controller;
import ceri.x10.util.X10Util;

public class Cm11aDevice implements X10Controller, Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_QUEUE_SIZE_DEF = 100;
	private final BlockingQueue<Command> inQueue;
	private final Processor processor;
	private final Dispatcher<CommandListener, Command> dispatcher;

	public static Cm11aDevice of(Cm11aDeviceConfig config, Cm11aConnector connector) {
		return new Cm11aDevice(config, connector);
	}

	private Cm11aDevice(Cm11aDeviceConfig config, Cm11aConnector connector) {
		inQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE_DEF);
		BlockingQueue<Command> outQueue = new LinkedBlockingQueue<>();
		processor = new Processor(config, connector, inQueue, outQueue);
		dispatcher = Dispatcher.of(outQueue, config.pollTimeoutMs, CommandListener::dispatcher);
	}

	@Override
	public boolean supports(Command command) {
		return command.group() != FunctionGroup.unsupported;
	}

	@Override
	public void command(Command command) {
		X10Util.verifySupported(this, command);
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
