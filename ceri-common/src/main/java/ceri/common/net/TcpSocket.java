package ceri.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.function.Closeables;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.io.Connector;
import ceri.common.reflect.Reflect;

/**
 * A TCP socket connector interface.
 */
public interface TcpSocket extends Connector {
	/** A no-op, stateless, socket instance. */
	TcpSocket NULL = new Null() {};

	@Override
	default String name() {
		return String.format("%d->%s", localPort(), hostPort());
	}

	/**
	 * Provides the socket remote host and port.
	 */
	HostPort hostPort();

	/**
	 * Provides the local port.
	 */
	int localPort();

	/**
	 * Set socket option.
	 */
	<T> void option(Option<T> option, T value) throws IOException;

	/**
	 * Get socket option.
	 */
	<T> T option(Option<T> option) throws IOException;

	/**
	 * Set socket options.
	 */
	default void options(Options options) throws IOException {
		options.applyAll(this);
	}

	/**
	 * Get all known socket options.
	 */
	default Options options() throws IOException {
		return Options.of().add(this).immutable();
	}

	/**
	 * An extension of SocketConnector that is aware of state.
	 */
	interface Fixable extends TcpSocket, Connector.Fixable {}

	/**
	 * Determines if the exception indicates a socket is fatally broken.
	 */
	static boolean isBroken(Exception e) {
		return e instanceof SocketException; // check broken pipe?
	}

	/**
	 * Connect the socket and wrap as a TcpSocket.
	 */
	@SuppressWarnings("resource")
	static Wrapper connect(HostPort hostPort) throws IOException {
		return Closeables.applyOrClose(new Socket(hostPort.host(), hostPort.port()),
			TcpSocket::wrap);
	}

	/**
	 * Wrapper for a jdk socket. The socket must already be connected.
	 */
	static Wrapper wrap(Socket socket) throws IOException {
		return new Wrapper(socket);
	}

	/**
	 * Returns a socket pass-through that allows the underlying socket to be replaced.
	 */
	static Replaceable replaceable() {
		return new Replaceable();
	}

	/**
	 * Socket options.
	 */
	class Option<T> {
		public static final Option<Integer> soTimeout =
			new Option<>("SO_TIMEOUT", Socket::setSoTimeout, Socket::getSoTimeout, 0);
		public static final Option<Integer> soLinger =
			new Option<>("SO_LINGER", Option::setSoLinger, Socket::getSoLinger, -1);
		public static final Option<Boolean> soKeepAlive =
			new Option<>("SO_KEEPALIVE", Socket::setKeepAlive, Socket::getKeepAlive, false);
		public static final Option<Boolean> soReuseAddr =
			new Option<>("SO_REUSEADDR", Socket::setReuseAddress, Socket::getReuseAddress, false);
		public static final Option<Boolean> soOobInline =
			new Option<>("SO_OOBINLINE", Socket::setOOBInline, Socket::getOOBInline, false);
		public static final Option<Boolean> tcpNoDelay =
			new Option<>("TCP_NODELAY", Socket::setTcpNoDelay, Socket::getTcpNoDelay, false);
		public static final Option<Integer> ipTos =
			new Option<>("IP_TOS", Socket::setTrafficClass, Socket::getTrafficClass, 0);
		public static final Option<Integer> soSndBuf =
			new Option<>("SO_SNDBUF", Socket::setSendBufferSize, Socket::getSendBufferSize, null);
		public static final Option<Integer> soRcvBuf = new Option<>("SO_RCVBUF",
			Socket::setReceiveBufferSize, Socket::getReceiveBufferSize, null);
		public static final List<Option<?>> all = List.of(soTimeout, soLinger, soKeepAlive,
			soReuseAddr, soOobInline, tcpNoDelay, ipTos, soSndBuf, soRcvBuf);
		public final String name;
		private final Excepts.BiConsumer<IOException, Socket, T> setFn;
		private final Excepts.Function<IOException, Socket, T> getFn;
		public final T disableValue;

		private static void setSoLinger(Socket socket, int sec) throws IOException {
			socket.setSoLinger(sec >= 0, sec);
		}

		private Option(String name, Excepts.BiConsumer<IOException, Socket, T> setFn,
			Excepts.Function<IOException, Socket, T> getFn, T disableValue) {
			this.name = name;
			this.setFn = setFn;
			this.getFn = getFn;
			this.disableValue = disableValue;
		}

		/**
		 * Determines if the option can be disabled.
		 */
		public boolean canDisable() {
			return disableValue != null;
		}

		/**
		 * Disables the option. Does nothing if the option cannot be disabled, such as SO_RCVBUF.
		 */
		public void disable(Socket socket) throws IOException {
			if (canDisable()) set(socket, disableValue);
		}

		/**
		 * Sets the option on the given socket.
		 */
		public void set(Socket socket, T value) throws IOException {
			setFn.accept(socket, value);
		}

