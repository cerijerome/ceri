package ceri.log.rpc.client;

import static ceri.common.net.NetUtil.LOCALHOST;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.net.NetUtil;
import ceri.common.text.ToString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Wraps a ManagedChannel as a Closeable resource.
 */
public class RpcChannel implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	private static final int SHUTDOWN_TIMEOUT_MS_DEF = 5000;
	private final int shutdownTimeoutMs;
	public final ManagedChannel channel;

	public static class Config {
		public static final Config NULL = builder().build();
		public final String host;
		public final Integer port;

		public static Config localhost(int port) {
			return builder().host(LOCALHOST).port(port).build();
		}

		public static Config of(String host, int port) {
			return builder().host(host).port(port).build();
		}

		public static class Builder {
			String host = null;
			Integer port = null;

			Builder() {}

			public Builder host(String host) {
				this.host = host;
				return this;
			}

			public Builder port(int port) {
				this.port = port;
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
			host = builder.host;
			port = builder.port;
		}

		public boolean enabled() {
			return host != null && port != null;
		}

		public boolean isLocalhost() {
			return NetUtil.isLocalhost(host);
		}

		@Override
		public int hashCode() {
			return Objects.hash(host, port);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Config)) return false;
			Config other = (Config) obj;
			if (!Objects.equals(host, other.host)) return false;
			if (!Objects.equals(port, other.port)) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, host, port);
		}
	}

	public static RpcChannel of(Config config) {
		return plaintext(config.host, config.port);
	}

	public static RpcChannel localhost(int port) {
		return plaintext(LOCALHOST, port);
	}

	public static RpcChannel plaintext(String host, int port) {
		return of(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
	}

	public static RpcChannel of(ManagedChannel channel) {
		return of(channel, SHUTDOWN_TIMEOUT_MS_DEF);
	}

	public static RpcChannel of(ManagedChannel channel, int shutdownTimeoutMs) {
		return new RpcChannel(channel, shutdownTimeoutMs);
	}

	private RpcChannel(ManagedChannel channel, int shutdownTimeoutMs) {
		this.shutdownTimeoutMs = shutdownTimeoutMs;
		this.channel = channel;
		logger.info("Channel for " + channel.authority());
	}

	@Override
	public void close() {
		channel.shutdownNow();
		try {
			channel.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.catching(Level.INFO, e);
		}
		logger.info("Stopped");
	}

	@Override
	public String toString() {
		return ToString.forClass(this, channel.authority());
	}
}