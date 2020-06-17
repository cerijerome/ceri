package ceri.common.math;

import static ceri.common.math.Bound.Type.exclusive;
import static ceri.common.math.Bound.Type.inclusive;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertRange;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class MathUtilTest {

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
	public void testByteExact() {
		long l0 = Byte.MIN_VALUE;
		long l1 = Byte.MAX_VALUE;
		assertThat(MathUtil.byteExact(l0), is(Byte.MIN_VALUE));
		assertThat(MathUtil.byteExact(l1), is(Byte.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.byteExact(Byte.MIN_VALUE - 1));
		TestUtil.assertThrown(() -> MathUtil.byteExact(Byte.MAX_VALUE + 1));
	}

	@Test
	public void testShortExact() {
		long l0 = Short.MIN_VALUE;
		long l1 = Short.MAX_VALUE;
		assertThat(MathUtil.shortExact(l0), is(Short.MIN_VALUE));
		assertThat(MathUtil.shortExact(l1), is(Short.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.shortExact(Short.MIN_VALUE - 1));
		TestUtil.assertThrown(() -> MathUtil.shortExact(Short.MAX_VALUE + 1));
	}

	@Test
	public void testUbyteExact() {
		assertThat(MathUtil.ubyteExact(0xff), is((byte) 0xff));
		assertThrown(() -> MathUtil.ubyteExact(-1));
		assertThrown(() -> MathUtil.ubyteExact(0x100));
	}

	@Test
	public void testUshortExact() {
		assertThat(MathUtil.ushortExact(0xffff), is((short) 0xffff));
		assertThrown(() -> MathUtil.ushortExact(-1));
		assertThrown(() -> MathUtil.ushortExact(0x10000));
	}

	@Test
	public void testUintExact() {
		assertThat(MathUtil.uintExact(0xffffffffL), is(0xffffffff));
		assertThrown(() -> MathUtil.uintExact(-1));
		assertThrown(() -> MathUtil.uintExact(0x100000000L));
	}

	@Test
	public void testUbyte() {
		assertThat(MathUtil.ubyte((byte) 0xff), is((short) 0xff));
	}

	@Test
	public void testUshort() {
		assertThat(MathUtil.ushort((short) 0xffff), is(0xffff));
	}

	@Test
	public void testUint() {
		assertThat(MathUtil.uint(0xffffffff), is(0xffffffffL));
	}

	@Test
	public void testAbsExactInt() {
		assertThat(MathUtil.absExact(0), is(0));
		assertThat(MathUtil.absExact(Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(MathUtil.absExact(Integer.MIN_VALUE + 1), is(Integer.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.absExact(Integer.MIN_VALUE));
	}

	@Test
	public void testAbsExactLong() {
		assertThat(MathUtil.absExact(0L), is(0L));
		assertThat(MathUtil.absExact(Long.MAX_VALUE), is(Long.MAX_VALUE));
		assertThat(MathUtil.absExact(Long.MIN_VALUE + 1), is(Long.MAX_VALUE));
		TestUtil.assertThrown(() -> MathUtil.absExact(Long.MIN_VALUE));
	}

	@Test
	public void testIntRoundExact() {
		double d0 = Integer.MIN_VALUE;
		double d1 = Integer.MAX_VALUE;
		assertThat(MathUtil.intRoundExact(d0), is(Integer.MIN_VALUE));
		assertThat(MathUtil.intRoundExact(d1), is(Integer.MAX_VALUE));
		assertThrown(() -> MathUtil.intRoundExact(d0 - 1));
		assertThrown(() -> MathUtil.intRoundExact(d1 + 1));
	}

	@Test
	public void testCeilDivInt() {
		assertThat(MathUtil.ceilDiv(10, 4), is(3));
		assertThat(MathUtil.ceilDiv(10, 3), is(4));
		assertThat(MathUtil.ceilDiv(10, -4), is(-2));
		assertThat(MathUtil.ceilDiv(10, -3), is(-3));
		assertThat(MathUtil.ceilDiv(-10, 4), is(-2));
		assertThat(MathUtil.ceilDiv(-10, 3), is(-3));
		assertThat(MathUtil.ceilDiv(-10, -4), is(3));
		assertThat(MathUtil.ceilDiv(-10, -3), is(4));
		assertThat(MathUtil.ceilDiv(Integer.MAX_VALUE, Integer.MAX_VALUE), is(1));
		assertThat(MathUtil.ceilDiv(Integer.MAX_VALUE, Integer.MIN_VALUE), is(0));
		assertThat(MathUtil.ceilDiv(Integer.MIN_VALUE, Integer.MAX_VALUE), is(-1));
		assertThat(MathUtil.ceilDiv(Integer.MIN_VALUE, Integer.MIN_VALUE), is(1));
		assertThrown(() -> MathUtil.ceilDiv(1, 0));
	}

	@Test
	public void testCeilDivLong() {
		assertThat(MathUtil.ceilDiv(10L, 4L), is(3L));
		assertThat(MathUtil.ceilDiv(10L, 3L), is(4L));
		assertThat(MathUtil.ceilDiv(10L, -4L), is(-2L));
		assertThat(MathUtil.ceilDiv(10L, -3L), is(-3L));
		assertThat(MathUtil.ceilDiv(-10L, 4L), is(-2L));
		assertThat(MathUtil.ceilDiv(-10L, 3L), is(-3L));
		assertThat(MathUtil.ceilDiv(-10L, -4L), is(3L));
		assertThat(MathUtil.ceilDiv(-10L, -3L), is(4L));
		assertThat(MathUtil.ceilDiv(Long.MAX_VALUE, Long.MAX_VALUE), is(1L));
		assertThat(MathUtil.ceilDiv(Long.MAX_VALUE, Long.MIN_VALUE), is(0L));
		assertThat(MathUtil.ceilDiv(Long.MIN_VALUE, Long.MAX_VALUE), is(-1L));
		assertThat(MathUtil.ceilDiv(Long.MIN_VALUE, Long.MIN_VALUE), is(1L));
		assertThrown(() -> MathUtil.ceilDiv(1L, 0L));
	}

	@Test
	public void testRoundDivInt() {
		assertThat(MathUtil.roundDiv(10, 4), is(3));
		assertThat(MathUtil.roundDiv(10, 3), is(3));
		assertThat(MathUtil.roundDiv(10, -4), is(-2));
		assertThat(MathUtil.roundDiv(10, -3), is(-3));
		assertThat(MathUtil.roundDiv(-10, 4), is(-2));
		assertThat(MathUtil.roundDiv(-10, 3), is(-3));
		assertThat(MathUtil.roundDiv(-10, -4), is(3));
		assertThat(MathUtil.roundDiv(-10, -3), is(3));
		assertThat(MathUtil.roundDiv(Integer.MAX_VALUE, Integer.MAX_VALUE), is(1));
		assertThat(MathUtil.roundDiv(Integer.MAX_VALUE, Integer.MIN_VALUE), is(-1));
		assertThat(MathUtil.roundDiv(Integer.MIN_VALUE, Integer.MAX_VALUE), is(-1));
		assertThat(MathUtil.roundDiv(Integer.MIN_VALUE, Integer.MIN_VALUE), is(1));
		assertThrown(() -> MathUtil.roundDiv(1, 0));
	}

	@Test
	public void testRoundDivLong() {
		assertThat(MathUtil.roundDiv(10L, 4L), is(3L));
		assertThat(MathUtil.roundDiv(10L, 3L), is(3L));
		assertThat(MathUtil.roundDiv(10L, -4L), is(-2L));
		assertThat(MathUtil.roundDiv(10L, -3L), is(-3L));
		assertThat(MathUtil.roundDiv(-10L, 4L), is(-2L));
		assertThat(MathUtil.roundDiv(-10L, 3L), is(-3L));
		assertThat(MathUtil.roundDiv(-10L, -4L), is(3L));
		assertThat(MathUtil.roundDiv(-10L, -3L), is(3L));
		assertThat(MathUtil.roundDiv(Long.MAX_VALUE, Long.MAX_VALUE), is(1L));
		assertThat(MathUtil.roundDiv(Long.MAX_VALUE, Long.MIN_VALUE), is(-1L));
		assertThat(MathUtil.roundDiv(Long.MIN_VALUE, Long.MAX_VALUE), is(-1L));
		assertThat(MathUtil.roundDiv(Long.MIN_VALUE, Long.MIN_VALUE), is(1L));
		assertThrown(() -> MathUtil.roundDiv(1L, 0L));
	}

	@Test
	public void testRound() {
		assertThat(MathUtil.round(1, 1000000000.15), is(1000000000.2));
		assertThat(MathUtil.round(1, -1000000000.15), is(-1000000000.2));
		assertThat(MathUtil.round(1, Double.POSITIVE_INFINITY), is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.round(1, Double.NEGATIVE_INFINITY), is(Double.NEGATIVE_INFINITY));
		assertThat(MathUtil.round(1, Double.NaN), is(Double.NaN));
		TestUtil.assertThrown(() -> MathUtil.round(-1, 777.7777));
	}

	@Test
	public void testSimpleRound() {
		assertThat(MathUtil.simpleRound(2, 11111.11111), is(11111.11));
		assertThat(MathUtil.simpleRound(10, 11111.11111), is(11111.11111));
		assertThat(MathUtil.simpleRound(2, 11111.111111111111111), is(11111.11));
		assertThat(MathUtil.simpleRound(3, 777.7777), is(777.778));
		assertThat(MathUtil.simpleRound(3, -777.7777), is(-777.778));
		assertTrue(Double.isNaN(MathUtil.simpleRound(0, Double.NaN)));
		TestUtil.assertThrown(() -> MathUtil.simpleRound(-1, 777.7777));
		TestUtil.assertThrown(() -> MathUtil.simpleRound(11, 777.7777));
	}

	@Test
	public void testSimpleRoundForOutOfRangeValues() {
		// Original values returned if out of range
		assertThat(MathUtil.simpleRound(1, 1000000000.15), is(1000000000.15));
		assertThat(MathUtil.simpleRound(1, -1000000000.15), is(-1000000000.15));
		assertThat(MathUtil.simpleRound(1, Double.POSITIVE_INFINITY), is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.simpleRound(1, Double.NEGATIVE_INFINITY), is(Double.NEGATIVE_INFINITY));
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
	public void testOverflowInt() {
		assertOverflow(Integer.MAX_VALUE, 0, false);
		assertOverflow(Integer.MAX_VALUE, 1, true);
		assertOverflow(Integer.MAX_VALUE, Integer.MIN_VALUE, false);
		assertOverflow(Integer.MAX_VALUE, Integer.MAX_VALUE, true);
		assertOverflow(Integer.MIN_VALUE, 0, false);
		assertOverflow(Integer.MIN_VALUE, -1, true);
		assertOverflow(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
		assertOverflow(Integer.MIN_VALUE, Integer.MIN_VALUE, true);
	}

	@Test
	public void testOverflowLong() {
		assertOverflow(Long.MAX_VALUE, 0L, false);
		assertOverflow(Long.MAX_VALUE, 1L, true);
		assertOverflow(Long.MAX_VALUE, Long.MIN_VALUE, false);
		assertOverflow(Long.MAX_VALUE, Long.MAX_VALUE, true);
		assertOverflow(Long.MIN_VALUE, 0L, false);
		assertOverflow(Long.MIN_VALUE, -1L, true);
		assertOverflow(Long.MIN_VALUE, Long.MAX_VALUE, false);
		assertOverflow(Long.MIN_VALUE, Long.MIN_VALUE, true);
	}

	private static void assertOverflow(int l, int r, boolean overflow) {
		assertThat(MathUtil.overflow(l + r, l, r), is(overflow));
	}

	private static void assertOverflow(long l, long r, boolean overflow) {
		assertThat(MathUtil.overflow(l + r, l, r), is(overflow));
	}

	@Test
	public void testRandomIntFill() {
		int bits = 0;
		for (int i = 0; i < 1000 && bits != 0xff; i++)
			bits |= 1 << MathUtil.random(0, 7);
		assertThat(bits, is(0xff));
	}

	@Test
	public void testRandomInt() {
		assertRange(MathUtil.random(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE,
			Integer.MAX_VALUE);
		assertRange(MathUtil.random(0, Integer.MAX_VALUE), 0, Integer.MAX_VALUE);
		assertRange(MathUtil.random(Integer.MIN_VALUE, 0), Integer.MIN_VALUE, 0);
		assertThat(MathUtil.random(Integer.MAX_VALUE, Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(MathUtil.random(Integer.MIN_VALUE, Integer.MIN_VALUE), is(Integer.MIN_VALUE));
	}

	@Test
	public void testRandomLongFill() {
		int bits = 0;
		for (int i = 0; i < 1000 && bits != 0xff; i++)
			bits |= 1 << MathUtil.random(0L, 7L);
		assertThat(bits, is(0xff));
	}

	@Test
	public void testRandomLong() {
		assertRange(MathUtil.random(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE,
			Long.MAX_VALUE);
		assertRange(MathUtil.random(0L, Long.MAX_VALUE), 0L, Long.MAX_VALUE);
		assertRange(MathUtil.random(Long.MIN_VALUE, 0L), Long.MIN_VALUE, 0L);
		assertThat(MathUtil.random(Long.MAX_VALUE, Long.MAX_VALUE), is(Long.MAX_VALUE));
		assertThat(MathUtil.random(Long.MIN_VALUE, Long.MIN_VALUE), is(Long.MIN_VALUE));
	}

	@Test
	public void testRandomDouble() {
		assertRange(MathUtil.random(), 0.0, 1.0);
	}

	@Test
	public void testRandomDoubleRange() {
		assertRange(MathUtil.random(-1.0, 2.0), -1.0, 2.0);
		assertRange(MathUtil.random(-1.0, 0.0), -1.0, 0.0);
	}

	@Test
	public void testToPercent() {
		assertThat(MathUtil.toPercent(0.9), is(90.0));
		assertThat(MathUtil.toPercent(90, 90), is(100.0));
		assertThat(MathUtil.toPercent(Double.MAX_VALUE, Double.MAX_VALUE), is(100.0));
		assertTrue(Double.isNaN(MathUtil.toPercent(Long.MAX_VALUE, 0)));
	}

	@Test
	public void testFromPercent() {
		assertThat(MathUtil.fromPercent(50), is(0.5));
		assertThat(MathUtil.fromPercent(50, 90), is(45.0));
		assertThat(MathUtil.fromPercent(100, Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.fromPercent(Double.MAX_VALUE, 100), is(Double.MAX_VALUE));
	}

	@Test
	public void testGcd() {
		assertThat(MathUtil.gcd(0, 0), is(0));
		assertThat(MathUtil.gcd(1, 0), is(1));
		assertThat(MathUtil.gcd(0, 1), is(1));
		assertThat(MathUtil.gcd(-1, 2), is(1));
		assertThat(MathUtil.gcd(1, 2), is(1));
		assertThat(MathUtil.gcd(2, -1), is(1));
		assertThat(MathUtil.gcd(2, 1), is(1));
		assertThat(MathUtil.gcd(4, 2), is(2));
		assertThat(MathUtil.gcd(2, 4), is(2));
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
	public void testMeanInt() {
		assertThat(MathUtil.mean(-1), is(-1.0));
		assertThat(MathUtil.mean(1, -1), is(0.0));
		assertThat(MathUtil.mean(1, -1, 3), is(1.0));
		assertThat(MathUtil.mean(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
			is((double) Integer.MAX_VALUE));
		assertThat(MathUtil.mean(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
			is((double) Integer.MIN_VALUE));
		assertThat(MathUtil.mean(Integer.MAX_VALUE, Integer.MIN_VALUE), is(-0.5));
		assertThrown(() -> MathUtil.mean(new int[0]));
		assertThrown(() -> MathUtil.mean(new int[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((int[]) null, 0, 1));
	}

	@Test
	public void testMeanLong() {
		assertThat(MathUtil.mean(-1L), is(-1.0));
		assertThat(MathUtil.mean(1L, -1L), is(0.0));
		assertThat(MathUtil.mean(1L, -1L, 3L), is(1.0));
		assertThat(MathUtil.mean(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE),
			is((double) Long.MAX_VALUE));
		assertThat(MathUtil.mean(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE),
			is((double) Long.MIN_VALUE));
		assertThat(MathUtil.mean(Long.MAX_VALUE, Long.MIN_VALUE), is(-0.5));
		assertThrown(() -> MathUtil.mean(new long[0]));
		assertThrown(() -> MathUtil.mean(new long[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((long[]) null, 0, 1));
	}

	@Test
	public void testMeanFloat() {
		assertThat(MathUtil.mean(-1.0f), is(-1.0f));
		assertThat(MathUtil.mean(1.0f, -1.0f), is(0.0f));
		assertThat(MathUtil.mean(1.0f, -1.0f, 3.0f), is(1.0f));
		assertThat(MathUtil.mean(Float.MAX_VALUE, -Float.MAX_VALUE), is(0.0f));
		assertThat(MathUtil.mean(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
			is(Float.POSITIVE_INFINITY));
		assertThat(MathUtil.mean(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), is(Float.NaN));
		assertThrown(() -> MathUtil.mean(new float[0]));
		assertThrown(() -> MathUtil.mean(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		assertThrown(() -> MathUtil.mean((float[]) null, 0, 1));
	}

	@Test
	public void testMeanDouble() {
		assertThat(MathUtil.mean(-1.0), is(-1.0));
		assertThat(MathUtil.mean(1.0, -1.0), is(0.0));
		assertThat(MathUtil.mean(1.0, -1.0, 3.0), is(1.0));
		assertThat(MathUtil.mean(Double.MAX_VALUE, -Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.mean(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
			is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.mean(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
			is(Double.NaN));
		assertThrown(() -> MathUtil.mean(new double[0]));
		assertThrown(() -> MathUtil.mean(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((double[]) null, 0, 1));
	}

	@Test
	public void testMedianInt() {
		assertThat(MathUtil.median(-1), is(-1.0));
		assertThat(MathUtil.median(1, -1), is(0.0));
		assertThat(MathUtil.median(1, -1, 3), is(1.0));
		assertThat(MathUtil.median(Integer.MAX_VALUE, Integer.MIN_VALUE), is(-0.5));
		assertThat(MathUtil.median(Integer.MAX_VALUE, Integer.MAX_VALUE),
			is((double) Integer.MAX_VALUE));
		assertThat(MathUtil.median(Integer.MIN_VALUE, Integer.MIN_VALUE),
			is((double) Integer.MIN_VALUE));
		assertThat(MathUtil.median(Integer.MAX_VALUE - 1, Integer.MAX_VALUE),
			is(Integer.MAX_VALUE - 0.5));
		assertThat(MathUtil.median(Integer.MIN_VALUE + 1, Integer.MIN_VALUE),
			is(Integer.MIN_VALUE + 0.5));
		assertThrown(() -> MathUtil.median(new int[0]));
		assertThrown(() -> MathUtil.median(new int[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.median((int[]) null, 0, 1));
	}

	@Test
	public void testMedianLong() {
		assertThat(MathUtil.median(-1L), is(-1.0));
		assertThat(MathUtil.median(1L, -1L), is(0.0));
		assertThat(MathUtil.median(1L, -1L, 3L), is(1.0));
		assertThat(MathUtil.median(Long.MAX_VALUE, Long.MIN_VALUE), is(-0.5));
		assertThat(MathUtil.median(Long.MAX_VALUE, Long.MAX_VALUE), is((double) Long.MAX_VALUE));
		assertThat(MathUtil.median(Long.MIN_VALUE, Long.MIN_VALUE), is((double) Long.MIN_VALUE));
		assertThat(MathUtil.median(Long.MAX_VALUE - 1, Long.MAX_VALUE), is(Long.MAX_VALUE - 0.5));
		assertThat(MathUtil.median(Long.MIN_VALUE + 1, Long.MIN_VALUE), is(Long.MIN_VALUE + 0.5));
		assertThrown(() -> MathUtil.median(new long[0]));
		assertThrown(() -> MathUtil.median(new long[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.median((long[]) null, 0, 1));
	}

	@Test
	public void testMedianFloat() {
		assertThat(MathUtil.median(-1.0f), is(-1.0f));
		assertThat(MathUtil.median(1.0f, -1.0f), is(0.0f));
		assertThat(MathUtil.median(1.0f, -1.0f, 3.0f), is(1.0f));
		assertThat(MathUtil.median(Float.MAX_VALUE, -Float.MAX_VALUE), is(0.0f));
		assertThat(MathUtil.median(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
			is(Float.POSITIVE_INFINITY));
		assertThat(MathUtil.median(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY),
			is(Float.NaN));
		assertThrown(() -> MathUtil.median(new float[0]));
		assertThrown(() -> MathUtil.median(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		assertThrown(() -> MathUtil.median((float[]) null, 0, 1));
	}

	@Test
	public void testMedianDouble() {
		assertThat(MathUtil.median(-1.0), is(-1.0));
		assertThat(MathUtil.median(1.0, -1.0), is(0.0));
		assertThat(MathUtil.median(1.0, -1.0, 3.0), is(1.0));
		assertThat(MathUtil.median(Double.MAX_VALUE, -Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.median(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
			is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.median(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
			is(Double.NaN));
		assertThrown(() -> MathUtil.median(new double[0]));
		assertThrown(() -> MathUtil.median(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		assertThrown(() -> MathUtil.median((double[]) null, 0, 1));
	}

	@Test
	public void testLimitInt() {
		assertThat(MathUtil.limit(2, -1, 1), is(1));
		assertThat(MathUtil.limit(-2, -1, 1), is(-1));
		assertThat(MathUtil.limit(0, -1, 1), is(0));
		assertThat(MathUtil.limit(Integer.MAX_VALUE, 0, Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(MathUtil.limit(Integer.MIN_VALUE, 0, Integer.MAX_VALUE), is(0));
		assertThrown(() -> MathUtil.limit(0, 1, 0));
	}

	@Test
	public void testLimitLong() {
		assertThat(MathUtil.limit(2L, -1L, 1L), is(1L));
		assertThat(MathUtil.limit(-2L, -1L, 1L), is(-1L));
		assertThat(MathUtil.limit(0L, -1L, 1L), is(0L));
		assertThat(MathUtil.limit(Long.MAX_VALUE, 0L, Long.MAX_VALUE), is(Long.MAX_VALUE));
		assertThat(MathUtil.limit(Long.MIN_VALUE, 0L, Long.MAX_VALUE), is(0L));
		assertThrown(() -> MathUtil.limit(0L, 1L, 0L));
	}

	@Test
	public void testLimitFloat() {
		assertThat(MathUtil.limit(1.0f, -0.5f, -0.5f), is(-0.5f));
		assertThat(MathUtil.limit(1.0f, -0.5f, 0.5f), is(0.5f));
		assertThat(MathUtil.limit(-1.0f, -0.5f, 0.5f), is(-0.5f));
		assertThat(MathUtil.limit(0.0f, -0.5f, 0.5f), is(0.0f));
		assertThat(MathUtil.limit(Float.MAX_VALUE, 0.0f, Float.MAX_VALUE), is(Float.MAX_VALUE));
		assertThat(MathUtil.limit(-Float.MAX_VALUE, 0.0f, Float.MAX_VALUE), is(0.0f));
		assertThat(MathUtil.limit(Float.POSITIVE_INFINITY, 0.0f, Float.MAX_VALUE),
			is(Float.MAX_VALUE));
		assertThat(MathUtil.limit(Float.NEGATIVE_INFINITY, 0.0f, Float.MAX_VALUE), is(0.0f));
		assertThrown(() -> MathUtil.limit(0.0f, 1.0f, 0.0f));
	}

	@Test
	public void testLimitDouble() {
		assertThat(MathUtil.limit(1.0, -0.5, -0.5), is(-0.5));
		assertThat(MathUtil.limit(1.0, -0.5, 0.5), is(0.5));
		assertThat(MathUtil.limit(-1.0, -0.5, 0.5), is(-0.5));
		assertThat(MathUtil.limit(0.0, -0.5, 0.5), is(0.0));
		assertThat(MathUtil.limit(Double.MAX_VALUE, 0.0, Double.MAX_VALUE), is(Double.MAX_VALUE));
		assertThat(MathUtil.limit(-Double.MAX_VALUE, 0.0, Double.MAX_VALUE), is(0.0));
		assertThat(MathUtil.limit(Double.POSITIVE_INFINITY, 0.0, Double.MAX_VALUE),
			is(Double.MAX_VALUE));
		assertThat(MathUtil.limit(Double.NEGATIVE_INFINITY, 0.0, Double.MAX_VALUE), is(0.0));
		assertThrown(() -> MathUtil.limit(0.0, 1.0, 0.0));
	}

	@Test
	public void testPeriodicLimitInt() {
		assertThat(MathUtil.periodicLimit(100, 10, inclusive), is(10));
		assertThat(MathUtil.periodicLimit(100, 10, exclusive), is(0));
		assertThat(MathUtil.periodicLimit(-100, 10, inclusive), is(0));
		assertThat(MathUtil.periodicLimit(-100, 10, exclusive), is(0));
		assertThat(MathUtil.periodicLimit(7, 10, inclusive), is(7));
		assertThat(MathUtil.periodicLimit(-7, 10, inclusive), is(3));
		assertThat(MathUtil.periodicLimit(0, Integer.MAX_VALUE, inclusive), is(0));
		assertThat(MathUtil.periodicLimit(0, Integer.MAX_VALUE, exclusive), is(0));
		assertThat(MathUtil.periodicLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, inclusive),
			is(Integer.MAX_VALUE));
		assertThat(MathUtil.periodicLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, exclusive), is(0));
		assertThat(MathUtil.periodicLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, inclusive),
			is(Integer.MAX_VALUE - 1));
		assertThat(MathUtil.periodicLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, exclusive),
			is(Integer.MAX_VALUE - 1));
		assertThrown(() -> MathUtil.periodicLimit(100, 10, null));
		assertThrown(() -> MathUtil.periodicLimit(100, 0, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100, -10, inclusive));
	}

	@Test
	public void testPeriodicLimitLong() {
		assertThat(MathUtil.periodicLimit(100L, 10L, inclusive), is(10L));
		assertThat(MathUtil.periodicLimit(100L, 10L, exclusive), is(0L));
		assertThat(MathUtil.periodicLimit(-100L, 10L, inclusive), is(0L));
		assertThat(MathUtil.periodicLimit(-100L, 10L, exclusive), is(0L));
		assertThat(MathUtil.periodicLimit(7L, 10L, inclusive), is(7L));
		assertThat(MathUtil.periodicLimit(-7L, 10L, inclusive), is(3L));
		assertThat(MathUtil.periodicLimit(0L, Long.MAX_VALUE, inclusive), is(0L));
		assertThat(MathUtil.periodicLimit(0L, Long.MAX_VALUE, exclusive), is(0L));
		assertThat(MathUtil.periodicLimit(Long.MAX_VALUE, Long.MAX_VALUE, inclusive),
			is(Long.MAX_VALUE));
		assertThat(MathUtil.periodicLimit(Long.MAX_VALUE, Long.MAX_VALUE, exclusive), is(0L));
		assertThat(MathUtil.periodicLimit(Long.MIN_VALUE, Long.MAX_VALUE, inclusive),
			is(Long.MAX_VALUE - 1));
		assertThat(MathUtil.periodicLimit(Long.MIN_VALUE, Long.MAX_VALUE, exclusive),
			is(Long.MAX_VALUE - 1));
		assertThrown(() -> MathUtil.periodicLimit(100L, 10L, null));
		assertThrown(() -> MathUtil.periodicLimit(100L, 0L, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100L, -10L, inclusive));
	}

	@Test
	public void testPeriodicLimitFloat() {
		assertThat(MathUtil.periodicLimit(100.0f, 10.0f, inclusive), is(10.0f));
		assertThat(MathUtil.periodicLimit(100.0f, 10.0f, exclusive), is(0.0f));
		assertThat(MathUtil.periodicLimit(-100.0f, 10.0f, inclusive), is(0.0f));
		assertThat(MathUtil.periodicLimit(-100.0f, 10.0f, exclusive), is(0.0f));
		assertThat(MathUtil.periodicLimit(7.0f, 10.0f, inclusive), is(7.0f));
		assertThat(MathUtil.periodicLimit(-7.0f, 10.0f, inclusive), is(3.0f));
		assertThat(MathUtil.periodicLimit(0.0f, Float.POSITIVE_INFINITY, inclusive), is(0.0f));
		assertThat(MathUtil.periodicLimit(0.0f, Float.POSITIVE_INFINITY, exclusive), is(0.0f));
		assertThat(MathUtil.periodicLimit( //
			Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, inclusive),
			is(Float.POSITIVE_INFINITY));
		assertThat(MathUtil.periodicLimit( //
			Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, exclusive), is(Float.NaN));
		assertThat(MathUtil.periodicLimit( //
			Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, inclusive), is(Float.NaN));
		assertThrown(() -> MathUtil.periodicLimit(100.0f, 10.0f, null));
		assertThrown(() -> MathUtil.periodicLimit(100.0f, 0.0f, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0f, -10.0f, inclusive));
	}

	@Test
	public void testPeriodicLimitDouble() {
		assertThat(MathUtil.periodicLimit(100.0, 10.0, inclusive), is(10.0));
		assertThat(MathUtil.periodicLimit(100.0, 10.0, exclusive), is(0.0));
		assertThat(MathUtil.periodicLimit(-100.0, 10.0, inclusive), is(0.0));
		assertThat(MathUtil.periodicLimit(-100.0, 10.0, exclusive), is(0.0));
		assertThat(MathUtil.periodicLimit(7.0, 10.0, inclusive), is(7.0));
		assertThat(MathUtil.periodicLimit(-7.0, 10.0, inclusive), is(3.0));
		assertThat(MathUtil.periodicLimit(0.0, Double.POSITIVE_INFINITY, inclusive), is(0.0));
		assertThat(MathUtil.periodicLimit(0.0, Double.POSITIVE_INFINITY, exclusive), is(0.0));
		assertThat(MathUtil.periodicLimit( //
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, inclusive),
			is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.periodicLimit( //
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, exclusive), is(Double.NaN));
		assertThat(MathUtil.periodicLimit( //
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, inclusive), is(Double.NaN));
		assertThrown(() -> MathUtil.periodicLimit(100.0, 10.0, null));
		assertThrown(() -> MathUtil.periodicLimit(100.0, 0.0, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0, -10.0, inclusive));
	}

	@Test
	public void testMinByte() {
		byte[] array = { Byte.MIN_VALUE, -Byte.MAX_VALUE, -1, 0, 1, Byte.MAX_VALUE };
		assertThat(MathUtil.min(array), is(Byte.MIN_VALUE));
		assertThat(MathUtil.min(array, 1, 5), is((byte) -Byte.MAX_VALUE));
		assertThat(MathUtil.min(array, 2, 3), is((byte) -1));
		assertThrown(() -> MathUtil.min(new byte[0]));
		assertThrown(() -> MathUtil.min(new byte[2], 1, 0));
		assertThrown(() -> MathUtil.min((byte[]) null));
		assertThrown(() -> MathUtil.min(new byte[3], 1, 3));
	}

	@Test
	public void testMinShort() {
		short[] array = { Short.MIN_VALUE, -Short.MAX_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertThat(MathUtil.min(array), is(Short.MIN_VALUE));
		assertThat(MathUtil.min(array, 1, 5), is((short) -Short.MAX_VALUE));
		assertThat(MathUtil.min(array, 2, 3), is((short) -1));
		assertThrown(() -> MathUtil.min(new short[0]));
		assertThrown(() -> MathUtil.min(new short[2], 1, 0));
		assertThrown(() -> MathUtil.min((short[]) null));
		assertThrown(() -> MathUtil.min(new short[3], 1, 3));
	}

	@Test
	public void testMinInt() {
		int[] array = { Integer.MIN_VALUE, -Integer.MAX_VALUE, -1, 0, 1, Integer.MAX_VALUE };
		assertThat(MathUtil.min(array), is(Integer.MIN_VALUE));
		assertThat(MathUtil.min(array, 1, 5), is(-Integer.MAX_VALUE));
		assertThat(MathUtil.min(array, 2, 3), is(-1));
		assertThrown(() -> MathUtil.min(new int[0]));
		assertThrown(() -> MathUtil.min(new int[2], 1, 0));
		assertThrown(() -> MathUtil.min((int[]) null));
		assertThrown(() -> MathUtil.min(new int[3], 1, 3));
	}

	@Test
	public void testMinLong() {
		long[] array = { Long.MIN_VALUE, -Long.MAX_VALUE, -1, 0, 1, Long.MAX_VALUE };
		assertThat(MathUtil.min(array), is(Long.MIN_VALUE));
		assertThat(MathUtil.min(array, 1, 5), is(-Long.MAX_VALUE));
		assertThat(MathUtil.min(array, 2, 3), is(-1L));
		assertThrown(() -> MathUtil.min(new long[0]));
		assertThrown(() -> MathUtil.min(new long[2], 1, 0));
		assertThrown(() -> MathUtil.min((long[]) null));
		assertThrown(() -> MathUtil.min(new long[3], 1, 3));
	}

	@Test
	public void testMinFloat() {
		float[] d = { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, Float.MAX_VALUE, -1, 0, 1,
			Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NaN };
		assertThat(MathUtil.min(d), is(Float.NaN));
		assertThat(MathUtil.min(d, 0, 8), is(Float.NEGATIVE_INFINITY));
		assertThat(MathUtil.min(d, 1, 7), is(-Float.MAX_VALUE));
		assertThat(MathUtil.min(d, 3, 3), is(-1.0f));
		assertThrown(() -> MathUtil.min(new float[0]));
		assertThrown(() -> MathUtil.min(new float[2], 1, 0));
		assertThrown(() -> MathUtil.min((float[]) null));
		assertThrown(() -> MathUtil.min(new float[3], 1, 3));
	}

	@Test
	public void testMinDouble() {
		double[] d = { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, Double.MAX_VALUE, -1, 0, 1,
			Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NaN };
		assertThat(MathUtil.min(d), is(Double.NaN));
		assertThat(MathUtil.min(d, 0, 8), is(Double.NEGATIVE_INFINITY));
		assertThat(MathUtil.min(d, 1, 7), is(-Double.MAX_VALUE));
		assertThat(MathUtil.min(d, 3, 3), is(-1.0));
		assertThrown(() -> MathUtil.min(new double[0]));
		assertThrown(() -> MathUtil.min(new double[2], 1, 0));
		assertThrown(() -> MathUtil.min((double[]) null));
		assertThrown(() -> MathUtil.min(new double[3], 1, 3));
	}

	@Test
	public void testMaxByte() {
		byte[] array = { Byte.MIN_VALUE, -Byte.MAX_VALUE, -1, 0, 1, Byte.MAX_VALUE };
		assertThat(MathUtil.max(array), is(Byte.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 2), is((byte) -Byte.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 5), is((byte) 1));
		assertThrown(() -> MathUtil.max(new byte[0]));
		assertThrown(() -> MathUtil.max(new byte[2], 1, 0));
		assertThrown(() -> MathUtil.max((byte[]) null));
		assertThrown(() -> MathUtil.max(new byte[3], 1, 3));
	}

	@Test
	public void testMaxShort() {
		short[] array = { Short.MIN_VALUE, -Short.MAX_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertThat(MathUtil.max(array), is(Short.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 2), is((short) -Short.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 5), is((short) 1));
		assertThrown(() -> MathUtil.max(new short[0]));
		assertThrown(() -> MathUtil.max(new short[2], 1, 0));
		assertThrown(() -> MathUtil.max((short[]) null));
		assertThrown(() -> MathUtil.max(new short[3], 1, 3));
	}

	@Test
	public void testMaxInt() {
		int[] array = { Integer.MIN_VALUE, -Integer.MAX_VALUE, -1, 0, 1, Integer.MAX_VALUE };
		assertThat(MathUtil.max(array), is(Integer.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 2), is(-Integer.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 5), is(1));
		assertThrown(() -> MathUtil.max(new int[0]));
		assertThrown(() -> MathUtil.max(new int[2], 1, 0));
		assertThrown(() -> MathUtil.max((int[]) null));
		assertThrown(() -> MathUtil.max(new int[3], 1, 3));
	}

	@Test
	public void testMaxLong() {
		long[] array = { Long.MIN_VALUE, -Long.MAX_VALUE, -1, 0, 1, Long.MAX_VALUE };
		assertThat(MathUtil.max(array), is(Long.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 2), is(-Long.MAX_VALUE));
		assertThat(MathUtil.max(array, 0, 5), is(1L));
		assertThrown(() -> MathUtil.max(new long[0]));
		assertThrown(() -> MathUtil.max(new long[2], 1, 0));
		assertThrown(() -> MathUtil.max((long[]) null));
		assertThrown(() -> MathUtil.max(new long[3], 1, 3));
	}

	@Test
	public void testMaxFloat() {
		float[] array = { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, Float.MAX_VALUE, -1, 0, 1,
			Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NaN };
		assertThat(MathUtil.max(array), is(Float.NaN));
		assertThat(MathUtil.max(array, 0, 8), is(Float.POSITIVE_INFINITY));
		assertThat(MathUtil.max(array, 0, 7), is(Float.MAX_VALUE));
		assertThat(MathUtil.max(array, 3, 3), is(1.0f));
		assertThrown(() -> MathUtil.max(new float[0]));
		assertThrown(() -> MathUtil.max(new float[2], 1, 0));
		assertThrown(() -> MathUtil.max((float[]) null));
		assertThrown(() -> MathUtil.max(new float[3], 1, 3));
	}

	@Test
	public void testMaxDouble() {
		double[] array = { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, Double.MAX_VALUE, -1, 0, 1,
			Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NaN };
		assertThat(MathUtil.max(array), is(Double.NaN));
		assertThat(MathUtil.max(array, 0, 8), is(Double.POSITIVE_INFINITY));
		assertThat(MathUtil.max(array, 0, 7), is(Double.MAX_VALUE));
		assertThat(MathUtil.max(array, 3, 3), is(1.0));
		assertThrown(() -> MathUtil.max(new double[0]));
		assertThrown(() -> MathUtil.max(new double[2], 1, 0));
		assertThrown(() -> MathUtil.max((double[]) null));
		assertThrown(() -> MathUtil.max(new double[3], 1, 3));
	}

}