		/**
		 * Gets the option from the given socket.
		 */
		public T get(Socket socket) throws IOException {
			return getFn.apply(socket);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Type-safe map of socket options and values.
	 */
	class Options {
		protected final Map<Option<?>, Object> map;

		/**
		 * Creates a mutable option container.
		 */
		public static Mutable of() {
			return of(Maps::link);
		}

		/**
		 * Creates a mutable option container using the given map constructor.
		 */
		public static Mutable of(Functions.Supplier<Map<Option<?>, Object>> mapSupplier) {
			return new Mutable(mapSupplier.get());
		}

		/**
		 * A mutable options holder.
		 */
		public static class Mutable extends Options {
			private Mutable(Map<Option<?>, Object> map) {
				super(map);
			}

			/**
			 * Copies option values.
			 */
			public Mutable set(Options options) {
				map.putAll(options.map);
				return this;
			}

			/**
			 * Sets the option value.
			 */
			public <T> Mutable set(Option<T> option, T value) {
				Objects.requireNonNull(option);
				Objects.requireNonNull(value);
				map.put(option, value);
				return this;
			}

			/**
			 * Sets options from the socket.
			 */
			public Mutable add(TcpSocket socket) throws IOException {
				for (var option : Option.all) {
					var value = socket.option(option);
					if (value != null) set(option, Reflect.unchecked(value));
				}
				return this;
			}
			
			/**
			 * Returns an immutable copy.
			 */
			public Options immutable() {
				return new Options(Map.copyOf(map));
			}
		}

		private Options(Map<Option<?>, Object> map) {
			this.map = map;
		}

		/**
		 * Provides the set of options that have values.
		 */
		public Set<Option<?>> options() {
			return Immutable.wrap(map.keySet());
		}

		/**
		 * Returns true if the option has been set.
		 */
		public boolean has(Option<?> option) {
			return map.containsKey(option);
		}

		/**
		 * Returns the option value, or null if unset.
		 */
		public <T> T get(Option<T> option) {
			return Reflect.unchecked(map.get(option));
		}

		/**
		 * Applies all options.
		 */
		public void applyAll(TcpSocket socket) throws IOException {
			for (var option : map.keySet())
				apply(option, socket);
		}

		/**
		 * Applies the option only if set.
		 */
		public <T> boolean apply(Option<T> option, TcpSocket socket) throws IOException {
			return apply(option, socket::option);
		}

		/**
		 * Applies the option only if set.
		 */
		public <E extends Exception, T> boolean apply(Option<T> option,
			Excepts.BiConsumer<E, Option<T>, T> consumer) throws E {
			T value = get(option);
			if (value == null) return false;
			consumer.accept(option, value);
			return true;
		}

		@Override
		public int hashCode() {
			return Objects.hash(map);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Options other)) return false;
			return map.equals(other.map);
		}

		@Override
		public String toString() {
			return map.toString();
		}
	}

	/**
	 * Wrapper for a jdk socket. The socket must already be connected.
	 */
	class Wrapper implements TcpSocket {
		private final Socket socket;
		private final HostPort hostPort;
		private final int localPort;
		private final InputStream in;
		private final OutputStream out;

		private Wrapper(Socket socket) throws IOException {
			if (!socket.isConnected()) throw new IOException("Socket is not connected");
			this.socket = socket;
			hostPort = HostPort.from((InetSocketAddress) socket.getRemoteSocketAddress());
			localPort = socket.getLocalPort();
			in = socket.getInputStream();
			out = socket.getOutputStream();
		}

		@Override
		public HostPort hostPort() {
			return hostPort;
		}

		@Override
		public int localPort() {
			return localPort;
		}

		@Override
		public <T> void option(Option<T> option, T value) throws IOException {
			option.set(socket, value);
		}

		@Override
		public <T> T option(Option<T> option) throws IOException {
			return option.get(socket);
		}

		@Override
		public InputStream in() {
			return in;
		}

		@Override
		public OutputStream out() {
			return out;
		}

		@Override
		public void close() throws IOException {
			socket.close();
		}
	}

	/**
	 * A socket pass-through that allows the underlying socket to be replaced.
	 */
	class Replaceable extends ceri.common.io.Replaceable.Con<TcpSocket> implements TcpSocket {
		private Replaceable() {
			super("socket");
		}

		@Override
		public HostPort hostPort() {
			return applyIfSet(TcpSocket::hostPort, HostPort.NULL);
		}

		@Override
		public int localPort() {
			return applyIfSet(TcpSocket::localPort, HostPort.INVALID_PORT);
		}

		@Override
		public <T> void option(Option<T> option, T value) throws IOException {
			acceptValid(socket -> socket.option(option, value));
		}

		@Override
		public <T> T option(Option<T> option) throws IOException {
			return applyValid(socket -> socket.option(option));
		}
	}

	/**
	 * A no-op, stateless, socket implementation.
	 */
	interface Null extends Connector.Null, TcpSocket.Fixable {
		@Override
		default HostPort hostPort() {
			return HostPort.NULL;
		}

		@Override
		default int localPort() {
			return 0;
		}

		@Override
		default <T> void option(Option<T> option, T value) throws IOException {}

		@Override
		default <T> T option(Option<T> option) throws IOException {
			return Objects.requireNonNullElse(option.disableValue, Reflect.unchecked(0));
		}
	}
}
