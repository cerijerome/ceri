package ceri.x10.cm11a.device;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class Cm11aDeviceConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Cm11aDeviceConfig t = Cm11aDeviceConfig.builder().queueSize(5).build();
		Cm11aDeviceConfig eq0 = Cm11aDeviceConfig.builder().queueSize(5).build();
		Cm11aDeviceConfig ne0 = Cm11aDeviceConfig.builder().queueSize(4).build();
		Cm11aDeviceConfig ne1 = Cm11aDeviceConfig.builder().queueSize(5).maxSendAttempts(9).build();
		Cm11aDeviceConfig ne2 =
			Cm11aDeviceConfig.builder().queueSize(5).queuePollTimeoutMs(1).build();
		Cm11aDeviceConfig ne3 = Cm11aDeviceConfig.builder().queueSize(5).readPollMs(1).build();
		Cm11aDeviceConfig ne4 = Cm11aDeviceConfig.builder().queueSize(5).readTimeoutMs(1).build();
		Cm11aDeviceConfig ne5 = Cm11aDeviceConfig.builder().queueSize(5).errorDelayMs(1).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

}
