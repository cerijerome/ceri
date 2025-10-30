package ceri.serial.i2c;

import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class I2cAddressBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = I2cAddress.of(0x6e);
		var eq0 = I2cAddress.of(0x6e);
		var eq1 = I2cAddress.of7Bit(0x6e);
		var ne0 = I2cAddress.of(0x6f);
		var ne1 = I2cAddress.of7Bit(0x6f);
		var ne2 = I2cAddress.of10Bit(0x6e);
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldFailToCreateFromInvalidAddress() {
		Assert.thrown(() -> I2cAddress.of(0x456));
		Assert.thrown(() -> I2cAddress.of7Bit(0x88));
		Assert.thrown(() -> I2cAddress.of10Bit(0x401));
	}

	@Test
	public void shouldCreateFrames() {
		Assert.array(I2cAddress.of7Bit(0x6e).frames(true), 0xdd);
		Assert.array(I2cAddress.of7Bit(0x6e).frames(false), 0xdc);
		Assert.array(I2cAddress.of10Bit(0x6e).frames(true), 0xf1, 0x6e);
		Assert.array(I2cAddress.of10Bit(0x6e).frames(false), 0xf0, 0x6e);
	}

	@Test
	public void shouldCreateFromFrames() {
		Assert.equal(I2cAddress.fromFrames(Array.bytes.of(0xdd)), I2cAddress.of7Bit(0x6e));
		Assert.equal(I2cAddress.fromFrames(Array.bytes.of(0xf1, 0x6e)),
			I2cAddress.of10Bit(0x6e));
		Assert.thrown(() -> I2cAddress.fromFrames(Array.bytes.of(0xe1, 0x6e)));
	}

	@Test
	public void shouldDetermineIfSlaveAddress() {
		Assert.equal(I2cAddress.of(0x7).isSlave(), false);
		Assert.equal(I2cAddress.of(0x8).isSlave(), true);
		Assert.equal(I2cAddress.of(0x77).isSlave(), true);
		Assert.equal(I2cAddress.of(0x78).isSlave(), false);
		Assert.equal(I2cAddress.of10Bit(0x7).isSlave(), true);
		Assert.equal(I2cAddress.of10Bit(0x78).isSlave(), true);
	}
}
