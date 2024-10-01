package ceri.log.rpc.client;

import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;

public class RpcChannelProperties extends Ref<TypedProperties> {
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";

	public RpcChannelProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
	}

	public RpcChannel.Config config() {
		return new RpcChannel.Config(host(), port());
	}

	private String host() {
		return ref.value(HOST_KEY);
	}

	private Integer port() {
		return ref.intValue(PORT_KEY);
	}
}
