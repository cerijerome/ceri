package ceri.common.net;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionFunction;

/**
 * Option constants for Socket. Created due to missing types in StandardSocketOptions. 
 */
public class TcpSocketOption<T> {
	public static final TcpSocketOption<Integer> soTimeout =
		new TcpSocketOption<>("SO_TIMEOUT", Socket::setSoTimeout, Socket::getSoTimeout, 0);
	public static final TcpSocketOption<Integer> soLinger =
		new TcpSocketOption<>("SO_LINGER", TcpSocketOption::setSoLinger, Socket::getSoLinger, -1);
	public static final TcpSocketOption<Boolean> soKeepAlive =
		new TcpSocketOption<>("SO_KEEPALIVE", Socket::setKeepAlive, Socket::getKeepAlive, false);
	public static final TcpSocketOption<Boolean> soReuseAddr = new TcpSocketOption<>("SO_REUSEADDR",
		Socket::setReuseAddress, Socket::getReuseAddress, false);
	public static final TcpSocketOption<Boolean> soOobInline =
		new TcpSocketOption<>("SO_OOBINLINE", Socket::setOOBInline, Socket::getOOBInline, false);
	public static final TcpSocketOption<Boolean> tcpNoDelay =
		new TcpSocketOption<>("TCP_NODELAY", Socket::setTcpNoDelay, Socket::getTcpNoDelay, false);
	public static final TcpSocketOption<Integer> ipTos =
		new TcpSocketOption<>("IP_TOS", Socket::setTrafficClass, Socket::getTrafficClass, 0);
	public static final TcpSocketOption<Integer> soSndBuf = new TcpSocketOption<>("SO_SNDBUF",
		Socket::setSendBufferSize, Socket::getSendBufferSize, null);
	public static final TcpSocketOption<Integer> soRcvBuf = new TcpSocketOption<>("SO_RCVBUF",
		Socket::setReceiveBufferSize, Socket::getReceiveBufferSize, null);
	public static final List<TcpSocketOption<?>> all = List.of(soTimeout, soLinger, soKeepAlive,
		soReuseAddr, soOobInline, tcpNoDelay, ipTos, soSndBuf, soRcvBuf);
	public final String name;
	private final ExceptionBiConsumer<IOException, Socket, T> setFn;
	private final ExceptionFunction<IOException, Socket, T> getFn;
	public final T disableValue;

	private static void setSoLinger(Socket socket, int sec) throws IOException {
		socket.setSoLinger(sec >= 0, sec);
	}

	private TcpSocketOption(String name, ExceptionBiConsumer<IOException, Socket, T> setFn,
		ExceptionFunction<IOException, Socket, T> getFn, T disableValue) {
		this.name = name;
		this.setFn = setFn;
		this.getFn = getFn;
		this.disableValue = disableValue;
	}

	public boolean canDisable() {
		return disableValue != null;
	}

	/**
	 * Disables the option. Does nothing if the option cannot be disabled.
	 */
	public void disable(Socket socket) throws IOException {
		if (canDisable()) set(socket, disableValue);
	}

	public void set(Socket socket, T value) throws IOException {
		setFn.accept(socket, value);
	}

	public T get(Socket socket) throws IOException {
		return getFn.apply(socket);
	}

	@Override
	public String toString() {
		return name;
	}
}
