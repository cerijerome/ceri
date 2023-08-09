package ceri.x10.cm11a;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;

public class Cm11aConfigBehavior {

	@Test
	public void shouldDetermineIfTest() {
		assertFalse(Cm11aConfig.of("com").isTest());
		assertTrue(Cm11aConfig.builder().mode(DeviceMode.test).build().isTest());
	}

	@Test
	public void shouldDetermineIfDevice() {
		assertTrue(Cm11aConfig.of("com").isDevice());
		assertFalse(Cm11aConfig.builder().device(Cm11aDeviceConfig.DEFAULT).build().isDevice());
		assertFalse(Cm11aConfig.builder().mode(DeviceMode.test)
			.serial(SelfHealingSerialConfig.of("com")).build().isDevice());
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertMatch(Cm11aConfig.builder().id(777).serial(SelfHealingSerialConfig.of("com"))
			.build().toString(), ".*\\b777\\b.*\\bcom\\b.*");
	}

}
