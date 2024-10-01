package ceri.log.rpc.service;

import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;

public class RpcServerProperties extends Ref<TypedProperties> {
	private static final String PORT_KEY = "port";
	private static final String SHUTDOWN_TIMEOUT_MS_KEY = "shutdown.timeout.ms";

	public RpcServerProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
	}

	public RpcServer.Config config() {
		return new RpcServer.Config(port(), shutdownTimeoutMs());
	}

	private Integer port() {
		return ref.intValue(PORT_KEY);
	}

	private int shutdownTimeoutMs() {
		return ref.intValue(RpcServer.Config.DEFAULT.shutdownTimeoutMs(), SHUTDOWN_TIMEOUT_MS_KEY);
	}
}
