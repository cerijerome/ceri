package ceri.log.io;

import static ceri.common.function.FunctionUtil.lambdaName;
import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Predicate;
import ceri.common.function.ExceptionObjIntFunction;
import ceri.common.net.HostPort;
import ceri.common.text.ToString;

public class SelfHealingSocketConfig {
	public static final SelfHealingSocketConfig NULL = new Builder(HostPort.NULL).build();
	public final ExceptionObjIntFunction<IOException, String, Socket> factory;
	public final HostPort hostPort;
	public final SocketParams params;
	public final int fixRetryDelayMs;
	public final int recoveryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static SelfHealingSocketConfig of(String host, int port) {
		return builder(host, port).build();
	}

	public static SelfHealingSocketConfig of(String host, int port, SocketParams params) {
		return builder(host, port).params(params).build();
	}

	public static class Builder {
		final HostPort hostPort;
		ExceptionObjIntFunction<IOException, String, Socket> factory = Socket::new;
		SocketParams params = SocketParams.DEFAULT;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = e -> false;

		Builder(HostPort hostPort) {
			this.hostPort = hostPort;
		}

		public Builder factory(ExceptionObjIntFunction<IOException, String, Socket> factory) {
			this.factory = factory;
			return this;
		}

		public Builder params(SocketParams params) {
			this.params = params;
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
		return new Builder(HostPort.of(host, port));
	}

	public static Builder builder(SelfHealingSocketConfig config) {
		return new Builder(config.hostPort).params(config.params)
			.fixRetryDelayMs(config.fixRetryDelayMs).recoveryDelayMs(config.recoveryDelayMs)
			.brokenPredicate(config.brokenPredicate);
	}

	SelfHealingSocketConfig(Builder builder) {
		factory = builder.factory;
		hostPort = builder.hostPort;
		params = builder.params;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public Socket openSocket() throws IOException {
		return factory.apply(hostPort.host, hostPort.port);
	}

	public boolean enabled() {
		return !hostPort.isNull();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, hostPort, params, fixRetryDelayMs, recoveryDelayMs,
			lambdaName(brokenPredicate));
	}

}