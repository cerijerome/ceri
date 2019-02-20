package ceri.log.rpc;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RpcServer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int SHUTDOWN_TIMEOUT_MS_DEF = 5000;
	private final int shutdownTimeoutMs;
	private final Server server;

	public static RpcServer of(BindableService service, int port) {
		return of(service, port, SHUTDOWN_TIMEOUT_MS_DEF);
	}

	public static RpcServer of(BindableService service, int port, int shutdownTimeoutMs) {
		return new RpcServer(service, port, shutdownTimeoutMs);
	}

	private RpcServer(BindableService service, int port, int shutdownTimeoutMs) {
		this.shutdownTimeoutMs = shutdownTimeoutMs;
		server = ServerBuilder.forPort(port).addService(service).build();
	}

	public void start() throws IOException {
		server.start();
		logger.info("Server started, listening on " + server.getPort());
	}

	@Override
	public void close() {
		server.shutdown();
		try {
			server.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			if (logger != null) logger.catching(Level.INFO, e);
		}
	}

}
