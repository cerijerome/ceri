package ceri.log.rpc.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.text.ToStringHelper;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RpcServer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final RpcServerConfig config;
	private final Server server;

	public static RpcServer of(BindableService service, RpcServerConfig config) {
		return new RpcServer(service, config);
	}

	private RpcServer(BindableService service, RpcServerConfig config) {
		this.config = config;
		server = ServerBuilder.forPort(config.port).addService(service).build();
	}

	public void start() throws IOException {
		server.start();
		logger.info("Listening on port {}", server.getPort());
	}

	@Override
	public void close() {
		server.shutdown();
		try {
			server.awaitTermination(config.shutdownTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			if (logger != null) logger.catching(Level.INFO, e);
		}
		logger.info("Stopped");
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, server.getPort()).toString();
	}
	
}
