package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class DeviceModeBehavior {

	@Test
	public void shouldGetFromBoolean() {
		assertEquals(DeviceMode.from(null), DeviceMode.test);
		assertEquals(DeviceMode.from(true), DeviceMode.enabled);
		assertEquals(DeviceMode.from(false), DeviceMode.disabled);
	}

}
