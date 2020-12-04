package ceri.serial.i2c;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.serial.i2c.DeviceId.Company;

public class DeviceIdBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		DeviceId t = DeviceId.of(7, 444, 5);
		DeviceId eq0 = DeviceId.of(7, 444, 5);
		DeviceId ne0 = DeviceId.of(6, 444, 5);
		DeviceId ne1 = DeviceId.of(7, 443, 5);
		DeviceId ne2 = DeviceId.of(7, 444, 6);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldEncode() {
		assertEquals(DeviceId.of(0x234, 0x111, 0x1).encode(), 0x234889);
	}

	@Test
	public void shouldEncodeBytes() {
		assertArray(DeviceId.of(0x234, 0x111, 0x1).encodeBytes(), 0x23, 0x48, 0x89);
	}

	@Test
	public void shouldDetermineCompany() {
		assertEquals(DeviceId.NONE.company(), Company.unknown);
		assertEquals(DeviceId.of(0x234, 0x111, 0x1).company(), Company.unknown);
		assertEquals(DeviceId.of(0x0a, 0x111, 0x1).company(), Company.Fujitsu_Semiconductor);
	}

	@Test
	public void shouldDetermineIfNone() {
		assertTrue(DeviceId.NONE.isNone());
		assertTrue(DeviceId.of(0, 0, 0).isNone());
		assertFalse(DeviceId.of(1, 0, 0).isNone());
		assertFalse(DeviceId.of(0, 1, 0).isNone());
		assertFalse(DeviceId.of(0, 0, 1).isNone());
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(DeviceId.NONE.toString(), "\\bnone\\b");
		assertFind(DeviceId.of(0x234, 0x111, 0x1).toString(), "\\b0x234\\b");
	}

}
