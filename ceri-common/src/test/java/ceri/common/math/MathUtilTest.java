package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertNaN;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.assertThrown;
import static java.lang.Math.PI;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import ceri.common.collection.CollectionUtil;
import ceri.common.test.TestTimer;
import ceri.common.test.TestUtil;

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
	public void testToInt() {
		assertThat(MathUtil.toInt(true), is(1));
		assertThat(MathUtil.toInt(false), is(0));
	}

	@Test
	public void testAverage() {
		assertNaN(MathUtil.average(new double[] {}));
		assertNaN(MathUtil.average(new int[] {}));
		assertNaN(MathUtil.average(new long[] {}));
		assertThat(MathUtil.averageInt(), is(0));
		assertThat(MathUtil.averageLong(), is(0L));
		assertThat(MathUtil.average(1.0), is(1.0));
		assertThat(MathUtil.average(1), is(1.0));
		assertThat(MathUtil.average(1L), is(1.0));
		assertThat(MathUtil.average(1.0, 2.0), is(1.5));
		assertThat(MathUtil.average(1, 2), is(1.5));
		assertThat(MathUtil.average(1L, 2L), is(1.5));
	}

	@Test
	public void testApproxEqual() {
		assertThat(MathUtil.approxEqual(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE),
			is(true));
		assertThat(MathUtil.approxEqual(0.001, 0.0001, 0.1), is(true));
		assertThat(MathUtil.approxEqual(0.001, 0.0001, 0.0001), is(false));
		assertThat(MathUtil.approxEqual(0.0011, 0.0012, 0.0001), is(true));
		assertThat(MathUtil.approxEqual(0.0011, 0.0012, 0.00009), is(false));
	}

	@Test
	public void testSafeToLong() {
		double d0 = Long.MIN_VALUE;
		double d1 = Long.MAX_VALUE;
		assertThat(MathUtil.safeToLong(d0), is(Long.MIN_VALUE));
		assertThat(MathUtil.safeToLong(d1), is(Long.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.safeToLong(Double.NaN));
		TestUtil.assertThrown(() -> MathUtil.safeToLong(Double.POSITIVE_INFINITY));
		TestUtil.assertThrown(() -> MathUtil.safeToLong(Double.NEGATIVE_INFINITY));
		TestUtil.assertThrown(() -> MathUtil.safeToLong(Double.MAX_VALUE));
	}

	@Test
	public void testSafeToInt() {
		double d0 = Integer.MIN_VALUE;
		double d1 = Integer.MAX_VALUE;
		assertThat(MathUtil.safeToInt(d0), is(Integer.MIN_VALUE));
		assertThat(MathUtil.safeToInt(d1), is(Integer.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.safeToInt(Double.NaN));
		TestUtil.assertThrown(() -> MathUtil.safeToInt(Double.POSITIVE_INFINITY));
		TestUtil.assertThrown(() -> MathUtil.safeToInt(Double.NEGATIVE_INFINITY));
		TestUtil.assertThrown(() -> MathUtil.safeToInt(Double.MAX_VALUE));
	}

	@Test
	public void testToShortExact() {
		long l0 = Short.MIN_VALUE;
		long l1 = Short.MAX_VALUE;
		assertThat(MathUtil.shortExact(l0), is(Short.MIN_VALUE));
		assertThat(MathUtil.shortExact(l1), is(Short.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.shortExact(Short.MIN_VALUE - 1));
		TestUtil.assertThrown(() -> MathUtil.shortExact(Short.MAX_VALUE + 1));
	}

	@Test
	public void testToByteExact() {
		long l0 = Byte.MIN_VALUE;
		long l1 = Byte.MAX_VALUE;
		assertThat(MathUtil.byteExact(l0), is(Byte.MIN_VALUE));
		assertThat(MathUtil.byteExact(l1), is(Byte.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.byteExact(Byte.MIN_VALUE - 1));
		TestUtil.assertThrown(() -> MathUtil.byteExact(Byte.MAX_VALUE + 1));
	}

	@Test
	public void testGcd() {
		assertThat(MathUtil.gcd(0, 0), is(0));
		assertThat(MathUtil.gcd(1, 0), is(1));
		assertThat(MathUtil.gcd(0, 1), is(1));
		assertThat(MathUtil.gcd(-1, 1), is(1));
		assertThat(MathUtil.gcd(-1, -1), is(1));
		assertThat(MathUtil.gcd(99, -44), is(11));
		assertThat(MathUtil.gcd(99999, 22222), is(11111));
		assertThat(MathUtil.gcd(22222, 99999), is(11111));
		assertThat(MathUtil.gcd(-99999, 22222), is(11111));
		assertThat(MathUtil.gcd(99999, -22222), is(11111));
		assertThat(MathUtil.gcd(-99999, -22222), is(11111));
		TestUtil.assertThrown(() -> MathUtil.gcd(Integer.MIN_VALUE, 0));
		assertThat(MathUtil.gcd(Integer.MIN_VALUE, 1), is(1));
		assertThat(MathUtil.gcd(Integer.MAX_VALUE, 0), is(Integer.MAX_VALUE));
		assertThat(MathUtil.gcd(Integer.MAX_VALUE, 1), is(1));
		TestUtil.assertThrown(() -> MathUtil.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertThat(MathUtil.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(MathUtil.gcd(Integer.MIN_VALUE, Integer.MAX_VALUE), is(1));
		TestUtil.assertThrown(() -> MathUtil.gcd(Long.MIN_VALUE, 0));
		assertThat(MathUtil.gcd(Long.MIN_VALUE, 1), is(1L));
		assertThat(MathUtil.gcd(Long.MAX_VALUE, 0), is(Long.MAX_VALUE));
		assertThat(MathUtil.gcd(Long.MAX_VALUE, 1), is(1L));
		TestUtil.assertThrown(() -> MathUtil.gcd(Long.MIN_VALUE, Long.MIN_VALUE));
		assertThat(MathUtil.gcd(Long.MAX_VALUE, Long.MAX_VALUE), is(Long.MAX_VALUE));
		assertThat(MathUtil.gcd(Long.MIN_VALUE, Long.MAX_VALUE), is(1L));
	}

	@Test
	public void testLcm() {
		assertThat(MathUtil.lcm(0, 0), is(0));
		assertThat(MathUtil.lcm(1, 0), is(0));
		assertThat(MathUtil.lcm(0, 1), is(0));
		assertThat(MathUtil.lcm(-1, 2), is(2));
		assertThat(MathUtil.lcm(1, -2), is(2));
		assertThat(MathUtil.lcm(2, -1), is(2));
		assertThat(MathUtil.lcm(-2, 1), is(2));
		assertThat(MathUtil.lcm(12, 4), is(12));
		assertThat(MathUtil.lcm(8, 16), is(16));
		assertThat(MathUtil.lcm(99, -44), is(396));
		assertThat(MathUtil.lcm(99999, 22222), is(199998));
		assertThat(MathUtil.lcm(22222, 99999), is(199998));
		assertThat(MathUtil.lcm(-99999, 22222), is(199998));
		assertThat(MathUtil.lcm(99999, -22222), is(199998));
		assertThat(MathUtil.lcm(-99999, -22222), is(199998));
		assertThat(MathUtil.lcm(Integer.MIN_VALUE, 0), is(0));
		TestUtil.assertThrown(() -> MathUtil.lcm(Integer.MIN_VALUE, 1));
		assertThat(MathUtil.lcm(Integer.MAX_VALUE, 0), is(0));
		assertThat(MathUtil.lcm(Integer.MAX_VALUE, 1), is(Integer.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.lcm(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertThat(MathUtil.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.lcm(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertThat(MathUtil.lcm(Long.MIN_VALUE, 0), is(0L));
		TestUtil.assertThrown(() -> MathUtil.lcm(Long.MIN_VALUE, 1L));
		assertThat(MathUtil.lcm(Long.MAX_VALUE, 0), is(0L));
		assertThat(MathUtil.lcm(Long.MAX_VALUE, 1), is(Long.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.lcm(Long.MIN_VALUE, Long.MIN_VALUE));
		assertThat(MathUtil.lcm(Long.MAX_VALUE, Long.MAX_VALUE), is(Long.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.lcm(Long.MIN_VALUE, Long.MAX_VALUE));
	}

	@Test
	public void testSignum() {
		assertThat(MathUtil.signum(0, 0), is(0));
		assertThat(MathUtil.signum(1, 0), is(0));
		assertThat(MathUtil.signum(0, 1), is(0));
		assertThat(MathUtil.signum(0L, 0L), is(0));
		assertThat(MathUtil.signum(Long.MAX_VALUE, Long.MAX_VALUE), is(1));
		assertThat(MathUtil.signum(Long.MIN_VALUE, Long.MIN_VALUE), is(1));
		assertThat(MathUtil.signum(Long.MAX_VALUE, Long.MIN_VALUE), is(-1));
		assertThat(MathUtil.signum(Long.MIN_VALUE, Long.MAX_VALUE), is(-1));
	}

	@Test
	public void testAbsExact() {
		assertThat(MathUtil.absExact(0), is(0));
		assertThat(MathUtil.absExact(Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(MathUtil.absExact(Integer.MIN_VALUE + 1), is(Integer.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.absExact(Integer.MIN_VALUE));
	}

	@Test
	public void testDivideUp() {
		assertThat(MathUtil.divideUp(Long.MIN_VALUE, Long.MIN_VALUE), is(1L));
		assertThat(MathUtil.divideUp(Long.MAX_VALUE, Long.MAX_VALUE), is(1L));
		assertThat(MathUtil.divideUp(Long.MAX_VALUE, Long.MIN_VALUE), is(-1L));
		assertThat(MathUtil.divideUp(Long.MIN_VALUE, Long.MAX_VALUE), is(-2L));
		assertThat(MathUtil.divideUp(10, 3), is(4));
		assertThat(MathUtil.divideUp(3, 10), is(1));
		assertThat(MathUtil.divideUp(0, 10), is(0));
	}

	@Test
	public void testRoundDiv() {
		assertThat(MathUtil.roundDiv(10, 4), is(3L));
		assertThat(MathUtil.roundDiv(10, 3), is(3L));
	}

	@Test
	public void testIntRoundExact() {
		double d0 = Integer.MIN_VALUE;
		double d1 = Integer.MAX_VALUE;
		assertThat(MathUtil.intRoundExact(d0), is(Integer.MIN_VALUE));
		assertThat(MathUtil.intRoundExact(d1), is(Integer.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.intRoundExact(d0 - 1));
		TestUtil.assertThrown(() -> MathUtil.intRoundExact(d1 + 1));
	}

	@Test
	public void testPeriodicLimit() {
		assertApprox(MathUtil.periodicLimit(3f, 2.5f), 0.5f);
		assertApprox(MathUtil.periodicLimit(2.5f, 2.5f), 2.5f);
		assertApprox(MathUtil.periodicLimit(-2.4f, 2.5f), 0.1f);
		assertApprox(MathUtil.periodicLimit(2.5 * PI, PI), 0.5 * PI);
		assertApprox(MathUtil.periodicLimit(PI, PI), PI);
		assertApprox(MathUtil.periodicLimit(-PI, PI), 0.0);
		assertThat(MathUtil.periodicLimit(100L, 10L), is(10L));
		assertThat(MathUtil.periodicLimit(-100L, 10L), is(0L));
	}

	@Test
	public void testPeriodicLimitEx() {
		assertThat(MathUtil.periodicLimitEx(100, 10), is(0));
		assertThat(MathUtil.periodicLimitEx(-100, 10), is(0));
		assertThat(MathUtil.periodicLimitEx(100L, 10L), is(0L));
		assertThat(MathUtil.periodicLimitEx(-100L, 10L), is(0L));

	}

	@Test
	public void testRandom() {
		assertEquals(0, MathUtil.random(0, 0));
		assertEquals(0, MathUtil.randomInt(0, 0));
		assertEquals(Long.MAX_VALUE, MathUtil.random(Long.MAX_VALUE, Long.MAX_VALUE));
		assertEquals(Long.MIN_VALUE, MathUtil.random(Long.MIN_VALUE, Long.MIN_VALUE));
		for (int i = 0; i < 100; i++) {
			assertRange(MathUtil.random(0, 1), 0, 1);
			assertRange(MathUtil.random(-1, 0), -1, 0);
			assertRange(MathUtil.random(1000, Long.MAX_VALUE), 1000, Long.MAX_VALUE);
			assertRange(MathUtil.random(Long.MIN_VALUE, -1000), Long.MIN_VALUE, -1000);
		}
		Set<Long> numbers = CollectionUtil.addAll(new HashSet<>(), -1L, 0L, 1L, 2L, 3L, 4L);
		TestTimer t = new TestTimer();
		while (!numbers.isEmpty()) {
			long l = MathUtil.random(-1, 4);
			assertRange(l, -1, 4);
			numbers.remove(l);
			t.assertLessThan(1000);
		}
	}

	@Test
	public void testLimit() {
		assertThat(MathUtil.limit(2, -3, -2), is(-2));
		assertThat(MathUtil.limit(-2, 2, 3), is(2));
		assertThat(MathUtil.limit(3, 2, 3), is(3));
		assertThat(MathUtil.limit(3, 2, 1), is(1));
		assertThat(MathUtil.limit(Long.MAX_VALUE, Long.MIN_VALUE, 0), is(0L));
		assertThat(MathUtil.limit(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE),
			is(Long.MAX_VALUE));
		assertThat(MathUtil.limit(0, Long.MIN_VALUE, Long.MAX_VALUE), is(0L));
		assertThat(MathUtil.limit(Float.MAX_VALUE, 0, Float.MIN_VALUE), is(Float.MIN_VALUE));
		assertThat(MathUtil.limit(Float.MIN_VALUE, 0, Float.MAX_VALUE), is(Float.MIN_VALUE));
		assertThat(MathUtil.limit(-Float.MAX_VALUE, 0, Float.MAX_VALUE), is(0f));
		assertThat(MathUtil.limit(Double.MAX_VALUE, 0, Double.MIN_VALUE), is(Double.MIN_VALUE));
		assertThat(MathUtil.limit(Double.MIN_VALUE, 0, Double.MAX_VALUE), is(Double.MIN_VALUE));
		assertThat(MathUtil.limit(-Double.MAX_VALUE, 0, Double.MAX_VALUE), is(0.0));
	}

	@Test
	public void testSimpleRoundAll() {
		assertArray(MathUtil.simpleRoundAll(0, 1.1, 5.5, 9.9), 1, 6, 10);
	}
	
	@Test
	public void testSimpleRound() {
		assertThat(MathUtil.simpleRound(11111.11111, 2), is(11111.11));
		assertThat(MathUtil.simpleRound(11111.11111, 10), is(11111.11111));
		assertThat(MathUtil.simpleRound(11111.111111111111111, 2), is(11111.11));
		assertThat(MathUtil.simpleRound(777.7777, 3), is(777.778));
		assertThat(MathUtil.simpleRound(-777.7777, 3), is(-777.778));
		assertTrue(Double.isNaN(MathUtil.simpleRound(Double.NaN, 0)));
		TestUtil.assertThrown(() -> MathUtil.simpleRound(777.7777, -1));
		TestUtil.assertThrown(() -> MathUtil.simpleRound(777.7777, 11));
	}

	@Test
	public void testSimpleRoundForOutOfRangeValues() {
		// Original values returned if out of range
		assertThat(MathUtil.simpleRound(1000000000.15, 1), is(1000000000.15));
		assertThat(MathUtil.simpleRound(-1000000000.15, 1), is(-1000000000.15));
		assertThat(MathUtil.simpleRound(Double.POSITIVE_INFINITY, 1), is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.simpleRound(Double.NEGATIVE_INFINITY, 1), is(Double.NEGATIVE_INFINITY));
	}

	@Test
	public void testRoundAll() {
		assertArray(MathUtil.roundAll(0, 1.1, 5.5, 9.9), 1, 6, 10);
	}
	
	@Test
	public void testRound() {
		assertThat(MathUtil.round(1000000000.15, 1), is(1000000000.2));
		assertThat(MathUtil.round(-1000000000.15, 1), is(-1000000000.2));
		assertThat(MathUtil.round(Double.POSITIVE_INFINITY, 1), is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.round(Double.NEGATIVE_INFINITY, 1), is(Double.NEGATIVE_INFINITY));
		assertThat(MathUtil.round(Double.NaN, 1), is(Double.NaN));
		TestUtil.assertThrown(() -> MathUtil.round(777.7777, -1));
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
		TestUtil.assertThrown(MathUtil::mean);
		assertThat(MathUtil.mean(0), is(0.0));
		assertThat(MathUtil.mean(-1, -2, 9), is(2.0));
		assertThat(MathUtil.mean(Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.mean(Double.MAX_VALUE, -Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.mean(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
			is(Double.NaN));
	}

	@Test
	public void testMedian() {
		TestUtil.assertThrown(MathUtil::median);
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
		assertThat(MathUtil.digits(0x123456789ABCDEFL, 16),
			is(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }));
		assertThat(MathUtil.digits(123456789L, Character.MAX_RADIX + 1),
			is(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }));
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
		assertThat(MathUtil.toPercentage(0.9), is(90.0));
		assertThat(MathUtil.toPercentage(90, 90), is(100.0));
		assertThat(MathUtil.toPercentage(Double.MAX_VALUE, Double.MAX_VALUE), is(100.0));
		assertTrue(Double.isNaN(MathUtil.toPercentage(Long.MAX_VALUE, 0)));
		assertThat(MathUtil.fromPercentage(50), is(0.5));
		assertThat(MathUtil.fromPercentage(50, 90), is(45.0));
		assertThat(MathUtil.fromPercentage(100, Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.fromPercentage(Double.MAX_VALUE, 100), is(Double.MAX_VALUE));
	}

	@Test
	public void testUnsignedExact() {
		assertThat(MathUtil.ubyteExact(0xff), is((byte) 0xff));
		assertThat(MathUtil.ushortExact(0xffff), is((short) 0xffff));
		assertThat(MathUtil.uintExact(0xffffffffL), is(0xffffffff));
		assertThrown(() -> MathUtil.ubyteExact(-1));
		assertThrown(() -> MathUtil.ubyteExact(0x100));
		assertThrown(() -> MathUtil.ushortExact(-1));
		assertThrown(() -> MathUtil.ushortExact(0x10000));
		assertThrown(() -> MathUtil.uintExact(-1));
		assertThrown(() -> MathUtil.uintExact(0x100000000L));
	}
	
	@Test
	public void testUnsigned() {
		assertThat(MathUtil.ubyte((byte) 0xff), is((short) 0xff));
		assertThat(MathUtil.ushort((short) 0xffff), is(0xffff));
		assertThat(MathUtil.uint(0xffffffff), is(0xffffffffL));
	}

}