package ceri.log.rpc.client;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;

public class RpcChannelProperties extends BaseProperties {
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";

	public RpcChannelProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcChannelConfig config() {
		RpcChannelConfig.Builder b = RpcChannelConfig.builder();
		safeAccept(host(), b::host);
		safeAccept(port(), b::port);
		return b.build();
	}

	private String host() {
		return value(HOST_KEY);
	}

	private Integer port() {
		return intValue(PORT_KEY);
	}

}
