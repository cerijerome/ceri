package ceri.x10.cm11a;

import static ceri.common.test.TestUtil.assertRegex;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;

public class Cm11aConfigBehavior {

	@Test
	public void shouldDetermineIfTest() {
		assertThat(Cm11aConfig.of("com").isTest(), is(false));
		assertThat(Cm11aConfig.builder().mode(DeviceMode.test).build().isTest(), is(true));
	}

	@Test
	public void shouldDetermineIfDevice() {
		assertThat(Cm11aConfig.of("com").isDevice(), is(true));
		assertThat(Cm11aConfig.builder().device(Cm11aDeviceConfig.DEFAULT).build().isDevice(),
			is(false));
		assertThat(Cm11aConfig.builder().mode(DeviceMode.test)
			.deviceSerial(SelfHealingSerialConfig.of("com")).build().isDevice(), is(false));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertRegex(Cm11aConfig.builder().id(777).deviceSerial(SelfHealingSerialConfig.of("com"))
			.build().toString(), ".*\\b777\\b.*\\bcom\\b.*");
	}

}
