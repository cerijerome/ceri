package ceri.common.array;

import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;

public class DynamicArrayBehavior {

	@Test
	public void shouldGrowX2() {
		assertGrowth(DynamicArray.growX2(0), 0, 2, 4, 6, 8, 10);
		assertGrowth(DynamicArray.growX2(1), 1, 2, 4, 6, 8, 10);
		assertGrowth(DynamicArray.growX2(2), 2, 2, 4, 6, 8, 10);
		assertGrowth(DynamicArray.growX2(3), 3, 3, 3, 6, 8, 10);
	}

	@Test
	public void shouldGrowByStep() {
		assertGrowth(DynamicArray.growByStep(0, 3), 3, 4, 5, 6, 7, 8);
		assertGrowth(DynamicArray.growByStep(2, 3), 2, 2, 5, 6, 7, 8);
	}

	@Test
	public void shouldGrowObjectArray() {
		var d = DynamicArray.of();
		Assert.array(d.array(), null, null, null, null, null, null, null, null);
		d.append(0, 1, 2, 3, 4, 5, 6, 7, 8);
		Assert.array(d.array(), 0, 1, 2, 3, 4, 5, 6, 7, 8, null, null, null, null, null, null,
			null);
		Assert.array(d.truncate(), 0, 1, 2, 3, 4, 5, 6, 7, 8);
	}

	@Test
	public void shouldAccessObjectsByIndex() {
		var d = DynamicArray.of();
		d.set(1, "a", "b");
		d.set(8, "c");
		Assert.equal(d.get(2), "b");
		Assert.equal(d.get(9), null);
		Assert.array(d.truncate(), null, "a", "b", null, null, null, null, null, "c", null);
	}

	@Test
	public void shouldGrowBools() {
		var d = DynamicArray.bools();
		d.accept(true);
		Assert.array(d.array(), true, false, false, false, false, false, false, false);
		d.append(true, true, false, false, false, false, true, true);
		Assert.array(d.array(), true, true, true, false, false, false, false, true, true, false,
			false, false, false, false, false, false);
		Assert.array(d.truncate(), true, true, true, false, false, false, false, true, true);
	}

	@Test
	public void shouldAccessBoolsByIndex() {
		var d = DynamicArray.bools();
		d.set(1, true, true);
		d.set(8, true);
		Assert.equal(d.get(2), true);
		Assert.equal(d.get(9), false);
		Assert.array(d.truncate(), false, true, true, false, false, false, false, false, true,
			false);
	}

	@Test
	public void shouldGrowChars() {
		var d = DynamicArray.chars();
		d.accept('a');
		d.append('b', 'c', 'd');
		Assert.array(d.array(), 'a', 'b', 'c', 'd', '\0', '\0', '\0', '\0');
		d.append("efghi");
		Assert.array(d.array(), 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '\0', '\0', '\0', '\0',
			'\0', '\0', '\0');
		Assert.equal(d.toString(), "abcdefghi");
	}

	@Test
	public void shouldAccessCharsAtIndex() {
		var d = DynamicArray.chars();
		d.set(1, "ab");
		d.set(8, 'c');
		Assert.equal(d.get(2), 'b');
		Assert.equal(d.get(9), '\0');
		Assert.array(d.truncate(), '\0', 'a', 'b', '\0', '\0', '\0', '\0', '\0', 'c', '\0');
	}

