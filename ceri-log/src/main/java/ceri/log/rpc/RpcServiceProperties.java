package ceri.log.rpc;

import ceri.common.property.BaseProperties;

public class RpcServiceProperties extends BaseProperties {
	private static final String PORT_KEY = "port";
	private static final int PORT_DEF = 50051;

	public RpcServiceProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcServiceConfig config() {
		return RpcServiceConfig.of(port());
	}

	private int port() {
		return intValue(PORT_DEF, PORT_KEY);
	}

}
