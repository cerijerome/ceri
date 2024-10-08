package ceri.log.rpc.client;

import ceri.common.property.TypedProperties;

public class RpcChannelProperties extends TypedProperties.Ref {
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";

	public RpcChannelProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public RpcChannel.Config config() {
		var host = parse(HOST_KEY).get();
		var port = parse(PORT_KEY).toInt();
		return new RpcChannel.Config(host, port);
	}
}