	@Test
	public void shouldGrowBytes() {
		var d = DynamicArray.bytes();
		d.accept(-1);
		d.append(Byte.MIN_VALUE, (byte) 0, Byte.MAX_VALUE);
		Assert.array(d.array(), -1, 0x80, 0, 0x7f, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		Assert.array(d.array(), -1, 0x80, 0, 0x7f, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0);
		Assert.array(d.truncate(), -1, 0x80, 0, 0x7f, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldAccessBytesByIndex() {
		var d = DynamicArray.bytes();
		d.set(1, Byte.MIN_VALUE, Byte.MAX_VALUE);
		d.set(8, 1);
		Assert.equal(d.get(2), Byte.MAX_VALUE);
		Assert.equal(d.get(9), (byte) 0);
		Assert.array(d.wrap(), 0, 0x80, 0x7f, 0, 0, 0, 0, 0, 1, 0);
	}

	@Test
	public void shouldGrowShorts() {
		var d = DynamicArray.shorts();
		d.accept(-1);
		d.append(Short.MIN_VALUE, (short) 0, Short.MAX_VALUE);
		Assert.array(d.array(), -1, 0x8000, 0, 0x7fff, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		Assert.array(d.array(), -1, 0x8000, 0, 0x7fff, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0);
		Assert.array(d.truncate(), -1, 0x8000, 0, 0x7fff, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldAccessShortsByIndex() {
		var d = DynamicArray.shorts();
		d.set(1, Short.MIN_VALUE, Short.MAX_VALUE);
		d.set(8, 1);
		Assert.equal(d.get(2), Short.MAX_VALUE);
		Assert.equal(d.get(9), (short) 0);
		Assert.array(d.truncate(), 0, 0x8000, 0x7fff, 0, 0, 0, 0, 0, 1, 0);
	}

	@Test
	public void shouldGrowInts() {
		var d = DynamicArray.ints();
		d.accept(-1);
		d.append(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		Assert.array(d.array(), -1, 0x80000000, 0, 0x7fffffff, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		Assert.array(d.array(), -1, 0x80000000, 0, 0x7fffffff, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0);
		Assert.array(d.truncate(), -1, 0x80000000, 0, 0x7fffffff, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldAccessIntsByIndex() {
		var d = DynamicArray.ints();
		d.set(1, -1, -1);
		d.set(8, 1);
		Assert.equal(d.get(2), -1);
		Assert.equal(d.get(9), 0);
		Assert.array(d.wrap(), 0, -1, -1, 0, 0, 0, 0, 0, 1, 0);
	}

	@Test
	public void shouldGrowLongs() {
		var d = DynamicArray.longs();
		d.accept(-1);
		d.append(Long.MIN_VALUE, 0, Long.MAX_VALUE);
		Assert.array(d.array(), -1, Long.MIN_VALUE, 0, Long.MAX_VALUE, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		Assert.array(d.array(), -1, Long.MIN_VALUE, 0, Long.MAX_VALUE, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0,
			0, 0);
		Assert.array(d.truncate(), -1, Long.MIN_VALUE, 0, Long.MAX_VALUE, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldAccessLongsByIndex() {
		var d = DynamicArray.longs();
		d.set(1, -1, -1);
		d.set(8, 1);
		Assert.equal(d.get(2), -1L);
		Assert.equal(d.get(9), 0L);
		Assert.array(d.wrap(), 0, -1, -1, 0, 0, 0, 0, 0, 1, 0);
	}

	@Test
	public void shouldGrowFloats() {
		var d = DynamicArray.floats();
		d.accept(-1);
		d.append(Float.MIN_VALUE, 0f, Float.MAX_VALUE);
		Assert.array(d.array(), -1, Float.MIN_VALUE, 0, Float.MAX_VALUE, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		Assert.array(d.array(), -1, Float.MIN_VALUE, 0, Float.MAX_VALUE, 1, 2, 3, 4, 5, 0, 0, 0, 0,
			0, 0, 0);
		Assert.array(d.truncate(), -1, Float.MIN_VALUE, 0, Float.MAX_VALUE, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldAccessFloatsByIndex() {
		var d = DynamicArray.floats();
		d.set(1, -1, -1);
		d.set(8, 1);
		Assert.equal(d.get(2), -1.0f);
		Assert.equal(d.get(9), 0.0f);
		Assert.array(d.truncate(), 0, -1, -1, 0, 0, 0, 0, 0, 1, 0);
	}

	@Test
	public void shouldGrowDoubles() {
		var d = DynamicArray.doubles();
		d.accept(-1);
		d.append(Double.MIN_VALUE, 0, Double.MAX_VALUE);
		Assert.array(d.array(), -1, Double.MIN_VALUE, 0, Double.MAX_VALUE, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		Assert.array(d.array(), -1, Double.MIN_VALUE, 0, Double.MAX_VALUE, 1, 2, 3, 4, 5, 0, 0, 0,
			0, 0, 0, 0);
		Assert.array(d.truncate(), -1, Double.MIN_VALUE, 0, Double.MAX_VALUE, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldAccessDoublesByIndex() {
		var d = DynamicArray.doubles();
		d.set(1, -1, -1);
		d.set(8, 1);
		Assert.equal(d.get(2), -1.0);
		Assert.equal(d.get(9), 0.0);
		Assert.array(d.truncate(), 0, -1, -1, 0, 0, 0, 0, 0, 1, 0);
	}

	@Test
	public void shouldSetIndex() {
		var d = DynamicArray.ints();
		d.index(2);
		d.append(1, 2, 3);
		d.index(-1);
		d.append(4, 5);
		Assert.array(d.wrap(), 0, 0, 1, 2, 4, 5);
	}

	@Test
	public void shouldSetAtRelativeIndex() {
		var d = DynamicArray.ints();
		d.append(1, 2, 3, 4, 5);
		d.set(-2, 6, 7, 8);
		Assert.array(d.wrap(), 1, 2, 3, 6, 7, 8);
	}

	private static void assertGrowth(Functions.IntOperator growth, int... expecteds) {
		for (int i = 0; i < expecteds.length; i++)
			Assert.equal(growth.applyAsInt(i), expecteds[i]);
	}
}
