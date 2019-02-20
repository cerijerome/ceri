package ceri.log.rpc;

import static ceri.common.net.NetUtil.LOCALHOST;
import ceri.common.property.BaseProperties;

public class RpcClientProperties extends BaseProperties {
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";
	private static final int PORT_DEF = 50051;

	public RpcClientProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcClientConfig config() {
		return RpcClientConfig.of(host(), port());
	}

	private String host() {
		return stringValue(LOCALHOST, HOST_KEY);
	}

	private int port() {
		return intValue(PORT_DEF, PORT_KEY);
	}

}
