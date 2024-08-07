package ceri.log.net;

import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;

public class TcpSocketProperties extends Ref<TypedProperties> {
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
		super(TypedProperties.from(properties, groups));
	}

	public HostPort hostPort() {
		return HostPort.parse(ref.value(HOST_PORT_KEY));
	}

	public TcpSocketOptions options() {
		var options = TcpSocketOptions.of();
		options.set(TcpSocketOption.soTimeout, optionSoTimeout());
		options.set(TcpSocketOption.soLinger, optionSoLinger());
		options.set(TcpSocketOption.soKeepAlive, optionSoKeepAlive());
		options.set(TcpSocketOption.soReuseAddr, optionSoReuseAddr());
		options.set(TcpSocketOption.soOobInline, optionSoOobInline());
		options.set(TcpSocketOption.tcpNoDelay, optionTcpNoDelay());
		options.set(TcpSocketOption.ipTos, optionIpTos());
		options.set(TcpSocketOption.soSndBuf, optionSoSndBuf());
		options.set(TcpSocketOption.soRcvBuf, optionSoRcvBuf());
		return options.immutable();
	}

	private Integer optionSoTimeout() {
		return ref.intValue(OPTION_KEY, SO_TIMEOUT_KEY);
	}

	private Integer optionSoLinger() {
		return ref.intValue(OPTION_KEY, SO_LINGER_KEY);
	}

	private Boolean optionSoKeepAlive() {
		return ref.booleanValue(OPTION_KEY, SO_KEEPALIVE_KEY);
	}

	private Boolean optionSoReuseAddr() {
		return ref.booleanValue(OPTION_KEY, SO_REUSEADDR_KEY);
	}

	private Boolean optionSoOobInline() {
		return ref.booleanValue(OPTION_KEY, SO_OOBINLINE_KEY);
	}

	private Boolean optionTcpNoDelay() {
		return ref.booleanValue(OPTION_KEY, TCP_NODELAY_KEY);
	}

	private Integer optionIpTos() {
		return ref.intValue(OPTION_KEY, IP_TOS_KEY);
	}

	private Integer optionSoSndBuf() {
		return ref.intValue(OPTION_KEY, SO_SNDBUF_KEY);
	}

	private Integer optionSoRcvBuf() {
		return ref.intValue(OPTION_KEY, SO_RCVBUF_KEY);
	}
}
