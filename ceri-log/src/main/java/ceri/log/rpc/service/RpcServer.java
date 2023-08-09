package ceri.log.rpc.service;

import static ceri.log.rpc.service.RpcServiceUtil.NULL_SERVER;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RpcServer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	public static final RpcServer NULL = new RpcServer(NULL_SERVER, RpcServerConfig.NULL);
	private final RpcServerConfig config;
	private final Server server;

	@SuppressWarnings("resource")
	public static RpcServer start(BindableService service, RpcServerConfig config)
		throws IOException {
		return LogUtil.acceptOrClose(of(service, config), RpcServer::start);
	}

	public static RpcServer of(BindableService service, RpcServerConfig config) {
		if (!config.enabled()) return NULL;
		Server server = ServerBuilder.forPort(config.port).addService(service).build();
		return new RpcServer(server, config);
	}

	RpcServer(Server server, RpcServerConfig config) {
		this.config = config;
		this.server = server;
	}

	public int port() {
		return server.getPort();
	}

	public void start() throws IOException {
		if (!enabled()) return;
		server.start();
		logger.info("Listening on port {}", server.getPort());
	}

	public boolean enabled() {
		return config.enabled();
	}

	@Override
	public void close() {
		if (!enabled()) return;
		server.shutdownNow();
		try {
			server.awaitTermination(config.shutdownTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.catching(Level.INFO, e);
		}
		logger.info("Stopped");
	}

	@Override
	public String toString() {
		return ToString.forClass(this, server.getPort());
	}
}
