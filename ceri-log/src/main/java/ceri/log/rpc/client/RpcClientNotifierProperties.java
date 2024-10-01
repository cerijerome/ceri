package ceri.log.rpc.client;

import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;

public class RpcClientNotifierProperties extends Ref<TypedProperties> {
	private static final String RESET_DELAY_MS_KEY = "reset.delay.ms";

	public RpcClientNotifierProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
	}

	public RpcClientNotifier.Config config() {
		return new RpcClientNotifier.Config(resetDelayMs());
	}

	private int resetDelayMs() {
		return ref.intValue(RpcClientNotifier.Config.DEFAULT.resetDelayMs(), RESET_DELAY_MS_KEY);
	}
}
