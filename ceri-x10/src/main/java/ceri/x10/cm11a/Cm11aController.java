package ceri.x10.cm11a;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandDispatcher;
import ceri.x10.command.CommandListener;
import ceri.x10.util.X10Controller;

public class Cm11aController implements X10Controller {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_QUEUE_SIZE_DEF = 100;
	private final BlockingQueue<BaseCommand<?>> inQueue;
	private final BlockingQueue<BaseCommand<?>> outQueue;
	private final Processor processor;
	private final CommandDispatcher dispatcher;

	public Cm11aController(Cm11aConnector connector, CommandListener listener) {
		inQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE_DEF);
		outQueue = new LinkedBlockingQueue<>();
		processor = Processor.builder(connector, inQueue, outQueue).build();
		dispatcher = new CommandDispatcher(outQueue, listener);
	}

	@Override
	public void command(BaseCommand<?> command) {
		logger.info("Command: {}", command);
		inQueue.add(command);
	}

	@Override
	public void close() {
		processor.close();
		dispatcher.close();
	}

}
