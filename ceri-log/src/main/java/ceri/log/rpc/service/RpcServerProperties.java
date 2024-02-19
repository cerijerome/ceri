package ceri.log.rpc.service;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;

public class RpcServerProperties extends BaseProperties {
	private static final String PORT_KEY = "port";
	private static final String SHUTDOWN_TIMEOUT_MS_KEY = "shutdown.timeout.ms";

	public RpcServerProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcServer.Config config() {
		var b = RpcServer.Config.builder();
		safeAccept(port(), b::port);
		safeAccept(shutdownTimeoutMs(), b::shutdownTimeoutMs);
		return b.build();
	}

	private Integer port() {
		return intValue(PORT_KEY);
	}

	private Integer shutdownTimeoutMs() {
		return intValue(SHUTDOWN_TIMEOUT_MS_KEY);
	}

}
