package ceri.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import ceri.common.function.Closeables;
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
	<T> void option(TcpSocketOption<T> option, T value) throws IOException;

	/**
	 * Get socket option.
	 */
	<T> T option(TcpSocketOption<T> option) throws IOException;

	/**
	 * Set socket options.
	 */
	default void options(TcpSocketOptions options) throws IOException {
		options.applyAll(this);
	}

	/**
	 * Get socket options.
	 */
	default TcpSocketOptions options() throws IOException {
		return TcpSocketOptions.from(this);
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
		return Closeables.applyOrClose(new Socket(hostPort.host, hostPort.port),
			TcpSocket::wrap);
	}

	/**
	 * Wrapper for a jdk socket. The socket must already be connected.
	 */
	static Wrapper wrap(Socket socket) throws IOException {
		return new Wrapper(socket);
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
		public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
			option.set(socket, value);
		}

		@Override
		public <T> T option(TcpSocketOption<T> option) throws IOException {
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
		default <T> void option(TcpSocketOption<T> option, T value) throws IOException {}

		@Override
		default <T> T option(TcpSocketOption<T> option) throws IOException {
			return Objects.requireNonNullElse(option.disableValue, Reflect.unchecked(0));
		}
	}
}
