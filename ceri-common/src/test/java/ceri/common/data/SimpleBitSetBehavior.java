package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class SimpleBitSetBehavior {

	@Test
	public void shouldCreateFromBits() {
		SimpleBitSet b = SimpleBitSet.from(0, 3, 5, 7, 8);
		assertArray(b.toByteArray(), 0xa9, 0x01);
	}

	@Test
	public void shouldBinaryOrBits() {
		SimpleBitSet b = SimpleBitSet.of();
		b.or(SimpleBitSet.ofInt(0xff));
		assertEquals(b.intValue(), 0xff);
	}

	@Test
	public void shouldCreateLongWidthSet() {
		SimpleBitSet b = SimpleBitSet.ofLong(0xfedcba9876543210L);
		assertArray(b.toByteArray(), 0x10, 0x32, 0x54, 0x76, 0x98, 0xba, 0xdc, 0xfe);
	}

	@Test
	public void shouldCreateShortWidthSet() {
		SimpleBitSet b = SimpleBitSet.ofShort(0xf0e0d);
		assertArray(b.toByteArray(), 0x0d, 0x0e);
	}

	@Test
	public void shouldCreateByteWidthSet() {
		SimpleBitSet b = SimpleBitSet.ofByte(0xf0e0d);
		assertArray(b.toByteArray(), 0x0d);
	}

	@Test
	public void shouldSetBits() {
		SimpleBitSet b = SimpleBitSet.ofInt(0xf0e0d);
		b.setBits(true, 8, 9);
		assertArray(b.toByteArray(), 0x0d, 0x0f, 0x0f);
	}

	@Test
	public void shouldSetBitsFromValue() {
		SimpleBitSet b = SimpleBitSet.ofInt(0xf0e0d);
		b.setValue(8, 0x78, Byte.SIZE);
		assertArray(b.toByteArray(), 0x0d, 0x78, 0x0f);
	}

	@Test
	public void shouldGetValueFromBits() {
		SimpleBitSet b = SimpleBitSet.ofInt(0xf0e0d);
		assertEquals(b.getValue(8, Short.SIZE), 0x0f0eL);
	}

	@Test
	public void shouldProvideSetBits() {
		SimpleBitSet b = SimpleBitSet.ofInt(0x4321);
		assertCollection(b.bits(), 0, 5, 8, 9, 14);
	}

	@Test
	public void shouldGetValueByWidth() {
		SimpleBitSet b = SimpleBitSet.ofInt(0xfedcba98);
		assertEquals(b.byteValue(), (byte) 0x98);
		assertEquals(b.ubyteValue(), (short) 0x98);
		assertEquals(b.shortValue(), (short) 0xba98);
		assertEquals(b.ushortValue(), 0xba98);
		assertEquals(b.intValue(), 0xfedcba98);
		assertEquals(b.uintValue(), 0xfedcba98L);
		assertEquals(b.longValue(), 0xfedcba98L);
	}

	@Test
	public void shouldGetEmptyValue() {
		SimpleBitSet b = SimpleBitSet.of();
		assertEquals(b.byteValue(), (byte) 0);
		assertEquals(b.shortValue(), (short) 0);
		assertEquals(b.intValue(), 0);
		assertEquals(b.uintValue(), 0L);
		assertEquals(b.longValue(), 0L);
	}

}
