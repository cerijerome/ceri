package ceri.common.array;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import ceri.common.test.Assert;
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
		Assert.array(ArrayUtil.bools.of(bools), bools);
		Assert.array(ArrayUtil.bools.of(bools), bools);
		Assert.array(ArrayUtil.chars.of(chars), chars);
		Assert.array(ArrayUtil.bytes.of(bytes), bytes);
		Assert.array(ArrayUtil.shorts.of(shorts), shorts);
		Assert.array(ArrayUtil.ints.of(ints), ints);
		Assert.array(ArrayUtil.longs.of(longs), longs);
		Assert.array(ArrayUtil.floats.of(floats), floats);
		Assert.array(ArrayUtil.doubles.of(doubles), doubles);
	}

	@Test
	public void shouldCreateRange() {
		Assert.array(ArrayUtil.ints.range(5, 0));
		Assert.array(ArrayUtil.ints.range(-1, 3), -1, 0, 1);
		Assert.array(ArrayUtil.longs.range(5, 0));
		Assert.array(ArrayUtil.longs.range(-1, 3), -1L, 0L, 1L);
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
		Assert.array(ArrayUtil.bools.unboxed(ArrayUtil.of(true, false, true)), bools);
		Assert.array(ArrayUtil.chars.unboxed(ArrayUtil.of('a', '\0', 'c')), chars);
	}

	@Test
	public void shouldUnboxLists() {
		Assert.array(ArrayUtil.bools.unboxed(List.of(true, false, true), 1), false, true);
		Assert.array(ArrayUtil.chars.unboxed(List.of('a', '\0', 'c'), 1), '\0', 'c');
	}

	@Test
	public void shouldProvideElementAtIndex() {
		Assert.equal(ArrayUtil.bools.at(bools, 1, true), false);
		Assert.equal(ArrayUtil.bools.at(bools, -1, true), true);
		Assert.equal(ArrayUtil.chars.at(chars, 1, -1), '\0');
		Assert.equal(ArrayUtil.chars.at(chars, -1, -1), (char) -1);
		Assert.equal(ArrayUtil.bytes.at(bytes, 1, -1), Byte.MAX_VALUE);
		Assert.equal(ArrayUtil.bytes.at(bytes, -1, -1), (byte) -1);
		Assert.equal(ArrayUtil.shorts.at(shorts, 1, -1), Short.MAX_VALUE);
		Assert.equal(ArrayUtil.shorts.at(shorts, -1, -1), (short) -1);
		Assert.equal(ArrayUtil.ints.at(ints, 1, -1), Integer.MAX_VALUE);
		Assert.equal(ArrayUtil.ints.at(ints, -1, -1), -1);
		Assert.equal(ArrayUtil.longs.at(longs, 1, -1), Long.MAX_VALUE);
		Assert.equal(ArrayUtil.longs.at(longs, -1, -1), -1L);
		Assert.equal(ArrayUtil.floats.at(floats, 1, -1), Float.MAX_VALUE);
		Assert.equal(ArrayUtil.floats.at(floats, -1, -1.0), -1f);
		Assert.equal(ArrayUtil.doubles.at(doubles, 1, -1), Double.MAX_VALUE);
		Assert.equal(ArrayUtil.doubles.at(doubles, -1, -1), -1.0);
	}

	@Test
	public void shouldProvideLastElement() {
		Assert.equal(ArrayUtil.bools.last(bools, false), true);
		Assert.equal(ArrayUtil.bools.last(ArrayUtil.bools.empty, false), false);
		Assert.equal(ArrayUtil.chars.last(chars, -1), 'c');
		Assert.equal(ArrayUtil.chars.last(ArrayUtil.chars.empty, -1), (char) -1);
		Assert.equal(ArrayUtil.bytes.last(bytes, -1), (byte) 0);
		Assert.equal(ArrayUtil.bytes.last(ArrayUtil.bytes.empty, -1), (byte) -1);
		Assert.equal(ArrayUtil.shorts.last(shorts, -1), (short) 0);
		Assert.equal(ArrayUtil.shorts.last(ArrayUtil.shorts.empty, -1), (short) -1);
		Assert.equal(ArrayUtil.ints.last(ints, -1), 0);
		Assert.equal(ArrayUtil.ints.last(ArrayUtil.ints.empty, -1), -1);
		Assert.equal(ArrayUtil.longs.last(longs, -1), 0L);
		Assert.equal(ArrayUtil.longs.last(ArrayUtil.longs.empty, -1), -1L);
		Assert.equal(ArrayUtil.floats.last(floats, -1), 0f);
		Assert.equal(ArrayUtil.floats.last(ArrayUtil.floats.empty, -1.0), -1f);
		Assert.equal(ArrayUtil.doubles.last(doubles, -1), 0.0);
		Assert.equal(ArrayUtil.doubles.last(ArrayUtil.doubles.empty, -1), -1.0);
	}

	@Test
	public void shouldAppendElements() {
		Assert.array(ArrayUtil.bools.append(bools, false), true, false, true, false);
		Assert.array(ArrayUtil.chars.append(chars, (char) -1), 'a', '\0', 'c', (char) -1);
		Assert.array(ArrayUtil.bytes.append(bytes, (byte) -1), Byte.MIN_VALUE, Byte.MAX_VALUE,
			(byte) 0, (byte) -1);
		Assert.array(ArrayUtil.shorts.append(shorts, (short) -1), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0, (short) -1);
		Assert.array(ArrayUtil.ints.append(ints, -1), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1);
		Assert.array(ArrayUtil.longs.append(longs, -1), Long.MIN_VALUE, Long.MAX_VALUE, 0L, -1L);
		Assert.array(ArrayUtil.floats.append(floats, -1), Float.MIN_VALUE, Float.MAX_VALUE, 0f,
			-1f);
		Assert.array(ArrayUtil.doubles.append(doubles, -1), Double.MIN_VALUE, Double.MAX_VALUE, 0.0,
			-1.0);
	}

	@Test
	public void shouldInsertElements() {
		Assert.array(ArrayUtil.bools.insert(bools, 1, false), true, false, false, true);
		Assert.array(ArrayUtil.chars.insert(chars, 1, (char) -1), 'a', (char) -1, '\0', 'c');
		Assert.array(ArrayUtil.bytes.insert(bytes, 1, (byte) -1), Byte.MIN_VALUE, (byte) -1,
			Byte.MAX_VALUE, (byte) 0);
		Assert.array(ArrayUtil.shorts.insert(shorts, 1, (short) -1), Short.MIN_VALUE, (short) -1,
			Short.MAX_VALUE, (short) 0);
		Assert.array(ArrayUtil.ints.insert(ints, 1, -1), Integer.MIN_VALUE, -1, Integer.MAX_VALUE,
			0);
		Assert.array(ArrayUtil.longs.insert(longs, 1, -1), Long.MIN_VALUE, -1L, Long.MAX_VALUE, 0L);
		Assert.array(ArrayUtil.floats.insert(floats, 1, -1), Float.MIN_VALUE, -1f, Float.MAX_VALUE,
			0f);
		Assert.array(ArrayUtil.doubles.insert(doubles, 1, -1), Double.MIN_VALUE, -1.0,
			Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldDetermineIfElementsAreContained() {
		Assert.equal(ArrayUtil.bools.contains(bools, bools), true);
		Assert.equal(ArrayUtil.chars.contains(chars, chars), true);
		Assert.equal(ArrayUtil.bytes.contains(bytes, bytes), true);
		Assert.equal(ArrayUtil.shorts.contains(shorts, shorts), true);
		Assert.equal(ArrayUtil.ints.contains(ints, ints), true);
		Assert.equal(ArrayUtil.longs.contains(longs, longs), true);
		Assert.equal(ArrayUtil.floats.contains(floats, floats), true);
		Assert.equal(ArrayUtil.doubles.contains(doubles, doubles), true);
	}

	@Test
	public void shouldDetermineIndexOfElements() {
		Assert.equal(ArrayUtil.bools.indexOf(bools, bools), 0);
		Assert.equal(ArrayUtil.chars.indexOf(chars, chars), 0);
		Assert.equal(ArrayUtil.bytes.indexOf(bytes, bytes), 0);
		Assert.equal(ArrayUtil.shorts.indexOf(shorts, shorts), 0);
		Assert.equal(ArrayUtil.ints.indexOf(ints, ints), 0);
		Assert.equal(ArrayUtil.longs.indexOf(longs, longs), 0);
		Assert.equal(ArrayUtil.floats.indexOf(floats, floats), 0);
		Assert.equal(ArrayUtil.doubles.indexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldDetermineLastIndexOfElements() {
		Assert.equal(ArrayUtil.bools.lastIndexOf(bools, bools), 0);
		Assert.equal(ArrayUtil.chars.lastIndexOf(chars, chars), 0);
		Assert.equal(ArrayUtil.bytes.lastIndexOf(bytes, bytes), 0);
		Assert.equal(ArrayUtil.shorts.lastIndexOf(shorts, shorts), 0);
		Assert.equal(ArrayUtil.ints.lastIndexOf(ints, ints), 0);
		Assert.equal(ArrayUtil.longs.lastIndexOf(longs, longs), 0);
		Assert.equal(ArrayUtil.floats.lastIndexOf(floats, floats), 0);
		Assert.equal(ArrayUtil.doubles.lastIndexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldFillElements() {
		Assert.array(ArrayUtil.bools.fill(new boolean[3], true), true, true, true);
		Assert.array(ArrayUtil.chars.fill(new char[3], (char) -1), (char) -1, (char) -1, (char) -1);
		Assert.array(ArrayUtil.bytes.fill(new byte[3], (byte) -1), (byte) -1, (byte) -1, (byte) -1);
		Assert.array(ArrayUtil.shorts.fill(new short[3], (short) -1), (short) -1, (short) -1,
			(short) -1);
		Assert.array(ArrayUtil.ints.fill(new int[3], -1), -1, -1, -1);
		Assert.array(ArrayUtil.longs.fill(new long[3], -1), -1L, -1L, -1L);
		Assert.array(ArrayUtil.floats.fill(new float[3], -1), -1f, -1f, -1f);
		Assert.array(ArrayUtil.doubles.fill(new double[3], -1), -1.0, -1.0, -1.0);
	}

	@Test
	public void shouldReverseElements() {
		Assert.array(ArrayUtil.bools.reverse(bools.clone()), true, false, true);
		Assert.array(ArrayUtil.chars.reverse(chars.clone()), 'c', '\0', 'a');
		Assert.array(ArrayUtil.bytes.reverse(bytes.clone()), 0, Byte.MAX_VALUE, Byte.MIN_VALUE);
		Assert.array(ArrayUtil.shorts.reverse(shorts.clone()), 0, Short.MAX_VALUE, Short.MIN_VALUE);
		Assert.array(ArrayUtil.ints.reverse(ints.clone()), 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
		Assert.array(ArrayUtil.longs.reverse(longs.clone()), 0, Long.MAX_VALUE, Long.MIN_VALUE);
		Assert.array(ArrayUtil.floats.reverse(floats.clone()), 0, Float.MAX_VALUE, Float.MIN_VALUE);
		Assert.array(ArrayUtil.doubles.reverse(doubles.clone()), 0, Double.MAX_VALUE,
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
		Assert.array(ArrayUtil.bools.sort(bools.clone()), false, true, true);
		Assert.array(ArrayUtil.chars.sort(chars.clone()), '\0', 'a', 'c');
		Assert.array(ArrayUtil.bytes.sort(bytes.clone()), Byte.MIN_VALUE, 0, Byte.MAX_VALUE);
		Assert.array(ArrayUtil.shorts.sort(shorts.clone()), Short.MIN_VALUE, 0, Short.MAX_VALUE);
		Assert.array(ArrayUtil.ints.sort(ints.clone()), Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		Assert.array(ArrayUtil.longs.sort(longs.clone()), Long.MIN_VALUE, 0, Long.MAX_VALUE);
		Assert.array(ArrayUtil.floats.sort(floats.clone()), 0, Float.MIN_VALUE, Float.MAX_VALUE);
		Assert.array(ArrayUtil.doubles.sort(doubles.clone()), 0, Double.MIN_VALUE,
			Double.MAX_VALUE);
	}

	@Test
	public void shouldHashElements() {
		Assert.equal(ArrayUtil.bools.hash(bools), Arrays.hashCode(bools));
		Assert.equal(ArrayUtil.chars.hash(chars), Arrays.hashCode(chars));
		Assert.equal(ArrayUtil.bytes.hash(bytes), Arrays.hashCode(bytes));
		Assert.equal(ArrayUtil.shorts.hash(shorts), Arrays.hashCode(shorts));
		Assert.equal(ArrayUtil.ints.hash(ints), Arrays.hashCode(ints));
		Assert.equal(ArrayUtil.longs.hash(longs), Arrays.hashCode(longs));
		Assert.equal(ArrayUtil.floats.hash(floats), Arrays.hashCode(floats));
		Assert.equal(ArrayUtil.doubles.hash(doubles), Arrays.hashCode(doubles));
	}

	@Test
	public void shouldDetermineElementEquality() {
		Assert.equal(ArrayUtil.bools.equals(bools, true, false, true), true);
		Assert.equal(ArrayUtil.chars.equals(chars, 'a', '\0', 'c'), true);
		Assert.equal(ArrayUtil.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0), true);
		Assert.equal(ArrayUtil.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, (short) 0),
			true);
		Assert.equal(ArrayUtil.ints.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE, 0), true);
		Assert.equal(ArrayUtil.longs.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE, 0L), true);
		Assert.equal(ArrayUtil.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, 0f), true);
		Assert.equal(ArrayUtil.doubles.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE, 0.0),
			true);
	}

	@Test
	public void shouldDetermineElementInEquality() {
		Assert.equal(ArrayUtil.bools.equals(bools, true, false), false);
		Assert.equal(ArrayUtil.bools.equals(bools, true, false), false);
		Assert.equal(ArrayUtil.bools.equals(bools, true, false, false), false);
		Assert.equal(ArrayUtil.chars.equals(chars, 'a', '\0'), false);
		Assert.equal(ArrayUtil.chars.equals(chars, 'a', '\0', 'd'), false);
		Assert.equal(ArrayUtil.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE), false);
		Assert.equal(ArrayUtil.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 1),
			false);
		Assert.equal(ArrayUtil.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE), false);
		Assert.equal(ArrayUtil.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1),
			false);
		Assert.equal(ArrayUtil.ints.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE), false);
		Assert.equal(ArrayUtil.ints.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), false);
		Assert.equal(ArrayUtil.longs.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE), false);
		Assert.equal(ArrayUtil.longs.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE, 1L), false);
		Assert.equal(ArrayUtil.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE), false);
		Assert.equal(ArrayUtil.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, .1f), false);
		Assert.equal(ArrayUtil.doubles.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE), false);
		Assert.equal(ArrayUtil.doubles.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE, .1),
			false);
	}

	@Test
	public void shouldDetermineElementEquivalence() {
		Assert.equal(ArrayUtil.chars.equals(null, (int[]) null), true);
		Assert.equal(ArrayUtil.chars.equals(chars, 'a', 0, 'c'), true);
		Assert.equal(ArrayUtil.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, 0), true);
		Assert.equal(ArrayUtil.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, 0), true);
		Assert.equal(ArrayUtil.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, 0.0), true);
	}

	@Test
	public void shouldDetermineElementInEquivalence() {
		Assert.equal(ArrayUtil.chars.equals(null, 0), false);
		Assert.equal(ArrayUtil.chars.equals(chars, (int[]) null), false);
		Assert.equal(ArrayUtil.chars.equals(chars, 'a', 0, 'c', 0), false);
		Assert.equal(ArrayUtil.chars.equals(chars, 'a', 1, 'c'), false);
		Assert.equal(ArrayUtil.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, 1), false);
		Assert.equal(ArrayUtil.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, 1), false);
		Assert.equal(ArrayUtil.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, .1), false);
	}

	@Test
	public void shouldProvideHexString() {
		Assert.equal(ArrayUtil.chars.toHex(chars), "[0x61, 0x0, 0x63]");
		Assert.equal(ArrayUtil.bytes.toHex(bytes), "[0x80, 0x7f, 0x0]");
		Assert.equal(ArrayUtil.shorts.toHex(shorts), "[0x8000, 0x7fff, 0x0]");
		Assert.equal(ArrayUtil.ints.toHex(ints), "[0x80000000, 0x7fffffff, 0x0]");
		Assert.equal(ArrayUtil.longs.toHex(longs), "[0x8000000000000000, 0x7fffffffffffffff, 0x0]");
	}

	@SafeVarargs
	private static <T> void assertBoxed(T[] result, T... expected) {
		Assert.array(result, expected);
	}
}
