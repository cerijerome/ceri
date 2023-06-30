package ceri.log.net;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;
import ceri.common.property.BaseProperties;
import ceri.log.io.SelfHealingConnectorProperties;

public class SelfHealingTcpSocketProperties extends BaseProperties {
	private static final String HOST_PORT_KEY = "host.port";
	private static final String OPTION_KEY = "option";
	private static final String SO_TIMEOUT_KEY = "so.timeout"; // int
	private static final String SO_LINGER_KEY = "so.linger"; // int
	private static final String SO_KEEPALIVE_KEY = "so.keepalive"; // bool
	private static final String SO_REUSEADDR_KEY = "so.reuseaddr"; // bool
	private static final String SO_OOBINLINE_KEY = "so.oobinline"; // bool
	private static final String TCP_NODELAY_KEY = "tcp.nodelay"; // bool
	private static final String IP_TOS_KEY = "ip.tos"; // int
	private static final String SO_SNDBUF_KEY = "so.sndbuf"; // int
	private static final String SO_RCVBUF_KEY = "so.rcvbuf"; // int
	private final SelfHealingConnectorProperties selfHealing;

	public SelfHealingTcpSocketProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		selfHealing = new SelfHealingConnectorProperties(this);
	}

	public SelfHealingTcpSocketConfig config() {
		SelfHealingTcpSocketConfig.Builder b = SelfHealingTcpSocketConfig.builder(hostPort());
		safeAccept(optionSoTimeout(), v -> b.option(TcpSocketOption.soTimeout, v));
		safeAccept(optionSoLinger(), v -> b.option(TcpSocketOption.soLinger, v));
		safeAccept(optionSoKeepAlive(), v -> b.option(TcpSocketOption.soKeepAlive, v));
		safeAccept(optionSoReuseAddr(), v -> b.option(TcpSocketOption.soReuseAddr, v));
		safeAccept(optionSoOobInline(), v -> b.option(TcpSocketOption.soOobInline, v));
		safeAccept(optionTcpNoDelay(), v -> b.option(TcpSocketOption.tcpNoDelay, v));
		safeAccept(optionIpTos(), v -> b.option(TcpSocketOption.ipTos, v));
		safeAccept(optionSoSndBuf(), v -> b.option(TcpSocketOption.soSndBuf, v));
		safeAccept(optionSoRcvBuf(), v -> b.option(TcpSocketOption.soRcvBuf, v));
		b.selfHealing(selfHealing.config());
		return b.build();
	}

	private HostPort hostPort() {
		return HostPort.parse(value(HOST_PORT_KEY));
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
