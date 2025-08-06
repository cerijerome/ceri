package ceri.common.array;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import ceri.common.test.Captor;

public class PrimitiveArrayBehavior {
	private final boolean[] bools = { true, false, true };
	private final char[] chars = { 'a', '\0', 'c' };
	private final byte[] bytes = { Byte.MIN_VALUE, Byte.MAX_VALUE, 0 };
	private final short[] shorts = { Short.MIN_VALUE, Short.MAX_VALUE, 0 };
	private final int[] ints = { Integer.MIN_VALUE, Integer.MAX_VALUE, 0 };
	private final long[] longs = { Long.MIN_VALUE, Long.MAX_VALUE, 0 };
	private final float[] floats = { Float.MIN_VALUE, Float.MAX_VALUE, 0 };
	private final double[] doubles = { Double.MIN_VALUE, Double.MAX_VALUE, 0 };

	@Test
	public void shouldCreateFromVarargs() {
		assertArray(ArrayUtil.bools.of(bools), bools);
		assertArray(ArrayUtil.bools.of(bools), bools);
		assertArray(ArrayUtil.chars.of(chars), chars);
		assertArray(ArrayUtil.bytes.of(bytes), bytes);
		assertArray(ArrayUtil.shorts.of(shorts), shorts);
		assertArray(ArrayUtil.ints.of(ints), ints);
		assertArray(ArrayUtil.longs.of(longs), longs);
		assertArray(ArrayUtil.floats.of(floats), floats);
		assertArray(ArrayUtil.doubles.of(doubles), doubles);
	}

	@Test
	public void shouldCreateRange() {
		assertArray(ArrayUtil.ints.range(5, 0));
		assertArray(ArrayUtil.ints.range(-1, 3), -1, 0, 1);
		assertArray(ArrayUtil.longs.range(5, 0));
		assertArray(ArrayUtil.longs.range(-1, 3), -1L, 0L, 1L);
	}

