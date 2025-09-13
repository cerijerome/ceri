package ceri.serial.i2c;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.test.TestUtil;

public class I2cAddressBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = I2cAddress.of(0x6e);
		var eq0 = I2cAddress.of(0x6e);
		var eq1 = I2cAddress.of7Bit(0x6e);
		var ne0 = I2cAddress.of(0x6f);
		var ne1 = I2cAddress.of7Bit(0x6f);
		var ne2 = I2cAddress.of10Bit(0x6e);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldFailToCreateFromInvalidAddress() {
		assertThrown(() -> I2cAddress.of(0x456));
		assertThrown(() -> I2cAddress.of7Bit(0x88));
		assertThrown(() -> I2cAddress.of10Bit(0x401));
	}

	@Test
	public void shouldCreateFrames() {
		assertArray(I2cAddress.of7Bit(0x6e).frames(true), 0xdd);
		assertArray(I2cAddress.of7Bit(0x6e).frames(false), 0xdc);
		assertArray(I2cAddress.of10Bit(0x6e).frames(true), 0xf1, 0x6e);
		assertArray(I2cAddress.of10Bit(0x6e).frames(false), 0xf0, 0x6e);
	}

	@Test
	public void shouldCreateFromFrames() {
		assertEquals(I2cAddress.fromFrames(ArrayUtil.bytes.of(0xdd)), I2cAddress.of7Bit(0x6e));
		assertEquals(I2cAddress.fromFrames(ArrayUtil.bytes.of(0xf1, 0x6e)),
			I2cAddress.of10Bit(0x6e));
		assertThrown(() -> I2cAddress.fromFrames(ArrayUtil.bytes.of(0xe1, 0x6e)));
	}

	@Test
	public void shouldDetermineIfSlaveAddress() {
		assertEquals(I2cAddress.of(0x7).isSlave(), false);
		assertEquals(I2cAddress.of(0x8).isSlave(), true);
		assertEquals(I2cAddress.of(0x77).isSlave(), true);
		assertEquals(I2cAddress.of(0x78).isSlave(), false);
		assertEquals(I2cAddress.of10Bit(0x7).isSlave(), true);
		assertEquals(I2cAddress.of10Bit(0x78).isSlave(), true);
	}
}
