package ceri.log.rpc.client;

import static ceri.common.net.NetUtil.LOCALHOST;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
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

	public static RpcChannel of(RpcChannelConfig config) {
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