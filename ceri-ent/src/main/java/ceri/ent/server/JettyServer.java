package ceri.ent.server;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

/**
 * Base wrapper class for managing a jetty server.
 */
public class JettyServer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Server server;

	public JettyServer(Server server) {
		this.server = server;
	}

	public void start() throws IOException {
		try {
			server.start();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public void stop() throws IOException {
		try {
			server.stop();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public void waitForServer() throws InterruptedException {
		server.join();
	}

	@Override
	public void close() throws IOException {
		stop();
		try {
			waitForServer();
		} catch (InterruptedException e) {
			logger.catching(Level.WARN, e);
		}
	}

	
}
