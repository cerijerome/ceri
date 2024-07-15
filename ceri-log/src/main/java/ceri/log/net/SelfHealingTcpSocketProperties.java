package ceri.log.net;

import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingTcpSocketProperties extends Ref<TypedProperties> {
	private final TcpSocketProperties socket;
	private final SelfHealingProperties selfHealing;

	public SelfHealingTcpSocketProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
		socket = new TcpSocketProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingTcpSocket.Config config() {
		return SelfHealingTcpSocket.Config.builder(socket.hostPort()).options(socket.options())
			.selfHealing(selfHealing.config()).build();
	}
}
