package ceri.log.net;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Lambdas;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocket;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealing;
import ceri.log.io.SelfHealingConnector;
import ceri.log.util.LogUtil;

/**
 * A self-healing TCP socket connector. It will automatically reconnect if the connection is broken.
 */
public class SelfHealingTcpSocket extends SelfHealingConnector<TcpSocket>
	implements TcpSocket.Fixable {
	private final Config config;
	private final TcpSocketOptions.Mutable options = TcpSocketOptions.of(ConcurrentHashMap::new);

	public static class Config {
		public static final Config NULL = new Builder(HostPort.NULL).build();
		private static final Predicate<Exception> DEFAULT_PREDICATE =
			Lambdas.register(TcpSocket::isBroken, "TcpSocket::isBroken");
		public final HostPort hostPort;
		public final Function<IOException, HostPort, TcpSocket> factory;
		public final TcpSocketOptions options;
		public final SelfHealing.Config selfHealing;

		public static Config of(HostPort hostPort) {
			return builder(hostPort).build();
		}

		public static class Builder {
			final HostPort hostPort;
			Function<IOException, HostPort, TcpSocket> factory = TcpSocket::connect;
			TcpSocketOptions.Mutable options = TcpSocketOptions.of();
			final SelfHealing.Config.Builder selfHealing =
				SelfHealing.Config.builder().brokenPredicate(DEFAULT_PREDICATE);

			Builder(HostPort hostPort) {
				this.hostPort = hostPort;
			}

			public Builder factory(Function<IOException, HostPort, TcpSocket> factory) {
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

			public Builder selfHealing(SelfHealing.Config selfHealing) {
				this.selfHealing.apply(selfHealing);
				return this;
			}

			public Builder selfHealing(Consumer<SelfHealing.Config.Builder> consumer) {
				consumer.accept(selfHealing);
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder(HostPort hostPort) {
			return new Builder(hostPort);
		}

		public static Builder builder(Config config) {
			return new Builder(config.hostPort).options(config.options)
				.selfHealing(config.selfHealing);
		}

		Config(Builder builder) {
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
			return ToString.forClass(this, hostPort, Lambdas.name(factory), options, selfHealing);
		}
	}

	public static class Properties extends TypedProperties.Ref {
		private final TcpSocketProperties socket;
		private final SelfHealing.Properties selfHealing;

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
			socket = new TcpSocketProperties(ref);
			selfHealing = new SelfHealing.Properties(ref);
		}

		public Config config() {
			return Config.builder(socket.hostPort()).options(socket.options())
				.selfHealing(selfHealing.config()).build();
		}
	}

	public static SelfHealingTcpSocket of(Config config) {
		return new SelfHealingTcpSocket(config);
	}

	private SelfHealingTcpSocket(Config config) {
		super(config.selfHealing);
		this.config = config;
		options.set(config.options);
	}

	@Override
	public HostPort hostPort() {
		return config.hostPort;
	}

	@Override
	public int localPort() {
		return device.applyIfSet(TcpSocket::localPort, HostPort.INVALID_PORT);
	}

	@Override
	public void options(TcpSocketOptions options) throws IOException {
		this.options.set(options);
		TcpSocket.Fixable.super.options(options);
	}

	@Override
	public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
		options.set(option, value);
		device.acceptIfSet(socket -> socket.option(option, value));
	}

	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		return device.applyIfSet(socket -> socket.option(option), null);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, config.hostPort, localPort(), options, config.selfHealing);
	}

	@Override
	protected TcpSocket openConnector() throws IOException {
		TcpSocket socket = null;
		try {
			socket = config.openSocket();
			options.applyAll(socket);
			return socket;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(socket);
			throw e;
		}
	}
}
