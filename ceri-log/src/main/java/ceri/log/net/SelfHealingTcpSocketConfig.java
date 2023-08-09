package ceri.log.net;

import static ceri.common.function.Namer.lambda;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.Namer;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealingConfig;

public class SelfHealingTcpSocketConfig {
	public static final SelfHealingTcpSocketConfig NULL = new Builder(HostPort.NULL).build();
	private static final Predicate<Exception> DEFAULT_PREDICATE =
		Namer.predicate(TcpSocket::isBroken, "TcpSocket::isBroken");
	public final HostPort hostPort;
	public final ExceptionFunction<IOException, HostPort, TcpSocket> factory;
	public final TcpSocketOptions options;
	public final SelfHealingConfig selfHealing;

	public static SelfHealingTcpSocketConfig of(HostPort hostPort) {
		return builder(hostPort).build();
	}

	public static class Builder {
		final HostPort hostPort;
		ExceptionFunction<IOException, HostPort, TcpSocket> factory = TcpSocket::connect;
		TcpSocketOptions.Mutable options = TcpSocketOptions.of();
		final SelfHealingConfig.Builder selfHealing =
			SelfHealingConfig.builder().brokenPredicate(DEFAULT_PREDICATE);

		Builder(HostPort hostPort) {
			this.hostPort = hostPort;
		}

		public Builder factory(ExceptionFunction<IOException, HostPort, TcpSocket> factory) {
			this.factory = factory;
			return this;
		}

		public <T> Builder option(TcpSocketOption<T> option, T value) {
			options.set(option, value);
			return this;
		}

		public Builder options(TcpSocketOptions options) {
			this.options.set(options);
			return this;
		}

		public Builder selfHealing(SelfHealingConfig selfHealing) {
			this.selfHealing.apply(selfHealing);
			return this;
		}

		public Builder selfHealing(Consumer<SelfHealingConfig.Builder> consumer) {
			consumer.accept(selfHealing);
			return this;
		}

		public SelfHealingTcpSocketConfig build() {
			return new SelfHealingTcpSocketConfig(this);
		}
	}

	public static Builder builder(HostPort hostPort) {
		return new Builder(hostPort);
	}

	public static Builder builder(SelfHealingTcpSocketConfig config) {
		return new Builder(config.hostPort).options(config.options).selfHealing(config.selfHealing);
	}

	SelfHealingTcpSocketConfig(Builder builder) {
		hostPort = builder.hostPort;
		factory = builder.factory;
		options = builder.options.immutable();
		selfHealing = builder.selfHealing.build();
	}

	public TcpSocket openSocket() throws IOException {
		return factory.apply(hostPort);
	}

	public boolean enabled() {
		return !hostPort.isNull();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, hostPort, lambda(factory), options, selfHealing);
	}
}