package ceri.log.io;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.ExceptionFunction;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.text.ToString;
import ceri.common.util.BasicUtil;

public class SelfHealingTcpSocketConfig {
	public static final SelfHealingTcpSocketConfig NULL = new Builder(HostPort.NULL).build();
	public final ExceptionFunction<IOException, HostPort, TcpSocket> factory;
	public final HostPort hostPort;
	public final Map<TcpSocketOption<Object>, Object> options;
	public final SelfHealingConnectorConfig selfHealing;

	public static SelfHealingTcpSocketConfig of(String host, int port) {
		return builder(host, port).build();
	}

	public static class Builder {
		final HostPort hostPort;
		ExceptionFunction<IOException, HostPort, TcpSocket> factory = TcpSocket::connect;
		Map<TcpSocketOption<Object>, Object> options = new LinkedHashMap<>();
		SelfHealingConnectorConfig.Builder selfHealing = SelfHealingConnectorConfig.builder();

		Builder(HostPort hostPort) {
			this.hostPort = hostPort;
		}

		public Builder factory(ExceptionFunction<IOException, HostPort, TcpSocket> factory) {
			this.factory = factory;
			return this;
		}

		public <T> Builder option(TcpSocketOption<T> option, T value) {
			options.put(BasicUtil.uncheckedCast(option), value);
			return this;
		}

		private Builder options(Map<TcpSocketOption<Object>, Object> options) {
			this.options.putAll(options);
			return this;
		}

		public Builder selfHealing(Consumer<SelfHealingConnectorConfig.Builder> consumer) {
			consumer.accept(selfHealing);
			return this;
		}

		public SelfHealingTcpSocketConfig build() {
			return new SelfHealingTcpSocketConfig(this);
		}
	}

	public static Builder builder(String host, int port) {
		return new Builder(HostPort.of(host, port));
	}

	public static Builder builder(SelfHealingTcpSocketConfig config) {
		return new Builder(config.hostPort).options(config.options)
			.selfHealing(b -> b.apply(config.selfHealing));
	}

	SelfHealingTcpSocketConfig(Builder builder) {
		factory = builder.factory;
		hostPort = builder.hostPort;
		options = ImmutableUtil.copyAsMap(builder.options);
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
		return ToString.forClass(this, hostPort, options, selfHealing);
	}

}