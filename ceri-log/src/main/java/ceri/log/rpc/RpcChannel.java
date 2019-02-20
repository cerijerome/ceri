package ceri.log.rpc;

import static ceri.common.net.NetUtil.LOCALHOST;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Wraps a ManagedChannel as a Closeable resource.
 */
public class RpcChannel implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int SHUTDOWN_TIMEOUT_MS_DEF = 5000;
	private final int shutdownTimeoutMs;
	public final ManagedChannel channel;

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
	}

	@Override
	public void close() {
		channel.shutdownNow();
		try {
			channel.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			if (logger != null) logger.catching(Level.INFO, e);
		}
	}
}