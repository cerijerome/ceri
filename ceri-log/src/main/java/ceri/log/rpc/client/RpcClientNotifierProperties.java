package ceri.log.rpc.client;

import ceri.common.property.TypedProperties;

public class RpcClientNotifierProperties extends TypedProperties.Ref {
	private static final String RESET_DELAY_MS_KEY = "reset.delay.ms";

	public RpcClientNotifierProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcClientNotifier.Config config() {
		return parse(RESET_DELAY_MS_KEY).asInt().as(RpcClientNotifier.Config::new)
			.get(RpcClientNotifier.Config.DEFAULT);
	}
}