	@Test
	public void shouldBoxElements() {
		assertBoxed(ArrayUtil.bools.boxed(bools), true, false, true);
		assertBoxed(ArrayUtil.chars.boxed(chars), 'a', '\0', 'c');
		assertBoxed(ArrayUtil.bytes.boxed(bytes), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0);
		assertBoxed(ArrayUtil.bytes.boxed(0x80, 0x7f, 0), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0);
		assertBoxed(ArrayUtil.shorts.boxed(shorts), Short.MIN_VALUE, Short.MAX_VALUE, (short) 0);
		assertBoxed(ArrayUtil.shorts.boxed(0x8000, 0x7fff, 0), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0);
		assertBoxed(ArrayUtil.ints.boxed(ints), Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		assertBoxed(ArrayUtil.longs.boxed(longs), Long.MIN_VALUE, Long.MAX_VALUE, 0L);
		assertBoxed(ArrayUtil.floats.boxed(floats), Float.MIN_VALUE, Float.MAX_VALUE, 0f);
		assertBoxed(ArrayUtil.floats.boxed(Float.MIN_VALUE, Float.MAX_VALUE, 0.0), Float.MIN_VALUE,
			Float.MAX_VALUE, 0f);
		assertBoxed(ArrayUtil.doubles.boxed(doubles), Double.MIN_VALUE, Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldUnboxElements() {
		assertArray(ArrayUtil.bools.unboxed(ArrayUtil.of(true, false, true)), bools);
		assertArray(ArrayUtil.chars.unboxed(ArrayUtil.of('a', '\0', 'c')), chars);
	}

	@Test
	public void shouldUnboxLists() {
		assertArray(ArrayUtil.bools.unboxed(List.of(true, false, true), 1), false, true);
		assertArray(ArrayUtil.chars.unboxed(List.of('a', '\0', 'c'), 1), '\0', 'c');
	}

	@Test
	public void shouldProvideElementAtIndex() {
		assertEquals(ArrayUtil.bools.at(bools, 1, true), false);
		assertEquals(ArrayUtil.bools.at(bools, -1, true), true);
		assertEquals(ArrayUtil.chars.at(chars, 1, -1), '\0');
		assertEquals(ArrayUtil.chars.at(chars, -1, -1), (char) -1);
		assertEquals(ArrayUtil.bytes.at(bytes, 1, -1), Byte.MAX_VALUE);
		assertEquals(ArrayUtil.bytes.at(bytes, -1, -1), (byte) -1);
		assertEquals(ArrayUtil.shorts.at(shorts, 1, -1), Short.MAX_VALUE);
		assertEquals(ArrayUtil.shorts.at(shorts, -1, -1), (short) -1);
		assertEquals(ArrayUtil.ints.at(ints, 1, -1), Integer.MAX_VALUE);
		assertEquals(ArrayUtil.ints.at(ints, -1, -1), -1);
		assertEquals(ArrayUtil.longs.at(longs, 1, -1), Long.MAX_VALUE);
		assertEquals(ArrayUtil.longs.at(longs, -1, -1), -1L);
		assertEquals(ArrayUtil.floats.at(floats, 1, -1), Float.MAX_VALUE);
		assertEquals(ArrayUtil.floats.at(floats, -1, -1.0), -1f);
		assertEquals(ArrayUtil.doubles.at(doubles, 1, -1), Double.MAX_VALUE);
		assertEquals(ArrayUtil.doubles.at(doubles, -1, -1), -1.0);
	}

	@Test
	public void shouldProvideLastElement() {
		assertEquals(ArrayUtil.bools.last(bools, false), true);
		assertEquals(ArrayUtil.bools.last(ArrayUtil.bools.empty, false), false);
		assertEquals(ArrayUtil.chars.last(chars, -1), 'c');
		assertEquals(ArrayUtil.chars.last(ArrayUtil.chars.empty, -1), (char) -1);
		assertEquals(ArrayUtil.bytes.last(bytes, -1), (byte) 0);
		assertEquals(ArrayUtil.bytes.last(ArrayUtil.bytes.empty, -1), (byte) -1);
		assertEquals(ArrayUtil.shorts.last(shorts, -1), (short) 0);
		assertEquals(ArrayUtil.shorts.last(ArrayUtil.shorts.empty, -1), (short) -1);
		assertEquals(ArrayUtil.ints.last(ints, -1), 0);
		assertEquals(ArrayUtil.ints.last(ArrayUtil.ints.empty, -1), -1);
		assertEquals(ArrayUtil.longs.last(longs, -1), 0L);
		assertEquals(ArrayUtil.longs.last(ArrayUtil.longs.empty, -1), -1L);
		assertEquals(ArrayUtil.floats.last(floats, -1), 0f);
		assertEquals(ArrayUtil.floats.last(ArrayUtil.floats.empty, -1.0), -1f);
		assertEquals(ArrayUtil.doubles.last(doubles, -1), 0.0);
		assertEquals(ArrayUtil.doubles.last(ArrayUtil.doubles.empty, -1), -1.0);
	}

	@Test
	public void shouldAppendElements() {
		assertArray(ArrayUtil.bools.append(bools, false), true, false, true, false);
		assertArray(ArrayUtil.chars.append(chars, (char) -1), 'a', '\0', 'c', (char) -1);
		assertArray(ArrayUtil.bytes.append(bytes, (byte) -1), Byte.MIN_VALUE, Byte.MAX_VALUE,
			(byte) 0, (byte) -1);
		assertArray(ArrayUtil.shorts.append(shorts, (short) -1), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0, (short) -1);
		assertArray(ArrayUtil.ints.append(ints, -1), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1);
		assertArray(ArrayUtil.longs.append(longs, -1), Long.MIN_VALUE, Long.MAX_VALUE, 0L, -1L);
		assertArray(ArrayUtil.floats.append(floats, -1), Float.MIN_VALUE, Float.MAX_VALUE, 0f, -1f);
		assertArray(ArrayUtil.doubles.append(doubles, -1), Double.MIN_VALUE, Double.MAX_VALUE, 0.0,
			-1.0);
	}

	@Test
	public void shouldInsertElements() {
		assertArray(ArrayUtil.bools.insert(bools, 1, false), true, false, false, true);
		assertArray(ArrayUtil.chars.insert(chars, 1, (char) -1), 'a', (char) -1, '\0', 'c');
		assertArray(ArrayUtil.bytes.insert(bytes, 1, (byte) -1), Byte.MIN_VALUE, (byte) -1,
			Byte.MAX_VALUE, (byte) 0);
		assertArray(ArrayUtil.shorts.insert(shorts, 1, (short) -1), Short.MIN_VALUE, (short) -1,
			Short.MAX_VALUE, (short) 0);
		assertArray(ArrayUtil.ints.insert(ints, 1, -1), Integer.MIN_VALUE, -1, Integer.MAX_VALUE,
			0);
		assertArray(ArrayUtil.longs.insert(longs, 1, -1), Long.MIN_VALUE, -1L, Long.MAX_VALUE, 0L);
		assertArray(ArrayUtil.floats.insert(floats, 1, -1), Float.MIN_VALUE, -1f, Float.MAX_VALUE,
			0f);
		assertArray(ArrayUtil.doubles.insert(doubles, 1, -1), Double.MIN_VALUE, -1.0,
			Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldDetermineIfElementsAreContained() {
		assertEquals(ArrayUtil.bools.contains(bools, bools), true);
		assertEquals(ArrayUtil.chars.contains(chars, chars), true);
		assertEquals(ArrayUtil.bytes.contains(bytes, bytes), true);
		assertEquals(ArrayUtil.shorts.contains(shorts, shorts), true);
		assertEquals(ArrayUtil.ints.contains(ints, ints), true);
		assertEquals(ArrayUtil.longs.contains(longs, longs), true);
		assertEquals(ArrayUtil.floats.contains(floats, floats), true);
		assertEquals(ArrayUtil.doubles.contains(doubles, doubles), true);
	}

	@Test
	public void shouldDetermineIndexOfElements() {
		assertEquals(ArrayUtil.bools.indexOf(bools, bools), 0);
		assertEquals(ArrayUtil.chars.indexOf(chars, chars), 0);
		assertEquals(ArrayUtil.bytes.indexOf(bytes, bytes), 0);
		assertEquals(ArrayUtil.shorts.indexOf(shorts, shorts), 0);
		assertEquals(ArrayUtil.ints.indexOf(ints, ints), 0);
		assertEquals(ArrayUtil.longs.indexOf(longs, longs), 0);
		assertEquals(ArrayUtil.floats.indexOf(floats, floats), 0);
		assertEquals(ArrayUtil.doubles.indexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldDetermineLastIndexOfElements() {
		assertEquals(ArrayUtil.bools.lastIndexOf(bools, bools), 0);
		assertEquals(ArrayUtil.chars.lastIndexOf(chars, chars), 0);
		assertEquals(ArrayUtil.bytes.lastIndexOf(bytes, bytes), 0);
		assertEquals(ArrayUtil.shorts.lastIndexOf(shorts, shorts), 0);
		assertEquals(ArrayUtil.ints.lastIndexOf(ints, ints), 0);
		assertEquals(ArrayUtil.longs.lastIndexOf(longs, longs), 0);
		assertEquals(ArrayUtil.floats.lastIndexOf(floats, floats), 0);
		assertEquals(ArrayUtil.doubles.lastIndexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldFillElements() {
		assertArray(ArrayUtil.bools.fill(new boolean[3], true), true, true, true);
		assertArray(ArrayUtil.chars.fill(new char[3], (char) -1), (char) -1, (char) -1, (char) -1);
		assertArray(ArrayUtil.bytes.fill(new byte[3], (byte) -1), (byte) -1, (byte) -1, (byte) -1);
		assertArray(ArrayUtil.shorts.fill(new short[3], (short) -1), (short) -1, (short) -1,
			(short) -1);
		assertArray(ArrayUtil.ints.fill(new int[3], -1), -1, -1, -1);
		assertArray(ArrayUtil.longs.fill(new long[3], -1), -1L, -1L, -1L);
		assertArray(ArrayUtil.floats.fill(new float[3], -1), -1f, -1f, -1f);
		assertArray(ArrayUtil.doubles.fill(new double[3], -1), -1.0, -1.0, -1.0);
	}

	@Test
	public void shouldReverseElements() {
		assertArray(ArrayUtil.bools.reverse(bools.clone()), true, false, true);
		assertArray(ArrayUtil.chars.reverse(chars.clone()), 'c', '\0', 'a');
		assertArray(ArrayUtil.bytes.reverse(bytes.clone()), 0, Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertArray(ArrayUtil.shorts.reverse(shorts.clone()), 0, Short.MAX_VALUE, Short.MIN_VALUE);
		assertArray(ArrayUtil.ints.reverse(ints.clone()), 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
		assertArray(ArrayUtil.longs.reverse(longs.clone()), 0, Long.MAX_VALUE, Long.MIN_VALUE);
		assertArray(ArrayUtil.floats.reverse(floats.clone()), 0, Float.MAX_VALUE, Float.MIN_VALUE);
		assertArray(ArrayUtil.doubles.reverse(doubles.clone()), 0, Double.MAX_VALUE,
			Double.MIN_VALUE);
	}

	@Test
	public void shouldConsumeElements() {
		Captor.of().apply(c -> ArrayUtil.bools.forEach(bools, c::accept)).verify(true, false, true);
		Captor.of().apply(c -> ArrayUtil.chars.forEach(chars, c::accept)).verify(97, 0, 99);
		Captor.of().apply(c -> ArrayUtil.bytes.forEach(bytes, c::accept)).verify(-0x80, 0x7f, 0);
		Captor.of().apply(c -> ArrayUtil.shorts.forEach(shorts, c::accept)).verify(-0x8000, 0x7fff,
			0);
		Captor.of().apply(c -> ArrayUtil.ints.forEach(ints, c::accept)).verify(Integer.MIN_VALUE,
			Integer.MAX_VALUE, 0);
		Captor.of().apply(c -> ArrayUtil.longs.forEach(longs, c::accept)).verify(Long.MIN_VALUE,
			Long.MAX_VALUE, 0L);
		Captor.of().apply(c -> ArrayUtil.floats.forEach(floats, c::accept))
			.verify((double) Float.MIN_VALUE, (double) Float.MAX_VALUE, 0.0);
		Captor.of().apply(c -> ArrayUtil.doubles.forEach(doubles, c::accept))
			.verify(Double.MIN_VALUE, Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldConsumeBoxedElements() {
		Captor.of().apply(c -> ArrayUtil.bools.forEachBox(bools, c::accept)).verify(true, false,
			true);
		Captor.of().apply(c -> ArrayUtil.chars.forEachBox(chars, c::accept)).verify('a', '\0', 'c');
	}

	@Test
	public void shouldConsumeIndexedBoxedElements() {
		Captor.ofBi().apply(c -> ArrayUtil.bools.forEachBoxIndexed(bools, c::accept)).verify(true,
			0, false, 1, true, 2);
		Captor.ofBi().apply(c -> ArrayUtil.chars.forEachBoxIndexed(chars, c::accept)).verify('a', 0,
			'\0', 1, 'c', 2);
	}

	@Test
	public void shouldSortElements() {
		assertArray(ArrayUtil.bools.sort(bools.clone()), false, true, true);
		assertArray(ArrayUtil.chars.sort(chars.clone()), '\0', 'a', 'c');
		assertArray(ArrayUtil.bytes.sort(bytes.clone()), Byte.MIN_VALUE, 0, Byte.MAX_VALUE);
		assertArray(ArrayUtil.shorts.sort(shorts.clone()), Short.MIN_VALUE, 0, Short.MAX_VALUE);
		assertArray(ArrayUtil.ints.sort(ints.clone()), Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		assertArray(ArrayUtil.longs.sort(longs.clone()), Long.MIN_VALUE, 0, Long.MAX_VALUE);
		assertArray(ArrayUtil.floats.sort(floats.clone()), 0, Float.MIN_VALUE, Float.MAX_VALUE);
		assertArray(ArrayUtil.doubles.sort(doubles.clone()), 0, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	@Test
	public void shouldHashelement() {
		assertEquals(ArrayUtil.bools.hash(bools), Arrays.hashCode(bools));
		assertEquals(ArrayUtil.chars.hash(chars), Arrays.hashCode(chars));
		assertEquals(ArrayUtil.bytes.hash(bytes), Arrays.hashCode(bytes));
		assertEquals(ArrayUtil.shorts.hash(shorts), Arrays.hashCode(shorts));
		assertEquals(ArrayUtil.ints.hash(ints), Arrays.hashCode(ints));
		assertEquals(ArrayUtil.longs.hash(longs), Arrays.hashCode(longs));
		assertEquals(ArrayUtil.floats.hash(floats), Arrays.hashCode(floats));
		assertEquals(ArrayUtil.doubles.hash(doubles), Arrays.hashCode(doubles));
	}

	@Test
	public void shouldProvideHexString() {
		assertEquals(ArrayUtil.chars.toHex(chars), "[0x61, 0x0, 0x63]");
		assertEquals(ArrayUtil.bytes.toHex(bytes), "[0x80, 0x7f, 0x0]");
		assertEquals(ArrayUtil.shorts.toHex(shorts), "[0x8000, 0x7fff, 0x0]");
		assertEquals(ArrayUtil.ints.toHex(ints), "[0x80000000, 0x7fffffff, 0x0]");
		assertEquals(ArrayUtil.longs.toHex(longs), "[0x8000000000000000, 0x7fffffffffffffff, 0x0]");
	}

	@SafeVarargs
	private static <T> void assertBoxed(T[] result, T... expected) {
		assertArray(result, expected);
	}
}
