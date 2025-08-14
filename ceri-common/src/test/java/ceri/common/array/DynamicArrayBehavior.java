package ceri.common.array;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.function.Functions;

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
		assertArray(d.array(), null, null, null, null, null, null, null, null);
		d.append(0, 1, 2, 3, 4, 5, 6, 7, 8);
		assertArray(d.array(), 0, 1, 2, 3, 4, 5, 6, 7, 8, null, null, null, null, null, null, null);
		assertArray(d.array(), 0, 1, 2, 3, 4, 5, 6, 7, 8, null, null, null, null, null, null, null);
	}

	@Test
	public void shouldGrowBools() {
		var d = DynamicArray.bools();
		d.accept(true);
		assertArray(d.array(), true, false, false, false, false, false, false, false);
		d.append(true, true, false, false, false, false, true, true);
		assertArray(d.array(), true, true, true, false, false, false, false, true, true, false,
			false, false, false, false, false, false);
		assertArray(d.truncate(), true, true, true, false, false, false, false, true, true);
	}

	@Test
	public void shouldGrowChars() {
		var d = DynamicArray.chars();
		d.accept('a');
		d.append('b', 'c', 'd');
		assertArray(d.array(), 'a', 'b', 'c', 'd', '\0', '\0', '\0', '\0');
		d.append("efghi");
		assertArray(d.array(), 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '\0', '\0', '\0', '\0',
			'\0', '\0', '\0');
		assertEquals(d.toString(), "abcdefghi");
	}

	@Test
	public void shouldGrowBytes() {
		var d = DynamicArray.bytes();
		d.accept(-1);
		d.append(Byte.MIN_VALUE, (byte) 0, Byte.MAX_VALUE);
		assertArray(d.array(), -1, 0x80, 0, 0x7f, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		assertArray(d.array(), -1, 0x80, 0, 0x7f, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0);
		assertArray(d.truncate(), -1, 0x80, 0, 0x7f, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldGrowShorts() {
		var d = DynamicArray.shorts();
		d.accept(-1);
		d.append(Short.MIN_VALUE, (short) 0, Short.MAX_VALUE);
		assertArray(d.array(), -1, 0x8000, 0, 0x7fff, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		assertArray(d.array(), -1, 0x8000, 0, 0x7fff, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0);
		assertArray(d.truncate(), -1, 0x8000, 0, 0x7fff, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldGrowInts() {
		var d = DynamicArray.ints();
		d.accept(-1);
		d.append(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		assertArray(d.array(), -1, 0x80000000, 0, 0x7fffffff, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		assertArray(d.array(), -1, 0x80000000, 0, 0x7fffffff, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0, 0);
		assertArray(d.truncate(), -1, 0x80000000, 0, 0x7fffffff, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldGrowLongs() {
		var d = DynamicArray.longs();
		d.accept(-1);
		d.append(Long.MIN_VALUE, 0, Long.MAX_VALUE);
		assertArray(d.array(), -1, Long.MIN_VALUE, 0, Long.MAX_VALUE, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		assertArray(d.array(), -1, Long.MIN_VALUE, 0, Long.MAX_VALUE, 1, 2, 3, 4, 5, 0, 0, 0, 0, 0,
			0, 0);
		assertArray(d.truncate(), -1, Long.MIN_VALUE, 0, Long.MAX_VALUE, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldGrowFloats() {
		var d = DynamicArray.floats();
		d.accept(-1);
		d.append(Float.MIN_VALUE, 0f, Float.MAX_VALUE);
		assertArray(d.array(), -1, Float.MIN_VALUE, 0, Float.MAX_VALUE, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		assertArray(d.array(), -1, Float.MIN_VALUE, 0, Float.MAX_VALUE, 1, 2, 3, 4, 5, 0, 0, 0, 0,
			0, 0, 0);
		assertArray(d.truncate(), -1, Float.MIN_VALUE, 0, Float.MAX_VALUE, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldGrowDoubles() {
		var d = DynamicArray.doubles();
		d.accept(-1);
		d.append(Double.MIN_VALUE, 0, Double.MAX_VALUE);
		assertArray(d.array(), -1, Double.MIN_VALUE, 0, Double.MAX_VALUE, 0, 0, 0, 0);
		d.append(1, 2, 3, 4, 5);
		assertArray(d.array(), -1, Double.MIN_VALUE, 0, Double.MAX_VALUE, 1, 2, 3, 4, 5, 0, 0, 0, 0,
			0, 0, 0);
		assertArray(d.truncate(), -1, Double.MIN_VALUE, 0, Double.MAX_VALUE, 1, 2, 3, 4, 5);
	}

	private static void assertGrowth(Functions.IntOperator growth, int... expecteds) {
		for (int i = 0; i < expecteds.length; i++)
			assertEquals(growth.applyAsInt(i), expecteds[i]);
	}
}
