package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MathUtilTest {

	@Test
	public void testMean() {
		assertThat(MathUtil.mean(0), is(0.0));
		assertThat(MathUtil.mean(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.mean(-1, -2, 9), is(2.0));
	}
	
	@Test
	public void testMedian() {
		assertThat(MathUtil.median(0), is(0.0));
		assertThat(MathUtil.median(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.median(-1, -2, 9), is(-1.0));
		assertThat(MathUtil.median(1, 2, 3, 4), is(2.5));
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
		assertThat(MathUtil.digits(123456789L, Character.MAX_RADIX + 1),
			is(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFactorial() {
		assertThat(MathUtil.factorial(0), is(1.0));
		assertEquals(9.33262e157, MathUtil.factorial(100), 0.00001e157);
		assertThat(MathUtil.longFactorial(21), is(0L));
	}
	
	@Test
	public void testIncrement() {
		byte[] b = MathUtil.increment(new byte[]{ Byte.MIN_VALUE, Byte.MAX_VALUE }, Byte.MAX_VALUE);
		assertThat(b, is(new byte[] { -1, -2 }));
		long[] l = MathUtil.increment(new long[]{ Long.MIN_VALUE, Long.MAX_VALUE }, Long.MIN_VALUE);
		assertThat(l, is(new long[] { 0, -1 }));
	}
	
	@Test
	public void testMaxAndMin() {
		short[] s = { Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertThat(MathUtil.max(s), is(Short.MAX_VALUE));
		assertThat(MathUtil.min(s), is(Short.MIN_VALUE));
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
		assertThat(MathUtil.unsignedByte((byte)0xff), is((short)0xff));
		assertThat(MathUtil.unsignedShort((short)0xffff), is(0xffff));
		assertThat(MathUtil.unsignedInt(0xffffffff), is(0xffffffffL));
	}

}