package ceri.serial.i2c;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.test.Assert;
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
		Assert.thrown(() -> I2cAddress.of(0x456));
		Assert.thrown(() -> I2cAddress.of7Bit(0x88));
		Assert.thrown(() -> I2cAddress.of10Bit(0x401));
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
		Assert.thrown(() -> I2cAddress.fromFrames(ArrayUtil.bytes.of(0xe1, 0x6e)));
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
