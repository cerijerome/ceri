package ceri.serial.javax.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.baseProperties;
import java.io.IOException;
import org.junit.Test;
import ceri.serial.javax.DataBits;
import ceri.serial.javax.Parity;
import ceri.serial.javax.SerialPortParams;
import ceri.serial.javax.StopBits;

public class SelfHealingSerialConfigBehavior {

	@Test
	public void shouldReplaceSerialParams() throws IOException {
		assertNull(SelfHealingSerialConfig.replace(null, SerialPortParams.of(123)));
		var s0 = SerialPortParams.of(123);
		var s1 = SerialPortParams.of(456);
		var config = SelfHealingSerialConfig.of("com0", s0);
		assertSame(SelfHealingSerialConfig.replace(config, null), config);
		assertSame(config.replace(null), config);
		assertSame(config.replace(s0), config);
		assertEquals(config.replace(s1).params, s1);
		assertEquals(config.replace(s1).commPortSupplier.get(), "com0");
	}

	@Test
	public void shouldDeterminefEnabled() {
		assertFalse(SelfHealingSerialConfig.NULL.enabled());
		assertTrue(SelfHealingSerialConfig.of("com0").enabled());
	}

	@Test
	public void shouldCreateFromPropertie() throws IOException {
		var config = new SelfHealingSerialProperties(baseProperties("serial"), "serial0").config();
		assertEquals(config.commPortSupplier.get(), "com0");
		assertEquals(config.params,
			SerialPortParams.of(19200, DataBits._8, StopBits._1, Parity.even));
		assertEquals(config.connectionTimeoutMs, 123);
		assertEquals(config.fixRetryDelayMs, 456);
		assertEquals(config.recoveryDelayMs, 789);
	}

}
