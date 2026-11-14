package ceri.common.io;

import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;

public class DeviceBehavior {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Device.class);
	}

	@Test
	public void shouldDetermineIfModeIsEnabled() {
		Assert.equal(Device.Mode.enabled(null), false);
		Assert.equal(Device.Mode.enabled(Device.Mode.disabled), false);
		Assert.equal(Device.Mode.enabled(Device.Mode.test), false);
		Assert.equal(Device.Mode.enabled(Device.Mode.enabled), true);
	}

	@Test
	public void shouldDetermineIfModeIsDisabled() {
		Assert.equal(Device.Mode.disabled(null), true);
		Assert.equal(Device.Mode.disabled(Device.Mode.disabled), true);
		Assert.equal(Device.Mode.disabled(Device.Mode.test), false);
		Assert.equal(Device.Mode.disabled(Device.Mode.enabled), false);
	}

	@Test
	public void shouldGetModeFromBoolean() {
		Assert.equal(Device.Mode.from(null), Device.Mode.test);
		Assert.equal(Device.Mode.from(true), Device.Mode.enabled);
		Assert.equal(Device.Mode.from(false), Device.Mode.disabled);
	}

	@Test
	public void shouldConvertBooleanToDevice.Change() {
		Assert.equal(Device.Change.from(null), Device.Change.none);
		Assert.equal(Device.Change.from(true), Device.Change.fixed);
		Assert.equal(Device.Change.from(false), Device.Change.broken);
	}

	@Test
	public void shouldDecodeIntToDevice.Change() {
		Assert.isNull(Device.Change.xcoder.decode(-1));
		Assert.isNull(Device.Change.xcoder.decode(3));
		Assert.equal(Device.Change.xcoder.decode(0), Device.Change.none);
		Assert.equal(Device.Change.xcoder.decode(1), Device.Change.fixed);
		Assert.equal(Device.Change.xcoder.decode(2), Device.Change.broken);
	}
}
