package ceri.log.net;

import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.property.TypedProperties;

public class TcpSocketProperties extends TypedProperties.Ref {
	private static final String HOST_PORT_KEY = "host.port";
	private static final String OPTION_KEY = "option";
	private static final String SO_TIMEOUT_KEY = "so.timeout"; // int
	private static final String SO_LINGER_KEY = "so.linger"; // int
	private static final String SO_KEEPALIVE_KEY = "so.keepalive"; // boolean
	private static final String SO_REUSEADDR_KEY = "so.reuseaddr"; // boolean
	private static final String SO_OOBINLINE_KEY = "so.oobinline"; // boolean
	private static final String TCP_NODELAY_KEY = "tcp.nodelay"; // boolean
	private static final String IP_TOS_KEY = "ip.tos"; // int
	private static final String SO_SNDBUF_KEY = "so.sndbuf"; // int
	private static final String SO_RCVBUF_KEY = "so.rcvbuf"; // int

	public TcpSocketProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public HostPort hostPort() {
		return HostPort.parse(parse(HOST_PORT_KEY).get());
	}

	public TcpSocketOptions options() {
		var options = TcpSocketOptions.of();
		options.set(TcpSocketOption.soTimeout, parse(OPTION_KEY, SO_TIMEOUT_KEY).toInt());
		options.set(TcpSocketOption.soLinger, parse(OPTION_KEY, SO_LINGER_KEY).toInt());
		options.set(TcpSocketOption.soKeepAlive, parse(OPTION_KEY, SO_KEEPALIVE_KEY).toBool());
		options.set(TcpSocketOption.soReuseAddr, parse(OPTION_KEY, SO_REUSEADDR_KEY).toBool());
		options.set(TcpSocketOption.soOobInline, parse(OPTION_KEY, SO_OOBINLINE_KEY).toBool());
		options.set(TcpSocketOption.tcpNoDelay, parse(OPTION_KEY, TCP_NODELAY_KEY).toBool());
		options.set(TcpSocketOption.ipTos, parse(OPTION_KEY, IP_TOS_KEY).toInt());
		options.set(TcpSocketOption.soSndBuf, parse(OPTION_KEY, SO_SNDBUF_KEY).toInt());
		options.set(TcpSocketOption.soRcvBuf, parse(OPTION_KEY, SO_RCVBUF_KEY).toInt());
		return options.immutable();
	}
}
