package ceri.log.net;

import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingTcpSocketProperties extends Ref<BaseProperties> {
	private final TcpSocketProperties socket;
	private final SelfHealingProperties selfHealing;

	public SelfHealingTcpSocketProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
		socket = new TcpSocketProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingTcpSocket.Config config() {
		return SelfHealingTcpSocket.Config.builder(socket.hostPort()).options(socket.options())
			.selfHealing(selfHealing.config()).build();
	}
}
