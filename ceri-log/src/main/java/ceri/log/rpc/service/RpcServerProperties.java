package ceri.log.rpc.service;

import ceri.common.property.TypedProperties;

public class RpcServerProperties extends TypedProperties.Ref {
	private static final String PORT_KEY = "port";
	private static final String SHUTDOWN_TIMEOUT_MS_KEY = "shutdown.timeout.ms";

	public RpcServerProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcServer.Config config() {
		var port = parse(PORT_KEY).toInt();
		var shutdownTimeoutMs =
			parse(SHUTDOWN_TIMEOUT_MS_KEY).toInt(RpcServer.Config.DEFAULT.shutdownTimeoutMs());
		return new RpcServer.Config(port, shutdownTimeoutMs);
	}
}
