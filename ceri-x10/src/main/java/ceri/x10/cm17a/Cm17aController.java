package ceri.x10.cm17a;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandDispatcher;
import ceri.x10.command.CommandListener;
import ceri.x10.util.X10Controller;

public class Cm17aController implements X10Controller {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_QUEUE_SIZE_DEF = 100;
	private final BlockingQueue<BaseCommand<?>> inQueue;
	private final Processor processor;
	private final CommandDispatcher dispatcher;

	public Cm17aController(Cm17aConnector connector, CommandListener listener) throws IOException {
		inQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE_DEF);
		BlockingQueue<BaseCommand<?>> outQueue = new LinkedBlockingQueue<>();
		processor = Processor.builder(connector, inQueue, outQueue).build();
		dispatcher = new CommandDispatcher(outQueue, listener);
	}

	@Override
	public void close() {
		processor.close();
		dispatcher.close();
	}

	@Override
	public void command(BaseCommand<?> command) {
		logger.info("Command: {}", command);
		if (!processor.supported(command.type)) throw new UnsupportedOperationException(
			"Function not supported: " + command.type);
		inQueue.add(command);
	}

}
