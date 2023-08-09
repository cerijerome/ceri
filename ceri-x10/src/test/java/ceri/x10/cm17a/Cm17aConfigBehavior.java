package ceri.x10.cm17a;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;

public class Cm17aConfigBehavior {

	@Test
	public void shouldDetermineIfTest() {
		assertFalse(Cm17aConfig.of("com").isTest());
		assertTrue(Cm17aConfig.builder().mode(DeviceMode.test).build().isTest());
	}

	@Test
	public void shouldDetermineIfDevice() {
		assertTrue(Cm17aConfig.of("com").isDevice());
		assertFalse(Cm17aConfig.builder().device(Cm17aDeviceConfig.DEFAULT).build().isDevice());
		assertFalse(Cm17aConfig.builder().mode(DeviceMode.test)
			.serial(SelfHealingSerialConfig.of("com")).build().isDevice());
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertMatch(Cm17aConfig.builder().id(777).serial(SelfHealingSerialConfig.of("com")).build()
			.toString(), ".*\\b777\\b.*\\bcom\\b.*");
	}

}
