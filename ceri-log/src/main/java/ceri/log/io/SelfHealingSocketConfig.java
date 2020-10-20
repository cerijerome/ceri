package ceri.log.io;

import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Predicate;
import ceri.common.function.ExceptionObjIntFunction;
import ceri.common.text.ToString;

public class SelfHealingSocketConfig {
	public static final SelfHealingSocketConfig NULL = builder(null, 0).build();
	public final ExceptionObjIntFunction<IOException, String, Socket> factory;
	public final String host;
	public final int port;
	public final SocketParams params;
	public final int connectionTimeoutMs;
	public final int fixRetryDelayMs;
	public final int recoveryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static SelfHealingSocketConfig of(String host, int port) {
		return new Builder(host, port).build();
	}

	public static SelfHealingSocketConfig of(String host, int port, SocketParams params) {
		return new Builder(host, port).params(params).build();
	}

	public static class Builder {
		final String host;
		final int port;
		ExceptionObjIntFunction<IOException, String, Socket> factory = Socket::new;
		SocketParams params = SocketParams.DEFAULT;
		int connectionTimeoutMs = 3000;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = e -> false;

		Builder(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public Builder factory(ExceptionObjIntFunction<IOException, String, Socket> factory) {
			this.factory = factory;
			return this;
		}

		public Builder params(SocketParams params) {
			this.params = params;
			return this;
		}

		public Builder connectionTimeoutMs(int connectionTimeoutMs) {
			this.connectionTimeoutMs = connectionTimeoutMs;
			return this;
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate) {
			this.brokenPredicate = brokenPredicate;
			return this;
		}

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate, String name) {
			return brokenPredicate(named(brokenPredicate, name));
		}

		public SelfHealingSocketConfig build() {
			return new SelfHealingSocketConfig(this);
		}
	}

	public static Builder builder(String host, int port) {
		return new Builder(host, port);
	}

	public static Builder builder(SelfHealingSocketConfig config) {
		return new Builder(config.host, config.port).params(config.params)
			.connectionTimeoutMs(config.connectionTimeoutMs).fixRetryDelayMs(config.fixRetryDelayMs)
			.recoveryDelayMs(config.recoveryDelayMs).brokenPredicate(config.brokenPredicate);
	}

	SelfHealingSocketConfig(Builder builder) {
		factory = builder.factory;
		host = builder.host;
		port = builder.port;
		params = builder.params;
		connectionTimeoutMs = builder.connectionTimeoutMs;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public Socket openSocket() throws IOException {
		return factory.apply(host, port);
	}

	public boolean enabled() {
		return host != null;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, host, port, params, connectionTimeoutMs, fixRetryDelayMs,
			recoveryDelayMs, brokenPredicate);
	}

}