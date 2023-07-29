package ceri.log.net;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.net.HostPort;

public class SelfHealingTcpSocketConfigBehavior {
	private static final HostPort hostPort = HostPort.of("test", 123);
	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(SelfHealingTcpSocketConfig.NULL.enabled());
		assertTrue(SelfHealingTcpSocketConfig.of(hostPort).enabled());
	}

	@Test
	public void shouldCopySettings() {
		var config0 = SelfHealingTcpSocketConfig.builder(hostPort).build();
		var config = SelfHealingTcpSocketConfig.builder(config0).build();
		assertEquals(config.hostPort, hostPort);
	}

	@Test
	public void shouldDetermineIfBroken() {
		var config = SelfHealingTcpSocketConfig.builder(hostPort).build();
		assertFalse(config.selfHealing.broken(null));
		assertFalse(config.selfHealing.broken(new RuntimeException()));
		assertFalse(config.selfHealing.broken(new IOException()));
		config = SelfHealingTcpSocketConfig.builder(hostPort).selfHealing(b -> b
			.brokenPredicate(IOException.class::isInstance)).build();
		assertFalse(config.selfHealing.broken(null));
		assertFalse(config.selfHealing.broken(new RuntimeException()));
		assertTrue(config.selfHealing.broken(new IOException()));
	}

}
