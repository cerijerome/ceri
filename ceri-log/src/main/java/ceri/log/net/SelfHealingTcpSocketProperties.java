package ceri.log.net;

import ceri.common.property.BaseProperties;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingTcpSocketProperties extends BaseProperties {
	private final TcpSocketProperties socket;
	private final SelfHealingProperties selfHealing;

	public SelfHealingTcpSocketProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		socket = new TcpSocketProperties(this);
		selfHealing = new SelfHealingProperties(this);
	}

	public SelfHealingTcpSocket.Config config() {
		return SelfHealingTcpSocket.Config.builder(socket.hostPort()).options(socket.options())
			.selfHealing(selfHealing.config()).build();
	}
}
