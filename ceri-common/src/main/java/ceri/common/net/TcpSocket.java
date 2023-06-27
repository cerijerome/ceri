package ceri.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import ceri.common.io.Connector;
import ceri.common.util.BasicUtil;

/**
 * A TCP socket connector interface.
 */
public interface TcpSocket extends Connector {
	/** A no-op, stateless, socket instance. */
	Null NULL = new Null();

	@Override
	default String name() {
		return String.format("localhost:%d->%s", localPort(), hostPort());
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
	 * An extension of SocketConnector that is aware of state.
	 */
	interface Fixable extends TcpSocket, Connector.Fixable {}

	static TcpSocket connect(HostPort hostPort) throws IOException {
		@SuppressWarnings("resource")
		Socket socket = new Socket(hostPort.host, hostPort.port);
		try {
			return wrap(socket);
		} catch (RuntimeException | IOException e) {
			socket.close();
			throw e;
		}
	}

	/**
	 * Wrapper for a jdk socket. The socket must already be connected.
	 */
	static TcpSocket wrap(Socket socket) throws IOException {
		return new Wrapper(socket);
	}

	/**
	 * Wrapper for a jdk socket. The socket must already be connected.
	 */
	static class Wrapper implements TcpSocket {
		private final Socket socket;
		private final HostPort hostPort;
		private final int localPort;
		private final InputStream in;
		private final OutputStream out;

		protected Wrapper(Socket socket) throws IOException {
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
	class Null extends Connector.Null implements TcpSocket.Fixable {
		@Override
		public HostPort hostPort() {
			return HostPort.NULL;
		}

		@Override
		public int localPort() {
			return 0;
		}

		@Override
		public <T> void option(TcpSocketOption<T> option, T value) throws IOException {}

		@Override
		public <T> T option(TcpSocketOption<T> option) throws IOException {
			return Objects.requireNonNullElse(option.disableValue, BasicUtil.uncheckedCast(0));
		}
	}

}
