package ceri.log.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.common.net.HostPort;

public class SelfHealingSocketConfigBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(SelfHealingSocketConfig.NULL.enabled());
		assertTrue(SelfHealingSocketConfig.of("test", 123).enabled());
	}

	@Test
	public void shouldCopySettings() {
		var config0 = SelfHealingSocketConfig.builder("test", 123).build();
		var config = SelfHealingSocketConfig.builder(config0).build();
		assertEquals(config.hostPort, HostPort.of("test", 123));
	}

	@Test
	public void shouldDetermineIfBroken() {
		var config = SelfHealingSocketConfig.builder("test", 123).build();
		assertFalse(config.broken(null));
		assertFalse(config.broken(new RuntimeException()));
		assertFalse(config.broken(new IOException()));
		config = SelfHealingSocketConfig.builder("test", 123)
			.brokenPredicate(IOException.class::isInstance).build();
		assertFalse(config.broken(null));
		assertFalse(config.broken(new RuntimeException()));
		assertTrue(config.broken(new IOException()));
	}

}
