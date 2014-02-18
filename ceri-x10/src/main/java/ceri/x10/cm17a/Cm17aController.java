package ceri.x10.cm17a;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandDispatcher;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.CommandListener;
import ceri.x10.command.CommandLogger;

public class Cm17aController implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int MAX_QUEUE_SIZE_DEF = 100;
	private final BlockingQueue<BaseCommand<?>> inQueue;
	private final BlockingQueue<BaseCommand<?>> outQueue;
	private final Processor processor;
	private final CommandDispatcher dispatcher;
	
	public static void main(String[] args) throws IOException {
		try (Cm17aConnector connector = new Cm17aConnector("/dev/cu.usbserial", 5000)) {
			try (Cm17aController controller = new Cm17aController(connector, new CommandLogger())) {
				BasicUtil.delay(5000);
				controller.command(CommandFactory.on("A2"));
				BasicUtil.delay(5000);
				controller.command(CommandFactory.off("A2"));
				BasicUtil.delay(500000);
			}
		}
	}
	public Cm17aController(Cm17aConnector connector, CommandListener listener) throws IOException {
		inQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE_DEF);
		outQueue = new LinkedBlockingQueue<>();
		processor = Processor.builder(connector, inQueue, outQueue).build();
		dispatcher = new CommandDispatcher(outQueue, listener);
	}

	@Override
	public void close() {
		processor.close();
		dispatcher.close();
	}

	public void command(BaseCommand<?> command) {
		logger.info("Command: {}", command);
		inQueue.add(command);
	}

}
