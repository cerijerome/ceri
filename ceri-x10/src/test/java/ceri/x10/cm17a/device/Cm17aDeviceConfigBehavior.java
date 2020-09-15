package ceri.x10.cm17a.device;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class Cm17aDeviceConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Cm17aDeviceConfig t = Cm17aDeviceConfig.builder().queueSize(5).build();
		Cm17aDeviceConfig eq0 = Cm17aDeviceConfig.builder().queueSize(5).build();
		Cm17aDeviceConfig ne0 = Cm17aDeviceConfig.builder().queueSize(6).build();
		Cm17aDeviceConfig ne1 =
			Cm17aDeviceConfig.builder().queueSize(5).commandIntervalMicros(11).build();
		Cm17aDeviceConfig ne2 = Cm17aDeviceConfig.builder().queueSize(5).errorDelayMs(22).build();
		Cm17aDeviceConfig ne3 =
			Cm17aDeviceConfig.builder().queueSize(5).queuePollTimeoutMs(33).build();
		Cm17aDeviceConfig ne4 =
			Cm17aDeviceConfig.builder().queueSize(5).resetIntervalMicros(44).build();
		Cm17aDeviceConfig ne5 =
			Cm17aDeviceConfig.builder().queueSize(5).waitIntervalMicros(55).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

}
