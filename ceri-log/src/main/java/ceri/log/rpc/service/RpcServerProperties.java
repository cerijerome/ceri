package ceri.log.rpc.service;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;

public class RpcServerProperties extends Ref<TypedProperties> {
	private static final String PORT_KEY = "port";
	private static final String SHUTDOWN_TIMEOUT_MS_KEY = "shutdown.timeout.ms";

	public RpcServerProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
	}

	public RpcServer.Config config() {
		var b = RpcServer.Config.builder();
		safeAccept(port(), b::port);
		safeAccept(shutdownTimeoutMs(), b::shutdownTimeoutMs);
		return b.build();
	}

	private Integer port() {
		return ref.intValue(PORT_KEY);
	}

	private Integer shutdownTimeoutMs() {
		return ref.intValue(SHUTDOWN_TIMEOUT_MS_KEY);
	}
}
