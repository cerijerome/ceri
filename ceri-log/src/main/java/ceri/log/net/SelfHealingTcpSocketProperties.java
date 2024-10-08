package ceri.log.net;

import ceri.common.property.TypedProperties;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingTcpSocketProperties extends TypedProperties.Ref {
	private final TcpSocketProperties socket;
	private final SelfHealingProperties selfHealing;

	public SelfHealingTcpSocketProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
		socket = new TcpSocketProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingTcpSocket.Config config() {
		return SelfHealingTcpSocket.Config.builder(socket.hostPort()).options(socket.options())
			.selfHealing(selfHealing.config()).build();
	}
}
