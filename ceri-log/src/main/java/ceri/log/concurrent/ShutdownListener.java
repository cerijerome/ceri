package ceri.log.concurrent;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.log.util.LogUtil;

/**
 * Listens on a given port to signal a shutdown.
 */
public class ShutdownListener implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	public static final int PORT_DEF = 9999;
	private final BooleanCondition stop = BooleanCondition.create();
	private final SocketListener socket;

	public static ShutdownListener create() throws IOException {
		return create(PORT_DEF);
	}

	public static ShutdownListener create(int port) throws IOException {
		return new ShutdownListener(port);
	}

	private ShutdownListener(int port) throws IOException {
		socket = SocketListener.create(port);
		socket.listeners().listen(data -> stop());
		logger.debug("Listening on port {}", port);
	}

	/**
	 * Call this method to wait for shutdown.
	 */
	public void await() {
		try {
			stop.await();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	public void stop() {
		logger.debug("Stop requested");
		stop.signal();
	}
	
	@Override
	public void close() {
		LogUtil.close(logger, socket);
	}

}
