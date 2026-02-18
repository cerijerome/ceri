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
		Assert.array(Array.BOOL.of(bools), bools);
		Assert.array(Array.BOOL.of(bools), bools);
		Assert.array(Array.CHAR.of(chars), chars);
		Assert.array(Array.BYTE.of(bytes), bytes);
		Assert.array(Array.SHORT.of(shorts), shorts);
		Assert.array(Array.INT.of(ints), ints);
		Assert.array(Array.LONG.of(longs), longs);
		Assert.array(Array.FLOAT.of(floats), floats);
		Assert.array(Array.DOUBLE.of(doubles), doubles);
	}

	@Test
	public void shouldCreateRange() {
		Assert.array(Array.INT.range(5, 0));
		Assert.array(Array.INT.range(-1, 3), -1, 0, 1);
		Assert.array(Array.LONG.range(5, 0));
		Assert.array(Array.LONG.range(-1, 3), -1L, 0L, 1L);
	}

	@Test
	public void shouldBoxElements() {
		assertBoxed(Array.BOOL.boxed(bools), true, false, true);
		assertBoxed(Array.CHAR.boxed(chars), 'a', '\0', 'c');
		assertBoxed(Array.BYTE.boxed(bytes), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0);
		assertBoxed(Array.BYTE.boxed(0x80, 0x7f, 0), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0);
		assertBoxed(Array.SHORT.boxed(shorts), Short.MIN_VALUE, Short.MAX_VALUE, (short) 0);
		assertBoxed(Array.SHORT.boxed(0x8000, 0x7fff, 0), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0);
		assertBoxed(Array.INT.boxed(ints), Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		assertBoxed(Array.LONG.boxed(longs), Long.MIN_VALUE, Long.MAX_VALUE, 0L);
		assertBoxed(Array.FLOAT.boxed(floats), Float.MIN_VALUE, Float.MAX_VALUE, 0f);
		assertBoxed(Array.FLOAT.boxed(Float.MIN_VALUE, Float.MAX_VALUE, 0.0), Float.MIN_VALUE,
			Float.MAX_VALUE, 0f);
		assertBoxed(Array.DOUBLE.boxed(doubles), Double.MIN_VALUE, Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldUnboxElements() {
		Assert.array(Array.BOOL.unboxed(Array.of(true, false, true)), bools);
		Assert.array(Array.CHAR.unboxed(Array.of('a', '\0', 'c')), chars);
	}

	@Test
	public void shouldUnboxLists() {
		Assert.array(Array.BOOL.unboxed(List.of(true, false, true), 1), false, true);
		Assert.array(Array.CHAR.unboxed(List.of('a', '\0', 'c'), 1), '\0', 'c');
	}

	@Test
	public void shouldProvideElementAtIndex() {
		Assert.equal(Array.BOOL.at(bools, 1, true), false);
		Assert.equal(Array.BOOL.at(bools, -1, true), true);
		Assert.equal(Array.CHAR.at(chars, 1, -1), '\0');
		Assert.equal(Array.CHAR.at(chars, -1, -1), (char) -1);
		Assert.equal(Array.BYTE.at(bytes, 1, -1), Byte.MAX_VALUE);
		Assert.equal(Array.BYTE.at(bytes, -1, -1), (byte) -1);
		Assert.equal(Array.SHORT.at(shorts, 1, -1), Short.MAX_VALUE);
		Assert.equal(Array.SHORT.at(shorts, -1, -1), (short) -1);
		Assert.equal(Array.INT.at(ints, 1, -1), Integer.MAX_VALUE);
		Assert.equal(Array.INT.at(ints, -1, -1), -1);
		Assert.equal(Array.LONG.at(longs, 1, -1), Long.MAX_VALUE);
		Assert.equal(Array.LONG.at(longs, -1, -1), -1L);
		Assert.equal(Array.FLOAT.at(floats, 1, -1), Float.MAX_VALUE);
		Assert.equal(Array.FLOAT.at(floats, -1, -1.0), -1f);
		Assert.equal(Array.DOUBLE.at(doubles, 1, -1), Double.MAX_VALUE);
		Assert.equal(Array.DOUBLE.at(doubles, -1, -1), -1.0);
	}

	@Test
	public void shouldProvideLastElement() {
		Assert.equal(Array.BOOL.last(bools, false), true);
		Assert.equal(Array.BOOL.last(Array.BOOL.empty, false), false);
		Assert.equal(Array.CHAR.last(chars, -1), 'c');
		Assert.equal(Array.CHAR.last(Array.CHAR.empty, -1), (char) -1);
		Assert.equal(Array.BYTE.last(bytes, -1), (byte) 0);
		Assert.equal(Array.BYTE.last(Array.BYTE.empty, -1), (byte) -1);
		Assert.equal(Array.SHORT.last(shorts, -1), (short) 0);
		Assert.equal(Array.SHORT.last(Array.SHORT.empty, -1), (short) -1);
		Assert.equal(Array.INT.last(ints, -1), 0);
		Assert.equal(Array.INT.last(Array.INT.empty, -1), -1);
		Assert.equal(Array.LONG.last(longs, -1), 0L);
		Assert.equal(Array.LONG.last(Array.LONG.empty, -1), -1L);
		Assert.equal(Array.FLOAT.last(floats, -1), 0f);
		Assert.equal(Array.FLOAT.last(Array.FLOAT.empty, -1.0), -1f);
		Assert.equal(Array.DOUBLE.last(doubles, -1), 0.0);
		Assert.equal(Array.DOUBLE.last(Array.DOUBLE.empty, -1), -1.0);
	}

	@Test
	public void shouldAppendElements() {
		Assert.array(Array.BOOL.append(bools, false), true, false, true, false);
		Assert.array(Array.CHAR.append(chars, (char) -1), 'a', '\0', 'c', (char) -1);
		Assert.array(Array.BYTE.append(bytes, (byte) -1), Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0,
			(byte) -1);
		Assert.array(Array.SHORT.append(shorts, (short) -1), Short.MIN_VALUE, Short.MAX_VALUE,
			(short) 0, (short) -1);
		Assert.array(Array.INT.append(ints, -1), Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1);
		Assert.array(Array.LONG.append(longs, -1), Long.MIN_VALUE, Long.MAX_VALUE, 0L, -1L);
		Assert.array(Array.FLOAT.append(floats, -1), Float.MIN_VALUE, Float.MAX_VALUE, 0f, -1f);
		Assert.array(Array.DOUBLE.append(doubles, -1), Double.MIN_VALUE, Double.MAX_VALUE, 0.0,
			-1.0);
	}

	@Test
	public void shouldInsertElements() {
		Assert.array(Array.BOOL.insert(bools, 1, false), true, false, false, true);
		Assert.array(Array.CHAR.insert(chars, 1, (char) -1), 'a', (char) -1, '\0', 'c');
		Assert.array(Array.BYTE.insert(bytes, 1, (byte) -1), Byte.MIN_VALUE, (byte) -1,
			Byte.MAX_VALUE, (byte) 0);
		Assert.array(Array.SHORT.insert(shorts, 1, (short) -1), Short.MIN_VALUE, (short) -1,
			Short.MAX_VALUE, (short) 0);
		Assert.array(Array.INT.insert(ints, 1, -1), Integer.MIN_VALUE, -1, Integer.MAX_VALUE, 0);
		Assert.array(Array.LONG.insert(longs, 1, -1), Long.MIN_VALUE, -1L, Long.MAX_VALUE, 0L);
		Assert.array(Array.FLOAT.insert(floats, 1, -1), Float.MIN_VALUE, -1f, Float.MAX_VALUE, 0f);
		Assert.array(Array.DOUBLE.insert(doubles, 1, -1), Double.MIN_VALUE, -1.0, Double.MAX_VALUE,
			0.0);
	}

	@Test
	public void shouldDetermineIfElementsAreContained() {
		Assert.equal(Array.BOOL.contains(bools, bools), true);
		Assert.equal(Array.CHAR.contains(chars, chars), true);
		Assert.equal(Array.BYTE.contains(bytes, bytes), true);
		Assert.equal(Array.SHORT.contains(shorts, shorts), true);
		Assert.equal(Array.INT.contains(ints, ints), true);
		Assert.equal(Array.LONG.contains(longs, longs), true);
		Assert.equal(Array.FLOAT.contains(floats, floats), true);
		Assert.equal(Array.DOUBLE.contains(doubles, doubles), true);
	}

	@Test
	public void shouldDetermineIndexOfElements() {
		Assert.equal(Array.BOOL.indexOf(bools, bools), 0);
		Assert.equal(Array.CHAR.indexOf(chars, chars), 0);
		Assert.equal(Array.BYTE.indexOf(bytes, bytes), 0);
		Assert.equal(Array.SHORT.indexOf(shorts, shorts), 0);
		Assert.equal(Array.INT.indexOf(ints, ints), 0);
		Assert.equal(Array.LONG.indexOf(longs, longs), 0);
		Assert.equal(Array.FLOAT.indexOf(floats, floats), 0);
		Assert.equal(Array.DOUBLE.indexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldDetermineLastIndexOfElements() {
		Assert.equal(Array.BOOL.lastIndexOf(bools, bools), 0);
		Assert.equal(Array.CHAR.lastIndexOf(chars, chars), 0);
		Assert.equal(Array.BYTE.lastIndexOf(bytes, bytes), 0);
		Assert.equal(Array.SHORT.lastIndexOf(shorts, shorts), 0);
		Assert.equal(Array.INT.lastIndexOf(ints, ints), 0);
		Assert.equal(Array.LONG.lastIndexOf(longs, longs), 0);
		Assert.equal(Array.FLOAT.lastIndexOf(floats, floats), 0);
		Assert.equal(Array.DOUBLE.lastIndexOf(doubles, doubles), 0);
	}

	@Test
	public void shouldFillElements() {
		Assert.array(Array.BOOL.fill(new boolean[3], true), true, true, true);
		Assert.array(Array.CHAR.fill(new char[3], (char) -1), (char) -1, (char) -1, (char) -1);
		Assert.array(Array.BYTE.fill(new byte[3], (byte) -1), (byte) -1, (byte) -1, (byte) -1);
		Assert.array(Array.SHORT.fill(new short[3], (short) -1), (short) -1, (short) -1,
			(short) -1);
		Assert.array(Array.INT.fill(new int[3], -1), -1, -1, -1);
		Assert.array(Array.LONG.fill(new long[3], -1), -1L, -1L, -1L);
		Assert.array(Array.FLOAT.fill(new float[3], -1), -1f, -1f, -1f);
		Assert.array(Array.DOUBLE.fill(new double[3], -1), -1.0, -1.0, -1.0);
	}

	@Test
	public void shouldReverseElements() {
		Assert.array(Array.BOOL.reverse(bools.clone()), true, false, true);
		Assert.array(Array.CHAR.reverse(chars.clone()), 'c', '\0', 'a');
		Assert.array(Array.BYTE.reverse(bytes.clone()), 0, Byte.MAX_VALUE, Byte.MIN_VALUE);
		Assert.array(Array.SHORT.reverse(shorts.clone()), 0, Short.MAX_VALUE, Short.MIN_VALUE);
		Assert.array(Array.INT.reverse(ints.clone()), 0, Integer.MAX_VALUE, Integer.MIN_VALUE);
		Assert.array(Array.LONG.reverse(longs.clone()), 0, Long.MAX_VALUE, Long.MIN_VALUE);
		Assert.array(Array.FLOAT.reverse(floats.clone()), 0, Float.MAX_VALUE, Float.MIN_VALUE);
		Assert.array(Array.DOUBLE.reverse(doubles.clone()), 0, Double.MAX_VALUE, Double.MIN_VALUE);
	}

	@Test
	public void shouldConsumeElements() {
		Captor.of().apply(c -> Array.BOOL.forEach(bools, c::accept)).verify(true, false, true);
		Captor.of().apply(c -> Array.CHAR.forEach(chars, c::accept)).verify(97, 0, 99);
		Captor.of().apply(c -> Array.BYTE.forEach(bytes, c::accept)).verify(-0x80, 0x7f, 0);
		Captor.of().apply(c -> Array.SHORT.forEach(shorts, c::accept)).verify(-0x8000, 0x7fff, 0);
		Captor.of().apply(c -> Array.INT.forEach(ints, c::accept)).verify(Integer.MIN_VALUE,
			Integer.MAX_VALUE, 0);
		Captor.of().apply(c -> Array.LONG.forEach(longs, c::accept)).verify(Long.MIN_VALUE,
			Long.MAX_VALUE, 0L);
		Captor.of().apply(c -> Array.FLOAT.forEach(floats, c::accept))
			.verify((double) Float.MIN_VALUE, (double) Float.MAX_VALUE, 0.0);
		Captor.of().apply(c -> Array.DOUBLE.forEach(doubles, c::accept)).verify(Double.MIN_VALUE,
			Double.MAX_VALUE, 0.0);
	}

	@Test
	public void shouldConsumeBoxedElements() {
		Captor.of().apply(c -> Array.BOOL.forEachBox(bools, c::accept)).verify(true, false, true);
		Captor.of().apply(c -> Array.CHAR.forEachBox(chars, c::accept)).verify('a', '\0', 'c');
	}

	@Test
	public void shouldConsumeIndexedBoxedElements() {
		Captor.ofBi().apply(c -> Array.BOOL.forEachBoxIndexed(bools, c::accept)).verify(true, 0,
			false, 1, true, 2);
		Captor.ofBi().apply(c -> Array.CHAR.forEachBoxIndexed(chars, c::accept)).verify('a', 0,
			'\0', 1, 'c', 2);
	}

	@Test
	public void shouldSortElements() {
		Assert.array(Array.BOOL.sort(bools.clone()), false, true, true);
		Assert.array(Array.CHAR.sort(chars.clone()), '\0', 'a', 'c');
		Assert.array(Array.BYTE.sort(bytes.clone()), Byte.MIN_VALUE, 0, Byte.MAX_VALUE);
		Assert.array(Array.SHORT.sort(shorts.clone()), Short.MIN_VALUE, 0, Short.MAX_VALUE);
		Assert.array(Array.INT.sort(ints.clone()), Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		Assert.array(Array.LONG.sort(longs.clone()), Long.MIN_VALUE, 0, Long.MAX_VALUE);
		Assert.array(Array.FLOAT.sort(floats.clone()), 0, Float.MIN_VALUE, Float.MAX_VALUE);
		Assert.array(Array.DOUBLE.sort(doubles.clone()), 0, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	@Test
	public void shouldHashElements() {
		Assert.equal(Array.BOOL.hash(bools), Arrays.hashCode(bools));
		Assert.equal(Array.CHAR.hash(chars), Arrays.hashCode(chars));
		Assert.equal(Array.BYTE.hash(bytes), Arrays.hashCode(bytes));
		Assert.equal(Array.SHORT.hash(shorts), Arrays.hashCode(shorts));
		Assert.equal(Array.INT.hash(ints), Arrays.hashCode(ints));
		Assert.equal(Array.LONG.hash(longs), Arrays.hashCode(longs));
		Assert.equal(Array.FLOAT.hash(floats), Arrays.hashCode(floats));
		Assert.equal(Array.DOUBLE.hash(doubles), Arrays.hashCode(doubles));
	}

	@Test
	public void shouldDetermineElementEquality() {
		Assert.equal(Array.BOOL.equals(bools, true, false, true), true);
		Assert.equal(Array.CHAR.equals(chars, 'a', '\0', 'c'), true);
		Assert.equal(Array.BYTE.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 0), true);
		Assert.equal(Array.SHORT.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, (short) 0),
			true);
		Assert.equal(Array.INT.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE, 0), true);
		Assert.equal(Array.LONG.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE, 0L), true);
		Assert.equal(Array.FLOAT.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, 0f), true);
		Assert.equal(Array.DOUBLE.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE, 0.0), true);
	}

	@Test
	public void shouldDetermineElementInEquality() {
		Assert.equal(Array.BOOL.equals(bools, true, false), false);
		Assert.equal(Array.BOOL.equals(bools, true, false), false);
		Assert.equal(Array.BOOL.equals(bools, true, false, false), false);
		Assert.equal(Array.CHAR.equals(chars, 'a', '\0'), false);
		Assert.equal(Array.CHAR.equals(chars, 'a', '\0', 'd'), false);
		Assert.equal(Array.BYTE.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE), false);
		Assert.equal(Array.BYTE.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) 1), false);
		Assert.equal(Array.SHORT.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE), false);
		Assert.equal(Array.SHORT.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1),
			false);
		Assert.equal(Array.INT.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE), false);
		Assert.equal(Array.INT.equals(ints, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), false);
		Assert.equal(Array.LONG.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE), false);
		Assert.equal(Array.LONG.equals(longs, Long.MIN_VALUE, Long.MAX_VALUE, 1L), false);
		Assert.equal(Array.FLOAT.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE), false);
		Assert.equal(Array.FLOAT.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, .1f), false);
		Assert.equal(Array.DOUBLE.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE), false);
		Assert.equal(Array.DOUBLE.equals(doubles, Double.MIN_VALUE, Double.MAX_VALUE, .1), false);
	}

	@Test
	public void shouldDetermineElementEquivalence() {
		Assert.equal(Array.CHAR.equals(null, (int[]) null), true);
		Assert.equal(Array.CHAR.equals(chars, 'a', 0, 'c'), true);
		Assert.equal(Array.BYTE.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, 0), true);
		Assert.equal(Array.SHORT.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, 0), true);
		Assert.equal(Array.FLOAT.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, 0.0), true);
	}

	@Test
	public void shouldDetermineElementInEquivalence() {
		Assert.equal(Array.CHAR.equals(null, 0), false);
		Assert.equal(Array.CHAR.equals(chars, (int[]) null), false);
		Assert.equal(Array.CHAR.equals(chars, 'a', 0, 'c', 0), false);
		Assert.equal(Array.CHAR.equals(chars, 'a', 1, 'c'), false);
		Assert.equal(Array.BYTE.equals(bytes, Byte.MIN_VALUE, Byte.MAX_VALUE, 1), false);
		Assert.equal(Array.SHORT.equals(shorts, Short.MIN_VALUE, Short.MAX_VALUE, 1), false);
		Assert.equal(Array.FLOAT.equals(floats, Float.MIN_VALUE, Float.MAX_VALUE, .1), false);
	}

	@Test
	public void shouldProvideHexString() {
		Assert.equal(Array.CHAR.toHex(chars), "[0x61, 0x0, 0x63]");
		Assert.equal(Array.BYTE.toHex(bytes), "[0x80, 0x7f, 0x0]");
		Assert.equal(Array.SHORT.toHex(shorts), "[0x8000, 0x7fff, 0x0]");
		Assert.equal(Array.INT.toHex(ints), "[0x80000000, 0x7fffffff, 0x0]");
		Assert.equal(Array.LONG.toHex(longs), "[0x8000000000000000, 0x7fffffffffffffff, 0x0]");
	}

	@Test
	public void shouldProvideBoxedHexStrings() {
		Assert.equal(Array.CHAR.box().toHex(Array.CHAR.boxed(chars)), "[0x61, 0x0, 0x63]");
		Assert.equal(Array.BYTE.box().toHex(Array.BYTE.boxed(bytes)), "[0x80, 0x7f, 0x0]");
		Assert.equal(Array.SHORT.box().toHex(Array.SHORT.boxed(shorts)), "[0x8000, 0x7fff, 0x0]");
		Assert.equal(Array.INT.box().toHex(Array.INT.boxed(ints)),
			"[0x80000000, 0x7fffffff, 0x0]");
		Assert.equal(Array.LONG.box().toHex(Array.LONG.boxed(longs)),
			"[0x8000000000000000, 0x7fffffffffffffff, 0x0]");
	}

	@SafeVarargs
	private static <T> void assertBoxed(T[] result, T... expected) {
		Assert.array(result, expected);
	}
}
