package ceri.serial.i2c;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class DeviceIdBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = DeviceId.of(7, 444, 5);
		var eq0 = DeviceId.of(7, 444, 5);
		var ne0 = DeviceId.of(6, 444, 5);
		var ne1 = DeviceId.of(7, 443, 5);
		var ne2 = DeviceId.of(7, 444, 6);
		TestUtil.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldEncode() {
		Assert.equal(DeviceId.of(0x234, 0x111, 0x1).encode(), 0x234889);
	}

	@Test
	public void shouldEncodeBytes() {
		Assert.array(DeviceId.of(0x234, 0x111, 0x1).encodeBytes(), 0x23, 0x48, 0x89);
	}

	@Test
	public void shouldDetermineCompany() {
		Assert.equal(DeviceId.NONE.company(), DeviceId.Company.unknown);
		Assert.equal(DeviceId.of(0x234, 0x111, 0x1).company(), DeviceId.Company.unknown);
		Assert.equal(DeviceId.of(0x0a, 0x111, 0x1).company(),
			DeviceId.Company.Fujitsu_Semiconductor);
	}

	@Test
	public void shouldDetermineIfNone() {
		Assert.equal(DeviceId.NONE.isNone(), true);
		Assert.equal(DeviceId.of(0, 0, 0).isNone(), true);
		Assert.equal(DeviceId.of(1, 0, 0).isNone(), false);
		Assert.equal(DeviceId.of(0, 1, 0).isNone(), false);
		Assert.equal(DeviceId.of(0, 0, 1).isNone(), false);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.find(DeviceId.NONE.toString(), "\\bnone\\b");
		Assert.find(DeviceId.of(0x234, 0x111, 0x1).toString(), "\\b0x234\\b");
	}
}
