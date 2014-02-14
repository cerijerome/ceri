package ceri.common.util;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
		assertThat(MathUtil.mean(0), is(0.0));
		assertThat(MathUtil.mean(-1, -2, 9), is(2.0));
		assertThat(MathUtil.mean(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.mean(Double.MAX_VALUE, -Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.mean(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
			is(Double.NaN));
	}

	@Test
	public void testMedian() {
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
	public void testFactorial() {
		assertThat(MathUtil.factorial(0), is(1.0));
		assertEquals(9.33262e157, MathUtil.factorial(100), 0.00001e157);
		assertException(IllegalArgumentException.class, new Runnable() {
			@Override
			public void run() {
				MathUtil.longFactorial(21);
			}
		});
	}

	@Test
	public void testIncrement() {
		byte[] b = { Byte.MAX_VALUE, Byte.MIN_VALUE };
		assertThat(MathUtil.increment(b, Byte.MIN_VALUE), is(b));
		assertArray(b, new byte[] { -1, 0 });
		short[] s = { Short.MAX_VALUE, Short.MIN_VALUE };
		assertThat(MathUtil.increment(s, Short.MIN_VALUE), is(s));
		assertArray(s, new short[] { -1, 0 });
		int[] i = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		assertThat(MathUtil.increment(i, Integer.MIN_VALUE), is(i));
		assertArray(i, new int[] { -1, 0 });
		long[] l = { Long.MAX_VALUE, Long.MIN_VALUE };
		assertThat(MathUtil.increment(l, Long.MIN_VALUE), is(l));
		assertArray(l, new long[] { -1, 0 });
		double[] d = { Double.MAX_VALUE, -Double.MAX_VALUE, Double.NaN };
		assertThat(MathUtil.increment(d, -Double.MAX_VALUE), is(d));
		assertArray(d, new double[] { 0, Double.NEGATIVE_INFINITY, Double.NaN });
		float[] f = { Float.MAX_VALUE, -Float.MAX_VALUE, Float.NaN };
		assertThat(MathUtil.increment(f, -Float.MAX_VALUE), is(f));
		assertArray(f, new float[] { 0, Float.NEGATIVE_INFINITY, Float.NaN });
	}

	@Test
	public void testMaxAndMin() {
		byte[] b = { Byte.MAX_VALUE, -1, 0, 1, Byte.MIN_VALUE };
		assertThat(MathUtil.max(b), is(Byte.MAX_VALUE));
		assertThat(MathUtil.min(b), is(Byte.MIN_VALUE));
		short[] s = { Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertThat(MathUtil.max(s), is(Short.MAX_VALUE));
		assertThat(MathUtil.min(s), is(Short.MIN_VALUE));
		int[] i = { Integer.MAX_VALUE, -1, 0, 1, Integer.MIN_VALUE };
		assertThat(MathUtil.max(i), is(Integer.MAX_VALUE));
		assertThat(MathUtil.min(i), is(Integer.MIN_VALUE));
		long[] l = { Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE };
		assertThat(MathUtil.max(l), is(Long.MAX_VALUE));
		assertThat(MathUtil.min(l), is(Long.MIN_VALUE));
		double[] d =
			{ -Double.MAX_VALUE, Double.MAX_VALUE, -1, 0, 1, Double.MIN_VALUE, Double.NaN };
		assertThat(MathUtil.max(d), is(Double.MAX_VALUE));
		assertThat(MathUtil.min(d), is(-Double.MAX_VALUE));
		float[] f = { -Float.MAX_VALUE, Float.MAX_VALUE, -1, 0, 1, Float.MIN_VALUE, Float.NaN };
		assertThat(MathUtil.max(f), is(Float.MAX_VALUE));
		assertThat(MathUtil.min(f), is(-Float.MAX_VALUE));
	}

	@Test
	public void testPascal() {
		assertThat(MathUtil.pascal(0, 0), is(1L));
		assertThat(MathUtil.pascal(1, 0), is(1L));
		assertThat(MathUtil.pascal(1, 1), is(1L));
		assertThat(MathUtil.pascal(5, 0), is(1L));
		assertThat(MathUtil.pascal(5, 1), is(5L));
		assertThat(MathUtil.pascal(5, 2), is(10L));
		assertThat(MathUtil.pascal(5, 3), is(10L));
		assertThat(MathUtil.pascal(5, 4), is(5L));
		assertThat(MathUtil.pascal(5, 5), is(1L));
	}

	@Test
	public void testPercentage() {
		assertThat(MathUtil.percentage(Double.MAX_VALUE, Double.MAX_VALUE), is(100.0));
		assertTrue(Double.isNaN(MathUtil.percentage(Long.MAX_VALUE, 0)));
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