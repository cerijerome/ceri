package ceri.common.math;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MathUtilTest {

	// Class to help test methods with overrides for each primitive number 
	static class Numbers {
		byte[] b = { Byte.MAX_VALUE, -1, 0, 1, Byte.MIN_VALUE };
		short[] s = { Short.MAX_VALUE, -1, 0, 1, Short.MIN_VALUE };
		int[] i = { Integer.MAX_VALUE, -1, 0, 1, Integer.MIN_VALUE };
		long[] l = { Long.MAX_VALUE, -1, 0, 1, Long.MIN_VALUE };
		double[] d = { Double.MAX_VALUE, -1, 0, 1, -Double.MAX_VALUE };
		double[] dx = { Double.POSITIVE_INFINITY, Double.NaN, Double.NEGATIVE_INFINITY };
		float[] f = { Float.MAX_VALUE, -1, 0, 1, -Float.MAX_VALUE };
		float[] fx = { Float.POSITIVE_INFINITY, Float.NaN, Float.NEGATIVE_INFINITY };
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(MathUtil.class);
	}

	@Test
	public void testSimpleRound() {
		assertThat(MathUtil.simpleRound(11111.11111, 2), is(11111.11));
		assertThat(MathUtil.simpleRound(11111.11111, 10), is(11111.11111));
		assertThat(MathUtil.simpleRound(11111.111111111111111, 2), is(11111.11));
		assertThat(MathUtil.simpleRound(777.7777, 3), is(777.778));
		assertThat(MathUtil.simpleRound(-777.7777, 3), is(-777.778));
		assertTrue(Double.isNaN(MathUtil.simpleRound(Double.NaN, 0)));
		assertException(() -> MathUtil.simpleRound(777.7777, 11));
		assertException(() -> MathUtil.simpleRound(1000000000.1, 1));
		assertException(() -> MathUtil.simpleRound(-1000000000.1, 1));
		assertException(() -> MathUtil.simpleRound(Double.POSITIVE_INFINITY, 1));
		assertException(() -> MathUtil.simpleRound(Double.NEGATIVE_INFINITY, 1));
	}

	@Test
	public void testShortToBytes() {
		byte ff = (byte) 0xff;
		byte _80 = (byte) 0x80;
		assertArrayEquals(MathUtil.shortToBytes(0xffff), new byte[] { ff, ff });
		assertArrayEquals(MathUtil.shortToBytes(0x7fff), new byte[] { 0x7f, ff });
		assertArrayEquals(MathUtil.shortToBytes(0x8000), new byte[] { _80, 0, });
		assertArrayEquals(MathUtil.shortToBytes(0x7f), new byte[] { 0, 0x7f });
		assertArrayEquals(MathUtil.shortToBytes(0x80), new byte[] { 0, _80 });
		assertArrayEquals(MathUtil.shortToBytes(0), new byte[] { 0, 0 });
	}

	@Test
	public void testIntToBytes() {
		byte ff = (byte) 0xff;
		byte _80 = (byte) 0x80;
		assertArrayEquals(MathUtil.intToBytes(0xffffffff), new byte[] { ff, ff, ff, ff });
		assertArrayEquals(MathUtil.intToBytes(0x7fffffff), new byte[] { 0x7f, ff, ff, ff });
		assertArrayEquals(MathUtil.intToBytes(0x80000000), new byte[] { _80, 0, 0, 0 });
		assertArrayEquals(MathUtil.intToBytes(0x7fffff), new byte[] { 0, 0x7f, ff, ff });
		assertArrayEquals(MathUtil.intToBytes(0x800000), new byte[] { 0, _80, 0, 0 });
		assertArrayEquals(MathUtil.intToBytes(0x7fff), new byte[] { 0, 0, 0x7f, ff });
		assertArrayEquals(MathUtil.intToBytes(0x8000), new byte[] { 0, 0, _80, 0, });
		assertArrayEquals(MathUtil.intToBytes(0x7f), new byte[] { 0, 0, 0, 0x7f });
		assertArrayEquals(MathUtil.intToBytes(0x80), new byte[] { 0, 0, 0, _80 });
		assertArrayEquals(MathUtil.intToBytes(0), new byte[] { 0, 0, 0, 0 });
	}

	@Test
	public void testMean() {
		assertException(() -> MathUtil.mean());
		assertThat(MathUtil.mean(0), is(0.0));
		assertThat(MathUtil.mean(-1, -2, 9), is(2.0));
		assertThat(MathUtil.mean(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.mean(Double.MAX_VALUE, -Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.mean(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
			is(Double.NaN));
	}

	@Test
	public void testMedian() {
		assertException(() -> MathUtil.median());
		assertThat(MathUtil.median(0), is(0.0));
		assertThat(MathUtil.median(-1, -2, 9), is(-1.0));
		assertThat(MathUtil.median(1, 2, 3, 4), is(2.5));
		assertThat(MathUtil.median(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.median(Double.MAX_VALUE, -Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.median(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
			is(Double.NaN));
	}

	@Test
	public void testCompare() {
		assertThat(MathUtil.compare(Integer.MAX_VALUE, Integer.MIN_VALUE), is(1));
		assertThat(MathUtil.compare(Integer.MIN_VALUE, Integer.MAX_VALUE), is(-1));
		assertThat(MathUtil.compare(Integer.MIN_VALUE, Integer.MIN_VALUE), is(0));
	}

	@Test
	public void testDigits() {
		assertThat(MathUtil.digits(123456789L), is(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
		assertThat(MathUtil.digits(153, 2), is(new byte[] { 1, 0, 0, 1, 1, 0, 0, 1 }));
		assertThat(MathUtil.digits(0x123456789ABCDEFL, 16), is(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8,
			9, 10, 11, 12, 13, 14, 15 }));
		assertThat(MathUtil.digits(123456789L, Character.MAX_RADIX + 1), is(new byte[] { 1, 2, 3,
			4, 5, 6, 7, 8, 9 }));
	}

	@Test
	public void testIncrementByte() {
		byte[] b = { Byte.MAX_VALUE, Byte.MIN_VALUE };
		assertThat(MathUtil.increment(b, (byte) 0), is(b));
		assertArray(b, new byte[] { Byte.MAX_VALUE, Byte.MIN_VALUE });
		assertThat(MathUtil.increment(b, Byte.MIN_VALUE), is(b));
		assertArray(b, new byte[] { -1, 0 });
	}

	@Test
	public void testIncrementShort() {
		short[] s = { Short.MAX_VALUE, Short.MIN_VALUE };
		assertThat(MathUtil.increment(s, (short) 0), is(s));
		assertArray(s, new short[] { Short.MAX_VALUE, Short.MIN_VALUE });
		assertThat(MathUtil.increment(s, Short.MIN_VALUE), is(s));
		assertArray(s, new short[] { -1, 0 });
	}

	@Test
	public void testIncrementInt() {
		int[] i = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		assertThat(MathUtil.increment(i, 0), is(i));
		assertArray(i, new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE });
		assertThat(MathUtil.increment(i, Integer.MIN_VALUE), is(i));
		assertArray(i, new int[] { -1, 0 });
	}

	@Test
	public void testIncrementLong() {
		long[] l = { Long.MAX_VALUE, Long.MIN_VALUE };
		assertThat(MathUtil.increment(l, 0L), is(l));
		assertArray(l, new long[] { Long.MAX_VALUE, Long.MIN_VALUE });
		assertThat(MathUtil.increment(l, Long.MIN_VALUE), is(l));
		assertArray(l, new long[] { -1, 0 });
	}

	@Test
	public void testIncrementDouble() {
		double[] d = { Double.MAX_VALUE, -Double.MAX_VALUE, Double.NaN };
		assertThat(MathUtil.increment(d, 0.0), is(d));
		assertArray(d, new double[] { Double.MAX_VALUE, -Double.MAX_VALUE, Double.NaN });
		assertThat(MathUtil.increment(d, -Double.MAX_VALUE), is(d));
		assertArray(d, new double[] { 0, Double.NEGATIVE_INFINITY, Double.NaN });
	}

	@Test
	public void testIncrementFloat() {
		float[] f = { Float.MAX_VALUE, -Float.MAX_VALUE, Float.NaN };
		assertThat(MathUtil.increment(f, 0.0f), is(f));
		assertArray(f, new float[] { Float.MAX_VALUE, -Float.MAX_VALUE, Float.NaN });
		assertThat(MathUtil.increment(f, -Float.MAX_VALUE), is(f));
		assertArray(f, new float[] { 0, Float.NEGATIVE_INFINITY, Float.NaN });
	}

	@Test
	public void testMaxAndMinByte() {
		byte[] b = { Byte.MAX_VALUE, -1, 0, 1, Byte.MIN_VALUE };
		assertThat(MathUtil.max(new byte[0]), is((byte) 0));
		assertThat(MathUtil.max((byte[]) null), is((byte) 0));
		assertThat(MathUtil.max(b), is(Byte.MAX_VALUE));
		assertThat(MathUtil.min(new byte[0]), is((byte) 0));
		assertThat(MathUtil.min((byte[]) null), is((byte) 0));
		assertThat(MathUtil.min(b), is(Byte.MIN_VALUE));
	}

	@Test
	public void testMaxAndMinShort() {
		short[] s = { Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertThat(MathUtil.max(new short[0]), is((short) 0));
		assertThat(MathUtil.max((short[]) null), is((short) 0));
		assertThat(MathUtil.max(s), is(Short.MAX_VALUE));
		assertThat(MathUtil.min(new short[0]), is((short) 0));
		assertThat(MathUtil.min((short[]) null), is((short) 0));
		assertThat(MathUtil.min(s), is(Short.MIN_VALUE));
	}

	@Test
	public void testMaxAndMinInt() {
		int[] i = { Integer.MAX_VALUE, -1, 0, 1, Integer.MIN_VALUE };
		assertThat(MathUtil.max(new int[0]), is(0));
		assertThat(MathUtil.max((int[]) null), is(0));
		assertThat(MathUtil.max(i), is(Integer.MAX_VALUE));
		assertThat(MathUtil.min(new int[0]), is(0));
		assertThat(MathUtil.min((int[]) null), is(0));
		assertThat(MathUtil.min(i), is(Integer.MIN_VALUE));
	}

	@Test
	public void testMaxAndMinLong() {
		long[] l = { Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE };
		assertThat(MathUtil.max(new long[0]), is((long) 0));
		assertThat(MathUtil.max((long[]) null), is((long) 0));
		assertThat(MathUtil.max(l), is(Long.MAX_VALUE));
		assertThat(MathUtil.min(new long[0]), is((long) 0));
		assertThat(MathUtil.min((long[]) null), is((long) 0));
		assertThat(MathUtil.min(l), is(Long.MIN_VALUE));
	}

	@Test
	public void testMaxAndMinDouble() {
		double[] d =
			{ -Double.MAX_VALUE, Double.MAX_VALUE, -1, 0, 1, Double.MIN_VALUE, Double.NaN };
		assertThat(MathUtil.max(new double[0]), is((double) 0));
		assertThat(MathUtil.max((double[]) null), is((double) 0));
		assertThat(MathUtil.max(d), is(Double.MAX_VALUE));
		assertThat(MathUtil.min(new double[0]), is((double) 0));
		assertThat(MathUtil.min((double[]) null), is((double) 0));
		assertThat(MathUtil.min(d), is(-Double.MAX_VALUE));
	}

	@Test
	public void testMaxAndMinFloat() {
		float[] f = { -Float.MAX_VALUE, Float.MAX_VALUE, -1, 0, 1, Float.MIN_VALUE, Float.NaN };
		assertThat(MathUtil.max(new float[0]), is((float) 0));
		assertThat(MathUtil.max((float[]) null), is((float) 0));
		assertThat(MathUtil.max(f), is(Float.MAX_VALUE));
		assertThat(MathUtil.min(new float[0]), is((float) 0));
		assertThat(MathUtil.min((float[]) null), is((float) 0));
		assertThat(MathUtil.min(f), is(-Float.MAX_VALUE));
	}

	@Test
	public void testPercentage() {
		assertThat(MathUtil.percentage(90, 90), is(100.0));
		assertThat(MathUtil.percentage(Double.MAX_VALUE, Double.MAX_VALUE), is(100.0));
		assertTrue(Double.isNaN(MathUtil.percentage(Long.MAX_VALUE, 0)));
		assertThat(MathUtil.valueFromPercentage(50, 90), is(45.0));
		assertThat(MathUtil.valueFromPercentage(100, Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.valueFromPercentage(Double.MAX_VALUE, 100), is(Double.MAX_VALUE));
	}

	@Test
	public void testUnsigned() {
		assertThat(MathUtil.unsignedByte((byte) 0xff), is((short) 0xff));
		assertThat(MathUtil.unsignedShort((short) 0xffff), is(0xffff));
		assertThat(MathUtil.unsignedInt(0xffffffff), is(0xffffffffL));
	}

}