package ceri.log.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.baseProperties;
import org.junit.Test;

public class SelfHealingConfigBehavior {

	@Test
	public void shouldCopyConfigExceptForDefaultBrokenPredicate() {
		var conf = SelfHealingConfig.builder().fixRetryDelayMs(1).recoveryDelayMs(3)
			.brokenPredicate(e -> true).apply(SelfHealingConfig.DEFAULT).build();
		assertEquals(conf.fixRetryDelayMs, SelfHealingConfig.DEFAULT.fixRetryDelayMs);
		assertEquals(conf.recoveryDelayMs, SelfHealingConfig.DEFAULT.recoveryDelayMs);
		assertTrue(conf.hasBrokenPredicate());
		assertTrue(conf.broken(null));
	}

	@Test
	public void shouldCreateFromProperties() {
		SelfHealingConfig config =
			new SelfHealingProperties(baseProperties("self-healing"), "device").config();
		assertEquals(config.fixRetryDelayMs, 123);
		assertEquals(config.recoveryDelayMs, 456);
	}

}
