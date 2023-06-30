package ceri.log.net;

import java.util.LinkedHashMap;
import java.util.Map;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;
import ceri.common.property.BaseProperties;
import ceri.common.util.BasicUtil;

public class TcpSocketProperties extends BaseProperties {
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

	public TcpSocketProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public HostPort hostPort() {
		return HostPort.parse(value(HOST_PORT_KEY));
	}

	public Map<TcpSocketOption<?>, Object> options() {
		var map = new LinkedHashMap<TcpSocketOption<?>, Object>();
		add(map, TcpSocketOption.soTimeout, optionSoTimeout());
		add(map, TcpSocketOption.soLinger, optionSoLinger());
		add(map, TcpSocketOption.soKeepAlive, optionSoKeepAlive());
		add(map, TcpSocketOption.soReuseAddr, optionSoReuseAddr());
		add(map, TcpSocketOption.soOobInline, optionSoOobInline());
		add(map, TcpSocketOption.tcpNoDelay, optionTcpNoDelay());
		add(map, TcpSocketOption.ipTos, optionIpTos());
		add(map, TcpSocketOption.soSndBuf, optionSoSndBuf());
		add(map, TcpSocketOption.soRcvBuf, optionSoRcvBuf());
		return map;
	}

	private <T> void add(Map<TcpSocketOption<?>, Object> map, TcpSocketOption<T> option, T value) {
		if (value != null) map.put(BasicUtil.uncheckedCast(option), value);
	}

	private Integer optionSoTimeout() {
		return intValue(OPTION_KEY, SO_TIMEOUT_KEY);
	}

	private Integer optionSoLinger() {
		return intValue(OPTION_KEY, SO_LINGER_KEY);
	}

	private Boolean optionSoKeepAlive() {
		return booleanValue(OPTION_KEY, SO_KEEPALIVE_KEY);
	}

	private Boolean optionSoReuseAddr() {
		return booleanValue(OPTION_KEY, SO_REUSEADDR_KEY);
	}

	private Boolean optionSoOobInline() {
		return booleanValue(OPTION_KEY, SO_OOBINLINE_KEY);
	}

	private Boolean optionTcpNoDelay() {
		return booleanValue(OPTION_KEY, TCP_NODELAY_KEY);
	}

	private Integer optionIpTos() {
		return intValue(OPTION_KEY, IP_TOS_KEY);
	}

	private Integer optionSoSndBuf() {
		return intValue(OPTION_KEY, SO_SNDBUF_KEY);
	}

	private Integer optionSoRcvBuf() {
		return intValue(OPTION_KEY, SO_RCVBUF_KEY);
	}
}
