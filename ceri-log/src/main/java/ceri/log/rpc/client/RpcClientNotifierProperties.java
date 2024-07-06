package ceri.log.rpc.client;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;

public class RpcClientNotifierProperties extends Ref<BaseProperties> {
	private static final String RESET_DELAY_MS_KEY = "reset.delay.ms";

	public RpcClientNotifierProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
	}

	public RpcClientNotifier.Config config() {
		var b = RpcClientNotifier.Config.builder();
		safeAccept(resetDelayMs(), b::resetDelayMs);
		return b.build();
	}

	private Integer resetDelayMs() {
		return ref.intValue(RESET_DELAY_MS_KEY);
	}
}
