package ceri.x10.cm11a;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.x10.cm11a.Cm11aConfig.Type.cm11aRef;
import static ceri.x10.cm11a.Cm11aConfig.Type.noOp;
import static ceri.x10.cm11a.Cm11aConfig.Type.serial;
import static ceri.x10.cm11a.Cm11aConfig.Type.serialRef;
import static ceri.x10.cm11a.Cm11aConfig.Type.test;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.comm.Serial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.device.Cm11a;

public class Cm11aConfigBehavior {

	@Test
	public void shouldDetermineContainerType() {
		assertEquals(Cm11aConfig.of("com").type(null, null), serial);
		assertEquals(Cm11aConfig.of("com").type(null, Serial.NULL), serialRef);
		assertEquals(Cm11aConfig.of("com").type(Cm11a.NULL, null), cm11aRef);
		assertEquals(Cm11aConfig.builder().mode(DeviceMode.test).build().type(null, null), test);
		assertEquals(Cm11aConfig.builder().mode(DeviceMode.disabled).build().type(null, null),
			noOp);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertMatch(Cm11aConfig.builder().id(777).serial(SelfHealingSerialConfig.of("com")).build()
			.toString(), ".*\\b777\\b.*\\bcom\\b.*");
	}

}
