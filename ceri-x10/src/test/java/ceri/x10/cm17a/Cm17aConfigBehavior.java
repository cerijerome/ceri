package ceri.x10.cm17a;

import static ceri.common.test.TestUtil.assertRegex;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;

public class Cm17aConfigBehavior {

	@Test
	public void shouldDetermineIfTest() {
		assertThat(Cm17aConfig.of("com").isTest(), is(false));
		assertThat(Cm17aConfig.builder().mode(DeviceMode.test).build().isTest(), is(true));
	}

	@Test
	public void shouldDetermineIfDevice() {
		assertThat(Cm17aConfig.of("com").isDevice(), is(true));
		assertThat(Cm17aConfig.builder().device(Cm17aDeviceConfig.DEFAULT).build().isDevice(),
			is(false));
		assertThat(Cm17aConfig.builder().mode(DeviceMode.test)
			.deviceSerial(SelfHealingSerialConfig.of("com")).build().isDevice(), is(false));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertRegex(Cm17aConfig.builder().id(777).deviceSerial(SelfHealingSerialConfig.of("com"))
			.build().toString(), ".*\\b777\\b.*\\bcom\\b.*");
	}

}
