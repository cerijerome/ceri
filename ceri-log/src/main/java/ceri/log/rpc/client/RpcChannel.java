package ceri.log.rpc.client;

import static ceri.common.net.NetUtil.LOCALHOST;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.Functions;
import ceri.common.net.NetUtil;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Wraps a ManagedChannel as a Closeable resource.
 */
public class RpcChannel implements Functions.Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int SHUTDOWN_TIMEOUT_MS_DEF = 5000;
	private final int shutdownTimeoutMs;
	public final ManagedChannel channel;

	/**
	 * Channel config. Fields can be null.
	 */
	public record Config(String host, Integer port) {
		public static final Config NULL = new Config(null, null);

		public static Config localhost(int port) {
			return new Config(LOCALHOST, port);
		}

		public boolean enabled() {
			return host != null && port != null;
		}

		public boolean isLocalhost() {
			return NetUtil.isLocalhost(host);
		}
	}

	public static class Properties extends TypedProperties.Ref {
		private static final String HOST_KEY = "host";
		private static final String PORT_KEY = "port";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		public Config config() {
			var host = parse(HOST_KEY).get();
			var port = parse(PORT_KEY).toInt();
			return new Config(host, port);
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