package ceri.common.net;

import java.io.IOException;
import ceri.common.io.ReplaceableConnector;

/**
 * A socket pass-through that allows the underlying socket to be replaced.
 */
public class ReplaceableTcpSocket extends ReplaceableConnector<TcpSocket> implements TcpSocket {

	public static ReplaceableTcpSocket of() {
		return new ReplaceableTcpSocket();
	}

	private ReplaceableTcpSocket() {}

	@Override
	public HostPort hostPort() {
		return applyConnector(TcpSocket::hostPort, HostPort.NULL);
	}

	@Override
	public int localPort() {
		return applyConnector(TcpSocket::localPort, HostPort.INVALID_PORT);
	}

	@Override
	public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
		acceptValidConnector(socket -> socket.option(option, value));
	}

	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		return applyValidConnector(socket -> socket.option(option));
	}

}
