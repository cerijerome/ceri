package ceri.serial.i2c;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class I2cAddressBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		I2cAddress t = I2cAddress.of(0x6e);
		I2cAddress eq0 = I2cAddress.of(0x6e);
		I2cAddress eq1 = I2cAddress.of7Bit(0x6e);
		I2cAddress ne0 = I2cAddress.of(0x6f);
		I2cAddress ne1 = I2cAddress.of7Bit(0x6f);
		I2cAddress ne2 = I2cAddress.of10Bit(0x6e);
		exerciseEquals(t, eq0, eq1);
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
		assertEquals(I2cAddress.fromFrames(bytes(0xdd)), I2cAddress.of7Bit(0x6e));
		assertEquals(I2cAddress.fromFrames(bytes(0xf1, 0x6e)), I2cAddress.of10Bit(0x6e));
		assertThrown(() -> I2cAddress.fromFrames(bytes(0xe1, 0x6e)));
	}

	@Test
	public void shouldDetermineIfSlaveAddress() {
		assertFalse(I2cAddress.of(0x7).isSlave());
		assertTrue(I2cAddress.of(0x8).isSlave());
		assertTrue(I2cAddress.of(0x77).isSlave());
		assertFalse(I2cAddress.of(0x78).isSlave());
		assertTrue(I2cAddress.of10Bit(0x7).isSlave());
		assertTrue(I2cAddress.of10Bit(0x78).isSlave());
	}

}
