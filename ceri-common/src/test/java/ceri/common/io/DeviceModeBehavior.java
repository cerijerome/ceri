package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class DeviceModeBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		assertEquals(DeviceMode.enabled(null), false);
		assertEquals(DeviceMode.enabled(DeviceMode.disabled), false);
		assertEquals(DeviceMode.enabled(DeviceMode.test), false);
		assertEquals(DeviceMode.enabled(DeviceMode.enabled), true);
	}

	@Test
	public void shouldGetFromBoolean() {
		assertEquals(DeviceMode.from(null), DeviceMode.test);
		assertEquals(DeviceMode.from(true), DeviceMode.enabled);
		assertEquals(DeviceMode.from(false), DeviceMode.disabled);
	}

}
