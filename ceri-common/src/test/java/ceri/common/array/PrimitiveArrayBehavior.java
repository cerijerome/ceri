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
		Assert.array(Array.bools.of(bools), bools);
		Assert.array(Array.bools.of(bools), bools);
		Assert.array(Array.chars.of(chars), chars);
		Assert.array(Array.bytes.of(bytes), bytes);
		Assert.array(Array.shorts.of(shorts), shorts);
		Assert.array(Array.ints.of(ints), ints);
		Assert.array(Array.longs.of(longs), longs);
		Assert.array(Array.floats.of(floats), floats);
		Assert.array(Array.doubles.of(doubles), doubles);
	}

	@Test
	public void shouldCreateRange() {
		Assert.array(Array.ints.range(5, 0));
		Assert.array(Array.ints.range(-1, 3), -1, 0, 1);
		Assert.array(Array.longs.range(5, 0));
		Assert.array(Array.longs.range(-1, 3), -1L, 0L, 1L);
	}

	@Test
	public void shouldBoxElements() {
		assertBoxed(Array.bools.boxed(bools), true, false, true);
		assertBoxed(Array.chars.boxed(chars), 'a', '\0', 'c');
		assertBoxed(Array.bytes.boxed(bytes), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0);
		assertBoxed(Array.bytes.boxed(0x80, 0x7f, 0), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0);
		assertBoxed(Array.shorts.boxed(shorts), Short.MIN_VALUE, Short.MAX_VALUE, (short) 0);
		assertBoxed(Array.shorts.boxed(0x8000, 0x7fff, 0), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0);
		assertBoxed(Array.ints.boxed(ints), Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		assertBoxed(Array.longs.boxed(longs), Long.MIN_VALUE, Long.MAX_VALUE, 0L);
		assertBoxed(Array.floats.boxed(floats), Float.MIN_VALUE, Float.MAX_VALUE, 0f);
		assertBoxed(Array.floats.boxed(Float.MIN_VALUE, Float.MAX_VALUE, 0.0), Float.MIN_VALUE,
			Float.MAX_VALUE, 0f);
		assertBoxed(Array.doubles.boxed(doubles), Double.MIN_VALUE, Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldUnboxElements() {
		Assert.array(Array.bools.unboxed(Array.of(true, false, true)), bools);
		Assert.array(Array.chars.unboxed(Array.of('a', '\0', 'c')), chars);
	}

	@Test
	public void shouldUnboxLists() {
		Assert.array(Array.bools.unboxed(List.of(true, false, true), 1), false, true);
		Assert.array(Array.chars.unboxed(List.of('a', '\0', 'c'), 1), '\0', 'c');
	}

	@Test
	public void shouldProvideElementAtIndex() {
		Assert.equal(Array.bools.at(bools, 1, true), false);
		Assert.equal(Array.bools.at(bools, -1, true), true);
		Assert.equal(Array.chars.at(chars, 1, -1), '\0');
		Assert.equal(Array.chars.at(chars, -1, -1), (char) -1);
		Assert.equal(Array.bytes.at(bytes, 1, -1), Byte.MAX_VALUE);
		Assert.equal(Array.bytes.at(bytes, -1, -1), (byte) -1);
		Assert.equal(Array.shorts.at(shorts, 1, -1), Short.MAX_VALUE);
		Assert.equal(Array.shorts.at(shorts, -1, -1), (short) -1);
		Assert.equal(Array.ints.at(ints, 1, -1), Integer.MAX_VALUE);
		Assert.equal(Array.ints.at(ints, -1, -1), -1);
		Assert.equal(Array.longs.at(longs, 1, -1), Long.MAX_VALUE);
		Assert.equal(Array.longs.at(longs, -1, -1), -1L);
		Assert.equal(Array.floats.at(floats, 1, -1), Float.MAX_VALUE);
		Assert.equal(Array.floats.at(floats, -1, -1.0), -1f);
		Assert.equal(Array.doubles.at(doubles, 1, -1), Double.MAX_VALUE);
		Assert.equal(Array.doubles.at(doubles, -1, -1), -1.0);
	}

	@Test
	public void shouldProvideLastElement() {
		Assert.equal(Array.bools.last(bools, false), true);
		Assert.equal(Array.bools.last(Array.bools.empty, false), false);
		Assert.equal(Array.chars.last(chars, -1), 'c');
		Assert.equal(Array.chars.last(Array.chars.empty, -1), (char) -1);
		Assert.equal(Array.bytes.last(bytes, -1), (byte) 0);
		Assert.equal(Array.bytes.last(Array.bytes.empty, -1), (byte) -1);
		Assert.equal(Array.shorts.last(shorts, -1), (short) 0);
		Assert.equal(Array.shorts.last(Array.shorts.empty, -1), (short) -1);
		Assert.equal(Array.ints.last(ints, -1), 0);
		Assert.equal(Array.ints.last(Array.ints.empty, -1), -1);
		Assert.equal(Array.longs.last(longs, -1), 0L);
		Assert.equal(Array.longs.last(Array.longs.empty, -1), -1L);
		Assert.equal(Array.floats.last(floats, -1), 0f);
		Assert.equal(Array.floats.last(Array.floats.empty, -1.0), -1f);
		Assert.equal(Array.doubles.last(doubles, -1), 0.0);
		Assert.equal(Array.doubles.last(Array.doubles.empty, -1), -1.0);
	}

	@Test
	public void shouldAppendElements() {
		Assert.array(Array.bools.append(bools, false), true, false, true, false);
		Assert.array(Array.chars.append(chars, (char) -1), 'a', '\0', 'c', (char) -1);
		Assert.array(Array.bytes.append(bytes, (byte) -1), Byte.MIN_VALUE, Byte.MAX_VALUE,
			(byte) 0, (byte) -1);
		Assert.array(Array.shorts.append(shorts, (short) -1), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0, (short) -1);
		Assert.array(Array.ints.append(ints, -1), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1);
		Assert.array(Array.longs.append(longs, -1), Long.MIN_VALUE, Long.MAX_VALUE, 0L, -1L);
		Assert.array(Array.floats.append(floats, -1), Float.MIN_VALUE, Float.MAX_VALUE, 0f,
			-1f);
		Assert.array(Array.doubles.append(doubles, -1), Double.MIN_VALUE, Double.MAX_VALUE, 0.0,
			-1.0);
	}

	@Test
	public void shouldInsertElements() {
		Assert.array(Array.bools.insert(bools, 1, false), true, false, false, true);
		Assert.array(Array.chars.insert(chars, 1, (char) -1), 'a', (char) -1, '\0', 'c');
		Assert.array(Array.bytes.insert(bytes, 1, (byte) -1), Byte.MIN_VALUE, (byte) -1,
			Byte.MAX_VALUE, (byte) 0);
		Assert.array(Array.shorts.insert(shorts, 1, (short) -1), Short.MIN_VALUE, (short) -1,
			Short.MAX_VALUE, (short) 0);
		Assert.array(Array.ints.insert(ints, 1, -1), Integer.MIN_VALUE, -1, Integer.MAX_VALUE,
			0);
		Assert.array(Array.longs.insert(longs, 1, -1), Long.MIN_VALUE, -1L, Long.MAX_VALUE, 0L);
		Assert.array(Array.floats.insert(floats, 1, -1), Float.MIN_VALUE, -1f, Float.MAX_VALUE,
			0f);
		Assert.array(Array.doubles.insert(doubles, 1, -1), Double.MIN_VALUE, -1.0,
			Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldDetermineIfElementsAreContained() {
		Assert.equal(Array.bools.contains(bools, bools), true);
		Assert.equal(Array.chars.contains(chars, chars), true);
		Assert.equal(Array.bytes.contains(bytes, bytes), true);
		Assert.equal(Array.shorts.contains(shorts, shorts), true);
		Assert.equal(Array.ints.contains(ints, ints), true);
		Assert.equal(Array.longs.contains(longs, longs), true);
		Assert.equal(Array.floats.contains(floats, floats), true);
		Assert.equal(Array.doubles.contains(doubles, doubles), true);
	}

	@Test
	public void shouldDetermineIndexOfElements() {
		Assert.equal(Array.bools.indexOf(bools, bools), 0);
		Assert.equal(Array.chars.indexOf(chars, chars), 0);
		Assert.equal(Array.bytes.indexOf(bytes, bytes), 0);
		Assert.equal(Array.shorts.indexOf(shorts, shorts), 0);
		Assert.equal(Array.ints.indexOf(ints, ints), 0);
		Assert.equal(Array.longs.indexOf(longs, longs), 0);
		Assert.equal(Array.floats.indexOf(floats, floats), 0);
		Assert.equal(Array.doubles.indexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldDetermineLastIndexOfElements() {
		Assert.equal(Array.bools.lastIndexOf(bools, bools), 0);
		Assert.equal(Array.chars.lastIndexOf(chars, chars), 0);
		Assert.equal(Array.bytes.lastIndexOf(bytes, bytes), 0);
		Assert.equal(Array.shorts.lastIndexOf(shorts, shorts), 0);
		Assert.equal(Array.ints.lastIndexOf(ints, ints), 0);
		Assert.equal(Array.longs.lastIndexOf(longs, longs), 0);
		Assert.equal(Array.floats.lastIndexOf(floats, floats), 0);
		Assert.equal(Array.doubles.lastIndexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldFillElements() {
		Assert.array(Array.bools.fill(new boolean[3], true), true, true, true);
		Assert.array(Array.chars.fill(new char[3], (char) -1), (char) -1, (char) -1, (char) -1);
		Assert.array(Array.bytes.fill(new byte[3], (byte) -1), (byte) -1, (byte) -1, (byte) -1);
		Assert.array(Array.shorts.fill(new short[3], (short) -1), (short) -1, (short) -1,
			(short) -1);
		Assert.array(Array.ints.fill(new int[3], -1), -1, -1, -1);
		Assert.array(Array.longs.fill(new long[3], -1), -1L, -1L, -1L);
		Assert.array(Array.floats.fill(new float[3], -1), -1f, -1f, -1f);
		Assert.array(Array.doubles.fill(new double[3], -1), -1.0, -1.0, -1.0);
	}

	@Test
	public void shouldReverseElements() {
		Assert.array(Array.bools.reverse(bools.clone()), true, false, true);
		Assert.array(Array.chars.reverse(chars.clone()), 'c', '\0', 'a');
		Assert.array(Array.bytes.reverse(bytes.clone()), 0, Byte.MAX_VALUE, Byte.MIN_VALUE);
		Assert.array(Array.shorts.reverse(shorts.clone()), 0, Short.MAX_VALUE, Short.MIN_VALUE);
		Assert.array(Array.ints.reverse(ints.clone()), 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
		Assert.array(Array.longs.reverse(longs.clone()), 0, Long.MAX_VALUE, Long.MIN_VALUE);
		Assert.array(Array.floats.reverse(floats.clone()), 0, Float.MAX_VALUE, Float.MIN_VALUE);
		Assert.array(Array.doubles.reverse(doubles.clone()), 0, Double.MAX_VALUE,
			Double.MIN_VALUE);
	}

	@Test
	public void shouldConsumeElements() {
		Captor.of().apply(c -> Array.bools.forEach(bools, c::accept)).verify(true, false, true);
		Captor.of().apply(c -> Array.chars.forEach(chars, c::accept)).verify(97, 0, 99);
		Captor.of().apply(c -> Array.bytes.forEach(bytes, c::accept)).verify(-0x80, 0x7f, 0);
		Captor.of().apply(c -> Array.shorts.forEach(shorts, c::accept)).verify(-0x8000, 0x7fff,
			0);
		Captor.of().apply(c -> Array.ints.forEach(ints, c::accept)).verify(Integer.MIN_VALUE,
			Integer.MAX_VALUE, 0);
		Captor.of().apply(c -> Array.longs.forEach(longs, c::accept)).verify(Long.MIN_VALUE,
			Long.MAX_VALUE, 0L);
		Captor.of().apply(c -> Array.floats.forEach(floats, c::accept))
			.verify((double) Float.MIN_VALUE, (double) Float.MAX_VALUE, 0.0);
		Captor.of().apply(c -> Array.doubles.forEach(doubles, c::accept))
			.verify(Double.MIN_VALUE, Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldConsumeBoxedElements() {
		Captor.of().apply(c -> Array.bools.forEachBox(bools, c::accept)).verify(true, false,
			true);
		Captor.of().apply(c -> Array.chars.forEachBox(chars, c::accept)).verify('a', '\0', 'c');
	}

	@Test
	public void shouldConsumeIndexedBoxedElements() {
		Captor.ofBi().apply(c -> Array.bools.forEachBoxIndexed(bools, c::accept)).verify(true,
			0, false, 1, true, 2);
		Captor.ofBi().apply(c -> Array.chars.forEachBoxIndexed(chars, c::accept)).verify('a', 0,
			'\0', 1, 'c', 2);
	}

	@Test
	public void shouldSortElements() {
		Assert.array(Array.bools.sort(bools.clone()), false, true, true);
		Assert.array(Array.chars.sort(chars.clone()), '\0', 'a', 'c');
		Assert.array(Array.bytes.sort(bytes.clone()), Byte.MIN_VALUE, 0, Byte.MAX_VALUE);
		Assert.array(Array.shorts.sort(shorts.clone()), Short.MIN_VALUE, 0, Short.MAX_VALUE);
		Assert.array(Array.ints.sort(ints.clone()), Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		Assert.array(Array.longs.sort(longs.clone()), Long.MIN_VALUE, 0, Long.MAX_VALUE);
		Assert.array(Array.floats.sort(floats.clone()), 0, Float.MIN_VALUE, Float.MAX_VALUE);
		Assert.array(Array.doubles.sort(doubles.clone()), 0, Double.MIN_VALUE,
			Double.MAX_VALUE);
	}

	@Test
	public void shouldHashElements() {
		Assert.equal(Array.bools.hash(bools), Arrays.hashCode(bools));
		Assert.equal(Array.chars.hash(chars), Arrays.hashCode(chars));
		Assert.equal(Array.bytes.hash(bytes), Arrays.hashCode(bytes));
		Assert.equal(Array.shorts.hash(shorts), Arrays.hashCode(shorts));
		Assert.equal(Array.ints.hash(ints), Arrays.hashCode(ints));
		Assert.equal(Array.longs.hash(longs), Arrays.hashCode(longs));
		Assert.equal(Array.floats.hash(floats), Arrays.hashCode(floats));
		Assert.equal(Array.doubles.hash(doubles), Arrays.hashCode(doubles));
	}

	@Test
	public void shouldDetermineElementEquality() {
		Assert.equal(Array.bools.equals(bools, true, false, true), true);
		Assert.equal(Array.chars.equals(chars, 'a', '\0', 'c'), true);
		Assert.equal(Array.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0), true);
		Assert.equal(Array.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, (short) 0),
			true);
		Assert.equal(Array.ints.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE, 0), true);
		Assert.equal(Array.longs.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE, 0L), true);
		Assert.equal(Array.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, 0f), true);
		Assert.equal(Array.doubles.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE, 0.0),
			true);
	}

	@Test
	public void shouldDetermineElementInEquality() {
		Assert.equal(Array.bools.equals(bools, true, false), false);
		Assert.equal(Array.bools.equals(bools, true, false), false);
		Assert.equal(Array.bools.equals(bools, true, false, false), false);
		Assert.equal(Array.chars.equals(chars, 'a', '\0'), false);
		Assert.equal(Array.chars.equals(chars, 'a', '\0', 'd'), false);
		Assert.equal(Array.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE), false);
		Assert.equal(Array.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 1),
			false);
		Assert.equal(Array.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE), false);
		Assert.equal(Array.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1),
			false);
		Assert.equal(Array.ints.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE), false);
		Assert.equal(Array.ints.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), false);
		Assert.equal(Array.longs.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE), false);
		Assert.equal(Array.longs.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE, 1L), false);
		Assert.equal(Array.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE), false);
		Assert.equal(Array.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, .1f), false);
		Assert.equal(Array.doubles.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE), false);
		Assert.equal(Array.doubles.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE, .1),
			false);
	}

	@Test
	public void shouldDetermineElementEquivalence() {
		Assert.equal(Array.chars.equals(null, (int[]) null), true);
		Assert.equal(Array.chars.equals(chars, 'a', 0, 'c'), true);
		Assert.equal(Array.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, 0), true);
		Assert.equal(Array.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, 0), true);
		Assert.equal(Array.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, 0.0), true);
	}

	@Test
	public void shouldDetermineElementInEquivalence() {
		Assert.equal(Array.chars.equals(null, 0), false);
		Assert.equal(Array.chars.equals(chars, (int[]) null), false);
		Assert.equal(Array.chars.equals(chars, 'a', 0, 'c', 0), false);
		Assert.equal(Array.chars.equals(chars, 'a', 1, 'c'), false);
		Assert.equal(Array.bytes.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, 1), false);
		Assert.equal(Array.shorts.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, 1), false);
		Assert.equal(Array.floats.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, .1), false);
	}

	@Test
	public void shouldProvideHexString() {
		Assert.equal(Array.chars.toHex(chars), "[0x61, 0x0, 0x63]");
		Assert.equal(Array.bytes.toHex(bytes), "[0x80, 0x7f, 0x0]");
		Assert.equal(Array.shorts.toHex(shorts), "[0x8000, 0x7fff, 0x0]");
		Assert.equal(Array.ints.toHex(ints), "[0x80000000, 0x7fffffff, 0x0]");
		Assert.equal(Array.longs.toHex(longs), "[0x8000000000000000, 0x7fffffffffffffff, 0x0]");
	}

	@SafeVarargs
	private static <T> void assertBoxed(T[] result, T... expected) {
		Assert.array(result, expected);
	}
}
