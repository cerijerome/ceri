package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class IntBitSetBehavior {

	@Test
	public void shouldCreateFromBits() {
		IntBitSet b = IntBitSet.from(0, 3, 5, 7, 8);
		assertArray(b.toByteArray(), 0xa9, 0x01);
	}

	@Test
	public void shouldBinaryOrBits() {
		IntBitSet b = IntBitSet.of();
		b.or(IntBitSet.of(0xff));
		assertEquals(b.value(), 0xff);
	}

	@Test
	public void shouldCreateShortWidthSet() {
		IntBitSet b = IntBitSet.ofShort(0xf0e0d);
		assertArray(b.toByteArray(), 0x0d, 0x0e);
	}

	@Test
	public void shouldCreateByteWidthSet() {
		IntBitSet b = IntBitSet.ofByte(0xf0e0d);
		assertArray(b.toByteArray(), 0x0d);
	}

	@Test
	public void shouldSetBits() {
		IntBitSet b = IntBitSet.of(0xf0e0d);
		b.setBits(true, 8, 9);
		assertArray(b.toByteArray(), 0x0d, 0x0f, 0x0f);
	}

	@Test
	public void shouldSetBitsFromValue() {
		IntBitSet b = IntBitSet.of(0xf0e0d);
		b.setValue(8, 0x78, Byte.SIZE);
		assertArray(b.toByteArray(), 0x0d, 0x78, 0x0f);
	}

	@Test
	public void shouldGetValueFromBits() {
		IntBitSet b = IntBitSet.of(0xf0e0d);
		assertEquals(b.getValue(8, Short.SIZE), 0x0f0e);
	}

	@Test
	public void shouldProvideSetBits() {
		IntBitSet b = IntBitSet.of(0x4321);
		assertCollection(b.bits(), 0, 5, 8, 9, 14);
	}

	@Test
	public void shouldValueByWidth() {
		IntBitSet b = IntBitSet.of(0xfedcba98);
		assertEquals(b.byteValue(), (byte) 0x98);
		assertEquals(b.shortValue(), (short) 0xba98);
		assertEquals(b.value(), 0xfedcba98);
		assertEquals(b.unsignedValue(), 0xfedcba98L);
	}

}
