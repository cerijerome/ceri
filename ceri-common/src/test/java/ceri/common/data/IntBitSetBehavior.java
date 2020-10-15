package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(b.value(), is(0xff));
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
		assertThat(b.getValue(8, Short.SIZE), is(0x0f0e));
	}

	@Test
	public void shouldProvideSetBits() {
		IntBitSet b = IntBitSet.of(0x4321);
		assertCollection(b.bits(), 0, 5, 8, 9, 14);
	}

	@Test
	public void shouldValueByWidth() {
		IntBitSet b = IntBitSet.of(0xfedcba98);
		assertThat(b.byteValue(), is((byte) 0x98));
		assertThat(b.shortValue(), is((short) 0xba98));
		assertThat(b.value(), is(0xfedcba98));
		assertThat(b.unsignedValue(), is(0xfedcba98L));
	}

}
