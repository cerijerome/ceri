package ceri.common.net;

import java.io.IOException;
import ceri.common.io.ReplaceableConnector;

/**
 * A socket pass-through that allows the underlying socket to be replaced. The caller to set() is
 * responsible for close/connect when changing sockets.
 */
public class ReplaceableTcpSocket extends ReplaceableConnector<TcpSocket> implements TcpSocket {

	public static ReplaceableTcpSocket of() {
		return new ReplaceableTcpSocket();
	}

	private ReplaceableTcpSocket() {}

	@SuppressWarnings("resource")
	@Override
	public HostPort hostPort() {
		return runtimeConnector().hostPort();
	}

	@SuppressWarnings("resource")
	@Override
	public int localPort() {
		return runtimeConnector().localPort();
	}

	@SuppressWarnings("resource")
	@Override
	public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
		connector().option(option, value);
	}

	@SuppressWarnings("resource")
	@Override
	public <T> T option(TcpSocketOption<T> option) throws IOException {
		return connector().option(option);
	}

}
