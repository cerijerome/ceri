package ceri.log.rpc.client;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;

public class RpcChannelProperties extends Ref<BaseProperties> {
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";

	public RpcChannelProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
	}

	public RpcChannel.Config config() {
		var b = RpcChannel.Config.builder();
		safeAccept(host(), b::host);
		safeAccept(port(), b::port);
		return b.build();
	}

	private String host() {
		return ref.value(HOST_KEY);
	}

	private Integer port() {
		return ref.intValue(PORT_KEY);
	}
}
