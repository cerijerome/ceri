package ceri.common.io;

import org.junit.Test;
import ceri.common.test.Assert;

public class DeviceModeBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		Assert.equal(DeviceMode.enabled(null), false);
		Assert.equal(DeviceMode.enabled(DeviceMode.disabled), false);
		Assert.equal(DeviceMode.enabled(DeviceMode.test), false);
		Assert.equal(DeviceMode.enabled(DeviceMode.enabled), true);
	}

	@Test
	public void shouldDetermineIfDisabled() {
		Assert.equal(DeviceMode.disabled(null), true);
		Assert.equal(DeviceMode.disabled(DeviceMode.disabled), true);
		Assert.equal(DeviceMode.disabled(DeviceMode.test), false);
		Assert.equal(DeviceMode.disabled(DeviceMode.enabled), false);
	}

	@Test
	public void shouldGetFromBoolean() {
		Assert.equal(DeviceMode.from(null), DeviceMode.test);
		Assert.equal(DeviceMode.from(true), DeviceMode.enabled);
		Assert.equal(DeviceMode.from(false), DeviceMode.disabled);
	}

}
