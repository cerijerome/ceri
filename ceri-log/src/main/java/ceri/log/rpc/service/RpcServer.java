package ceri.log.rpc.service;

import static ceri.log.rpc.service.RpcServiceUtil.NULL_SERVER;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.text.ToString;
import ceri.log.rpc.client.RpcChannel;
import ceri.log.util.LogUtil;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RpcServer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	public static final RpcServer NULL = new RpcServer(NULL_SERVER, Config.NULL);
	private final Config config;
	private final Server server;

	public static class Config {
		public static final Config NULL = builder().build();
		public static final Config DEFAULT = builder().port(0).build();
		public final Integer port;
		public final int shutdownTimeoutMs;

		/**
		 * Default settings with given port; use 0 for server-chosen port.
		 */
		public static Config of(int port) {
			return builder().port(port).build();
		}

		public static class Builder {
			Integer port = null;
			int shutdownTimeoutMs = 5000;

			Builder() {}

			public Builder port(int port) {
				this.port = port;
				return this;
			}

			public Builder shutdownTimeoutMs(int shutdownTimeoutMs) {
				this.shutdownTimeoutMs = shutdownTimeoutMs;
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		Config(Builder builder) {
			port = builder.port;
			shutdownTimeoutMs = builder.shutdownTimeoutMs;
		}

		public boolean enabled() {
			return port != null;
		}

		/**
		 * Checks if the channel will connect to this server, based on configuration.
		 */
		public boolean isLoop(RpcChannel.Config channel) {
			if (!enabled() || channel == null || !channel.isLocalhost()) return false;
			return Objects.equals(channel.port, port);
		}

		/**
		 * Throws an exception if the channel will connect to this server, based on configuration.
		 */
		public Config requireNoLoop(RpcChannel.Config channel) {
			if (!isLoop(channel)) return this;
			throw new IllegalArgumentException("Rpc service and client loop on port " + port);
		}

		@Override
		public int hashCode() {
			return Objects.hash(port, shutdownTimeoutMs);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Config)) return false;
			Config other = (Config) obj;
			if (!Objects.equals(port, other.port)) return false;
			if (shutdownTimeoutMs != other.shutdownTimeoutMs) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, port, shutdownTimeoutMs);
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
