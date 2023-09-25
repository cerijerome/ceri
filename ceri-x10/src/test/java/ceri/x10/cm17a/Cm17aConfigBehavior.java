package ceri.x10.cm17a;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.x10.cm17a.Cm17aConfig.Type.cm17aRef;
import static ceri.x10.cm17a.Cm17aConfig.Type.noOp;
import static ceri.x10.cm17a.Cm17aConfig.Type.serial;
import static ceri.x10.cm17a.Cm17aConfig.Type.serialRef;
import static ceri.x10.cm17a.Cm17aConfig.Type.test;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.comm.Serial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17a;

public class Cm17aConfigBehavior {

	@Test
	public void shouldDetermineContainerType() {
		assertEquals(Cm17aConfig.of("com").type(null, null), serial);
		assertEquals(Cm17aConfig.of("com").type(null, Serial.NULL), serialRef);
		assertEquals(Cm17aConfig.of("com").type(Cm17a.NULL, null), cm17aRef);
		assertEquals(Cm17aConfig.builder().mode(DeviceMode.test).build().type(null, null), test);
		assertEquals(Cm17aConfig.builder().mode(DeviceMode.disabled).build().type(null, null),
			noOp);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertMatch(Cm17aConfig.builder().id(777).serial(SelfHealingSerialConfig.of("com")).build()
			.toString(), ".*\\b777\\b.*\\bcom\\b.*");
	}

}
