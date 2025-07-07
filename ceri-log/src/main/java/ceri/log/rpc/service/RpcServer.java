package ceri.log.rpc.service;

import static ceri.log.rpc.service.RpcServiceUtil.NULL_SERVER;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.Excepts.RuntimeCloseable;
import ceri.common.text.ToString;
import ceri.common.util.Enablable;
import ceri.log.rpc.client.RpcChannel;
import ceri.log.util.LogUtil;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RpcServer implements RuntimeCloseable, Enablable {
	private static final Logger logger = LogManager.getLogger();
	public static final RpcServer NULL = new RpcServer(NULL_SERVER, Config.NULL);
	private final Config config;
	private final Server server;

	public record Config(Integer port, int shutdownTimeoutMs) {
		public static final Config DEFAULT = new Config(0, 5000);
		public static final Config NULL = new Config(null, 5000);

		/**
		 * Default settings with given port; use 0 for server-chosen port.
		 */
		public static Config of(int port) {
			return new Config(port, DEFAULT.shutdownTimeoutMs());
		}

		/**
		 * The config is disabled if the port is null.
		 */
		public boolean enabled() {
			return port != null;
		}

		/**
		 * Checks if the channel will connect to this server, based on configuration.
		 */
		public boolean isLoop(RpcChannel.Config channel) {
			if (!enabled() || channel == null || !channel.isLocalhost()) return false;
			return Objects.equals(channel.port(), port);
		}

		/**
		 * Throws an exception if the channel will connect to this server, based on configuration.
		 */
		public Config requireNoLoop(RpcChannel.Config channel) {
			if (!isLoop(channel)) return this;
			throw new IllegalArgumentException("Rpc service and client loop on port " + port);
		}
	}

	@SuppressWarnings("resource")
	public static RpcServer start(BindableService service, Config config) throws IOException {
		return LogUtil.acceptOrClose(of(service, config), RpcServer::start);
	}

	public static RpcServer of(BindableService service, Config config) {
		if (!config.enabled()) return NULL;
		Server server = ServerBuilder.forPort(config.port).addService(service).build();
		return new RpcServer(server, config);
	}

	RpcServer(Server server, Config config) {
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

	@Override
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
