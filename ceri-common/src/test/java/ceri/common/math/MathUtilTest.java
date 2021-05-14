package ceri.common.math;

import static ceri.common.math.Bound.Type.exclusive;
import static ceri.common.math.Bound.Type.inclusive;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNaN;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRange;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class MathUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(MathUtil.class);
	}

	@Test
	public void testDoublePolynomial() {
		assertEquals(MathUtil.polynomial(2.0), 0.0);
		assertEquals(MathUtil.polynomial(0.5, 3.0, 0.5, 0.1, 0.2), 3.3);
	}

	@Test
	public void testLongPolynomial() {
		assertEquals(MathUtil.polynomial(5), 0L);
		assertEquals(MathUtil.polynomial(2, 3, 0, 5, 1), 31L);
	}

	@Test
	public void testToInt() {
		assertEquals(MathUtil.toInt(true), 1);
		assertEquals(MathUtil.toInt(false), 0);
	}

	@Test
	public void testAbsLimitInt() {
		assertEquals(MathUtil.absLimit(-1), 1);
		assertEquals(MathUtil.absLimit(Integer.MIN_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.absLimit(Integer.MAX_VALUE), Integer.MAX_VALUE);
	}

	@Test
	public void testAbsLimitLong() {
		assertEquals(MathUtil.absLimit(-1L), 1L);
		assertEquals(MathUtil.absLimit(Long.MIN_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.absLimit(Long.MAX_VALUE), Long.MAX_VALUE);
	}

	@Test
	public void testAddLimitInt() {
		assertEquals(MathUtil.addLimit(Integer.MAX_VALUE, Integer.MIN_VALUE), -1);
		assertEquals(MathUtil.addLimit(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.addLimit(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE);
	}

	@Test
	public void testAddLimitLong() {
		assertEquals(MathUtil.addLimit(Long.MAX_VALUE, Long.MIN_VALUE), -1L);
		assertEquals(MathUtil.addLimit(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.addLimit(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE);
	}

	@Test
	public void testSubtractLimitInt() {
		assertEquals(MathUtil.subtractLimit(Integer.MIN_VALUE, Integer.MIN_VALUE), 0);
		assertEquals(MathUtil.subtractLimit(Integer.MAX_VALUE, Integer.MIN_VALUE),
			Integer.MAX_VALUE);
		assertEquals(MathUtil.subtractLimit(Integer.MIN_VALUE, Integer.MAX_VALUE),
			Integer.MIN_VALUE);
	}

	@Test
	public void testSubtractLimitLong() {
		assertEquals(MathUtil.subtractLimit(Long.MIN_VALUE, Long.MIN_VALUE), 0L);
		assertEquals(MathUtil.subtractLimit(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.subtractLimit(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE);
	}

	@Test
	public void testMultiplyLimitInt() {
		assertEquals(MathUtil.multiplyLimit(Integer.MIN_VALUE, Integer.MIN_VALUE),
			Integer.MAX_VALUE);
		assertEquals(MathUtil.multiplyLimit(Integer.MIN_VALUE, Integer.MAX_VALUE),
			Integer.MIN_VALUE);
		assertEquals(MathUtil.multiplyLimit(Integer.MAX_VALUE, Integer.MAX_VALUE),
			Integer.MAX_VALUE);
		assertEquals(MathUtil.multiplyLimit(Integer.MIN_VALUE, 0), 0);
		assertEquals(MathUtil.multiplyLimit(Integer.MIN_VALUE, -1), Integer.MAX_VALUE);
	}

	@Test
	public void testMultiplyLimitLong() {
		assertEquals(MathUtil.multiplyLimit(Long.MIN_VALUE, Long.MIN_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.multiplyLimit(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE);
		assertEquals(MathUtil.multiplyLimit(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.multiplyLimit(Long.MIN_VALUE, 0), 0L);
		assertEquals(MathUtil.multiplyLimit(Long.MIN_VALUE, -1), Long.MAX_VALUE);
		assertEquals(MathUtil.multiplyLimit(0x100000000L, 1), 0x100000000L);
	}

	@Test
	public void testDecrementLimitInt() {
		assertEquals(MathUtil.decrementLimit(Integer.MAX_VALUE), Integer.MAX_VALUE - 1);
		assertEquals(MathUtil.decrementLimit(Integer.MIN_VALUE), Integer.MIN_VALUE);
	}

	@Test
	public void testDecrementLimitLong() {
		assertEquals(MathUtil.decrementLimit(Long.MAX_VALUE), Long.MAX_VALUE - 1);
		assertEquals(MathUtil.decrementLimit(Long.MIN_VALUE), Long.MIN_VALUE);
	}

	@Test
	public void testIncrementLimitInt() {
		assertEquals(MathUtil.incrementLimit(Integer.MIN_VALUE), Integer.MIN_VALUE + 1);
		assertEquals(MathUtil.incrementLimit(Integer.MAX_VALUE), Integer.MAX_VALUE);
	}

	@Test
	public void testIncrementLimitLong() {
		assertEquals(MathUtil.incrementLimit(Long.MIN_VALUE), Long.MIN_VALUE + 1);
		assertEquals(MathUtil.incrementLimit(Long.MAX_VALUE), Long.MAX_VALUE);
	}

	@Test
	public void testNegateInt() {
		assertEquals(MathUtil.negateLimit(Integer.MAX_VALUE), Integer.MIN_VALUE + 1);
		assertEquals(MathUtil.negateLimit(Integer.MIN_VALUE), Integer.MAX_VALUE);
	}

	@Test
	public void testNegateLong() {
		assertEquals(MathUtil.negateLimit(Long.MAX_VALUE), Long.MIN_VALUE + 1);
		assertEquals(MathUtil.negateLimit(Long.MIN_VALUE), Long.MAX_VALUE);
	}

	@Test
	public void testToIntLimit() {
		assertEquals(MathUtil.toIntLimit(Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(MathUtil.toIntLimit(Long.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.toIntLimit(Long.MIN_VALUE), Integer.MIN_VALUE);
	}

	@Test
	public void testToIntLimitDouble() {
		assertEquals(MathUtil.toIntLimit((double) Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.toIntLimit(Double.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.toIntLimit(-Double.MAX_VALUE), Integer.MIN_VALUE);
	}

	@Test
	public void testToLongLimitDouble() {
		assertEquals(MathUtil.toLongLimit(Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.toLongLimit(Double.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.toLongLimit(-Double.MAX_VALUE), Long.MIN_VALUE);
	}

	@Test
	public void testSafeToInt() {
		double d0 = Integer.MIN_VALUE;
		double d1 = Integer.MAX_VALUE;
		assertEquals(MathUtil.safeToInt(d0), Integer.MIN_VALUE);
		assertEquals(MathUtil.safeToInt(d1), Integer.MAX_VALUE);
		assertThrown(() -> MathUtil.safeToInt(Double.NaN));
		assertThrown(() -> MathUtil.safeToInt(Double.POSITIVE_INFINITY));
		assertThrown(() -> MathUtil.safeToInt(Double.NEGATIVE_INFINITY));
		assertThrown(() -> MathUtil.safeToInt(Double.MAX_VALUE));
	}

	@Test
	public void testSafeToLong() {
		double d0 = Long.MIN_VALUE;
		double d1 = Long.MAX_VALUE;
		assertEquals(MathUtil.safeToLong(d0), Long.MIN_VALUE);
		assertEquals(MathUtil.safeToLong(d1), Long.MAX_VALUE);
		assertThrown(() -> MathUtil.safeToLong(Double.NaN));
		assertThrown(() -> MathUtil.safeToLong(Double.POSITIVE_INFINITY));
		assertThrown(() -> MathUtil.safeToLong(Double.NEGATIVE_INFINITY));
		assertThrown(() -> MathUtil.safeToLong(Double.MAX_VALUE));
	}

	@Test
	public void testByteExact() {
		long l0 = Byte.MIN_VALUE;
		long l1 = Byte.MAX_VALUE;
		assertEquals(MathUtil.byteExact(l0), Byte.MIN_VALUE);
		assertEquals(MathUtil.byteExact(l1), Byte.MAX_VALUE);
		assertThrown(() -> MathUtil.byteExact(Byte.MIN_VALUE - 1));
		assertThrown(() -> MathUtil.byteExact(Byte.MAX_VALUE + 1));
	}

	@Test
	public void testShortExact() {
		long l0 = Short.MIN_VALUE;
		long l1 = Short.MAX_VALUE;
		assertEquals(MathUtil.shortExact(l0), Short.MIN_VALUE);
		assertEquals(MathUtil.shortExact(l1), Short.MAX_VALUE);
		assertThrown(() -> MathUtil.shortExact(Short.MIN_VALUE - 1));
		assertThrown(() -> MathUtil.shortExact(Short.MAX_VALUE + 1));
	}

	@Test
	public void testUbyteExact() {
		assertEquals(MathUtil.ubyteExact(0xff), (byte) 0xff);
		assertThrown(() -> MathUtil.ubyteExact(-1));
		assertThrown(() -> MathUtil.ubyteExact(0x100));
	}

	@Test
	public void testUshortExact() {
		assertEquals(MathUtil.ushortExact(0xffff), (short) 0xffff);
		assertThrown(() -> MathUtil.ushortExact(-1));
		assertThrown(() -> MathUtil.ushortExact(0x10000));
	}

	@Test
	public void testUintExact() {
		assertEquals(MathUtil.uintExact(0xffffffffL), 0xffffffff);
		assertThrown(() -> MathUtil.uintExact(-1));
		assertThrown(() -> MathUtil.uintExact(0x100000000L));
	}

	@Test
	public void testUbyte() {
		assertEquals(MathUtil.ubyte((byte) 0xff), (short) 0xff);
	}

	@Test
	public void testUshort() {
		assertEquals(MathUtil.ushort((short) 0xffff), 0xffff);
	}

	@Test
	public void testUint() {
		assertEquals(MathUtil.uint(0xffffffff), 0xffffffffL);
	}

	@Test
	public void testIntRoundExact() {
		double d0 = Integer.MIN_VALUE;
		double d1 = Integer.MAX_VALUE;
		assertEquals(MathUtil.intRoundExact(d0), Integer.MIN_VALUE);
		assertEquals(MathUtil.intRoundExact(d1), Integer.MAX_VALUE);
		assertThrown(() -> MathUtil.intRoundExact(d0 - 1));
		assertThrown(() -> MathUtil.intRoundExact(d1 + 1));
	}

	@Test
	public void testCeilDivInt() {
		assertEquals(MathUtil.ceilDiv(10, 4), 3);
		assertEquals(MathUtil.ceilDiv(10, 3), 4);
		assertEquals(MathUtil.ceilDiv(10, -4), -2);
		assertEquals(MathUtil.ceilDiv(10, -3), -3);
		assertEquals(MathUtil.ceilDiv(-10, 4), -2);
		assertEquals(MathUtil.ceilDiv(-10, 3), -3);
		assertEquals(MathUtil.ceilDiv(-10, -4), 3);
		assertEquals(MathUtil.ceilDiv(-10, -3), 4);
		assertEquals(MathUtil.ceilDiv(Integer.MAX_VALUE, Integer.MAX_VALUE), 1);
		assertEquals(MathUtil.ceilDiv(Integer.MAX_VALUE, Integer.MIN_VALUE), 0);
		assertEquals(MathUtil.ceilDiv(Integer.MIN_VALUE, Integer.MAX_VALUE), -1);
		assertEquals(MathUtil.ceilDiv(Integer.MIN_VALUE, Integer.MIN_VALUE), 1);
		assertThrown(() -> MathUtil.ceilDiv(1, 0));
	}

	@Test
	public void testCeilDivLong() {
		assertEquals(MathUtil.ceilDiv(10L, 4L), 3L);
		assertEquals(MathUtil.ceilDiv(10L, 3L), 4L);
		assertEquals(MathUtil.ceilDiv(10L, -4L), -2L);
		assertEquals(MathUtil.ceilDiv(10L, -3L), -3L);
		assertEquals(MathUtil.ceilDiv(-10L, 4L), -2L);
		assertEquals(MathUtil.ceilDiv(-10L, 3L), -3L);
		assertEquals(MathUtil.ceilDiv(-10L, -4L), 3L);
		assertEquals(MathUtil.ceilDiv(-10L, -3L), 4L);
		assertEquals(MathUtil.ceilDiv(Long.MAX_VALUE, Long.MAX_VALUE), 1L);
		assertEquals(MathUtil.ceilDiv(Long.MAX_VALUE, Long.MIN_VALUE), 0L);
		assertEquals(MathUtil.ceilDiv(Long.MIN_VALUE, Long.MAX_VALUE), -1L);
		assertEquals(MathUtil.ceilDiv(Long.MIN_VALUE, Long.MIN_VALUE), 1L);
		assertThrown(() -> MathUtil.ceilDiv(1L, 0L));
	}

	@Test
	public void testRoundDivInt() {
		assertEquals(MathUtil.roundDiv(10, 4), 3);
		assertEquals(MathUtil.roundDiv(10, 3), 3);
		assertEquals(MathUtil.roundDiv(10, -4), -2);
		assertEquals(MathUtil.roundDiv(10, -3), -3);
		assertEquals(MathUtil.roundDiv(-10, 4), -2);
		assertEquals(MathUtil.roundDiv(-10, 3), -3);
		assertEquals(MathUtil.roundDiv(-10, -4), 3);
		assertEquals(MathUtil.roundDiv(-10, -3), 3);
		assertEquals(MathUtil.roundDiv(Integer.MAX_VALUE, Integer.MAX_VALUE), 1);
		assertEquals(MathUtil.roundDiv(Integer.MAX_VALUE, Integer.MIN_VALUE), -1);
		assertEquals(MathUtil.roundDiv(Integer.MIN_VALUE, Integer.MAX_VALUE), -1);
		assertEquals(MathUtil.roundDiv(Integer.MIN_VALUE, Integer.MIN_VALUE), 1);
		assertThrown(() -> MathUtil.roundDiv(1, 0));
	}

	@Test
	public void testRoundDivLong() {
		assertEquals(MathUtil.roundDiv(10L, 4L), 3L);
		assertEquals(MathUtil.roundDiv(10L, 3L), 3L);
		assertEquals(MathUtil.roundDiv(10L, -4L), -2L);
		assertEquals(MathUtil.roundDiv(10L, -3L), -3L);
		assertEquals(MathUtil.roundDiv(-10L, 4L), -2L);
		assertEquals(MathUtil.roundDiv(-10L, 3L), -3L);
		assertEquals(MathUtil.roundDiv(-10L, -4L), 3L);
		assertEquals(MathUtil.roundDiv(-10L, -3L), 3L);
		assertEquals(MathUtil.roundDiv(Long.MAX_VALUE, Long.MAX_VALUE), 1L);
		assertEquals(MathUtil.roundDiv(Long.MAX_VALUE, Long.MIN_VALUE), -1L);
		assertEquals(MathUtil.roundDiv(Long.MIN_VALUE, Long.MAX_VALUE), -1L);
		assertEquals(MathUtil.roundDiv(Long.MIN_VALUE, Long.MIN_VALUE), 1L);
		assertThrown(() -> MathUtil.roundDiv(1L, 0L));
	}

	@Test
	public void testRound() {
		assertEquals(MathUtil.round(1, 1000000000.15), 1000000000.2);
		assertEquals(MathUtil.round(1, -1000000000.15), -1000000000.2);
		assertEquals(MathUtil.round(1, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY);
		assertEquals(MathUtil.round(1, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY);
		assertEquals(MathUtil.round(1, Double.NaN), Double.NaN);
		assertThrown(() -> MathUtil.round(-1, 777.7777));
	}

	@Test
	public void testSimpleRound() {
		assertEquals(MathUtil.simpleRound(2, 11111.11111), 11111.11);
		assertEquals(MathUtil.simpleRound(10, 11111.11111), 11111.11111);
		assertEquals(MathUtil.simpleRound(2, 11111.111111111111111), 11111.11);
		assertEquals(MathUtil.simpleRound(3, 777.7777), 777.778);
		assertEquals(MathUtil.simpleRound(3, -777.7777), -777.778);
		assertTrue(Double.isNaN(MathUtil.simpleRound(0, Double.NaN)));
		assertThrown(() -> MathUtil.simpleRound(-1, 777.7777));
		assertThrown(() -> MathUtil.simpleRound(11, 777.7777));
	}

	@Test
	public void testSimpleRoundForOutOfRangeValues() {
		// Original values returned if out of range
		assertEquals(MathUtil.simpleRound(1, 1000000000.15), 1000000000.15);
		assertEquals(MathUtil.simpleRound(1, -1000000000.15), -1000000000.15);
		assertEquals(MathUtil.simpleRound(1, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY);
		assertEquals(MathUtil.simpleRound(1, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testApproxEqual() {
		assertTrue(MathUtil.approxEqual(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE));
		assertTrue(MathUtil.approxEqual(0.001, 0.0001, 0.1));
		assertFalse(MathUtil.approxEqual(0.001, 0.0001, 0.0001));
		assertTrue(MathUtil.approxEqual(0.0011, 0.0012, 0.0001));
		assertFalse(MathUtil.approxEqual(0.0011, 0.0012, 0.00009));
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
		assertEquals(MathUtil.overflow(l + r, l, r), overflow);
	}

	private static void assertOverflow(long l, long r, boolean overflow) {
		assertEquals(MathUtil.overflow(l + r, l, r), overflow);
	}

	@Test
	public void testRandomIntFill() {
		int bits = 0;
		for (int i = 0; i < 1000 && bits != 0xff; i++)
			bits |= 1 << MathUtil.random(0, 7);
		assertEquals(bits, 0xff);
	}

	@Test
	public void testRandomInt() {
		assertRange(MathUtil.random(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE,
			Integer.MAX_VALUE);
		assertRange(MathUtil.random(0, Integer.MAX_VALUE), 0, Integer.MAX_VALUE);
		assertRange(MathUtil.random(Integer.MIN_VALUE, 0), Integer.MIN_VALUE, 0);
		assertEquals(MathUtil.random(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.random(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE);
	}

	@Test
	public void testRandomLongFill() {
		int bits = 0;
		for (int i = 0; i < 1000 && bits != 0xff; i++)
			bits |= 1 << MathUtil.random(0L, 7L);
		assertEquals(bits, 0xff);
	}

	@Test
	public void testRandomLong() {
		assertRange(MathUtil.random(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE,
			Long.MAX_VALUE);
		assertRange(MathUtil.random(0L, Long.MAX_VALUE), 0L, Long.MAX_VALUE);
		assertRange(MathUtil.random(Long.MIN_VALUE, 0L), Long.MIN_VALUE, 0L);
		assertEquals(MathUtil.random(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.random(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE);
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
	public void testRandomList() {
		String s = MathUtil.random("1", "2", "3");
		assertTrue(Set.of("1", "2", "3").contains(s));
		assertEquals(MathUtil.random("1"), "1");
		assertNull(MathUtil.random(List.of()));
	}

	@Test
	public void testRandomSet() {
		Set<String> set = Set.of("1", "2", "3");
		String s = MathUtil.random(set);
		assertTrue(set.contains(s));
		assertEquals(MathUtil.random(Set.of("1")), "1");
		assertNull(MathUtil.random(Set.of()));
	}

	@Test
	public void testToPercent() {
		assertEquals(MathUtil.toPercent(0.9), 90.0);
		assertEquals(MathUtil.toPercent(90, 90), 100.0);
		assertEquals(MathUtil.toPercent(Double.MAX_VALUE, Double.MAX_VALUE), 100.0);
		assertTrue(Double.isNaN(MathUtil.toPercent(Long.MAX_VALUE, 0)));
	}

	@Test
	public void testFromPercent() {
		assertEquals(MathUtil.fromPercent(50), 0.5);
		assertEquals(MathUtil.fromPercent(50, 90), 45.0);
		assertEquals(MathUtil.fromPercent(100, Double.MAX_VALUE), Double.MAX_VALUE);
		assertEquals(MathUtil.fromPercent(Double.MAX_VALUE, 100), Double.MAX_VALUE);
	}

	@Test
	public void testGcdForInts() {
		assertEquals(MathUtil.gcd(60, 90, 45), 15);
		assertEquals(MathUtil.gcd(45, 60, 90), 15);
		assertEquals(MathUtil.gcd(90, 45, 60), 15);
		assertEquals(MathUtil.gcd(0, 0), 0);
		assertEquals(MathUtil.gcd(1, 0), 1);
		assertEquals(MathUtil.gcd(0, 1), 1);
		assertEquals(MathUtil.gcd(-1, 2), 1);
		assertEquals(MathUtil.gcd(1, 2), 1);
		assertEquals(MathUtil.gcd(2, -1), 1);
		assertEquals(MathUtil.gcd(2, 1), 1);
		assertEquals(MathUtil.gcd(4, 2), 2);
		assertEquals(MathUtil.gcd(2, 4), 2);
		assertEquals(MathUtil.gcd(99, -44), 11);
		assertEquals(MathUtil.gcd(99999, 22222), 11111);
		assertEquals(MathUtil.gcd(22222, 99999), 11111);
		assertEquals(MathUtil.gcd(-99999, 22222), 11111);
		assertEquals(MathUtil.gcd(99999, -22222), 11111);
		assertEquals(MathUtil.gcd(-99999, -22222), 11111);
		assertThrown(() -> MathUtil.gcd(Integer.MIN_VALUE, 0));
		assertEquals(MathUtil.gcd(Integer.MIN_VALUE, 1), 1);
		assertEquals(MathUtil.gcd(Integer.MAX_VALUE, 0), Integer.MAX_VALUE);
		assertEquals(MathUtil.gcd(Integer.MAX_VALUE, 1), 1);
		assertThrown(() -> MathUtil.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertEquals(MathUtil.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.gcd(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);
	}

	@Test
	public void testGcdForLongs() {
		assertThrown(() -> MathUtil.gcd(Long.MIN_VALUE, 0));
		assertEquals(MathUtil.gcd(Long.MIN_VALUE, 1), 1L);
		assertEquals(MathUtil.gcd(Long.MAX_VALUE, 0), Long.MAX_VALUE);
		assertEquals(MathUtil.gcd(Long.MAX_VALUE, 1), 1L);
		assertThrown(() -> MathUtil.gcd(Long.MIN_VALUE, Long.MIN_VALUE));
		assertEquals(MathUtil.gcd(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.gcd(Long.MIN_VALUE, Long.MAX_VALUE), 1L);
	}

	@Test
	public void testLcmForInts() {
		assertEquals(MathUtil.lcm(8, 12, 16, 24, 32), 96);
		assertEquals(MathUtil.lcm(16, 24, 32, 8, 12), 96);
		assertEquals(MathUtil.lcm(0, 0), 0);
		assertEquals(MathUtil.lcm(1, 0), 0);
		assertEquals(MathUtil.lcm(0, 1), 0);
		assertEquals(MathUtil.lcm(-1, 2), 2);
		assertEquals(MathUtil.lcm(1, -2), 2);
		assertEquals(MathUtil.lcm(2, -1), 2);
		assertEquals(MathUtil.lcm(-2, 1), 2);
		assertEquals(MathUtil.lcm(12, 4), 12);
		assertEquals(MathUtil.lcm(8, 16), 16);
		assertEquals(MathUtil.lcm(99, -44), 396);
		assertEquals(MathUtil.lcm(99999, 22222), 199998);
		assertEquals(MathUtil.lcm(22222, 99999), 199998);
		assertEquals(MathUtil.lcm(-99999, 22222), 199998);
		assertEquals(MathUtil.lcm(99999, -22222), 199998);
		assertEquals(MathUtil.lcm(-99999, -22222), 199998);
		assertEquals(MathUtil.lcm(Integer.MIN_VALUE, 0), 0);
		assertThrown(() -> MathUtil.lcm(Integer.MIN_VALUE, 1));
		assertEquals(MathUtil.lcm(Integer.MAX_VALUE, 0), 0);
		assertEquals(MathUtil.lcm(Integer.MAX_VALUE, 1), Integer.MAX_VALUE);
		assertThrown(() -> MathUtil.lcm(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertEquals(MathUtil.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertThrown(() -> MathUtil.lcm(Integer.MIN_VALUE, Integer.MAX_VALUE));
	}

	@Test
	public void testLcmForLongs() {
		assertEquals(MathUtil.lcm(Long.MIN_VALUE, 0), 0L);
		assertThrown(() -> MathUtil.lcm(Long.MIN_VALUE, 1L));
		assertEquals(MathUtil.lcm(Long.MAX_VALUE, 0), 0L);
		assertEquals(MathUtil.lcm(Long.MAX_VALUE, 1), Long.MAX_VALUE);
		assertThrown(() -> MathUtil.lcm(Long.MIN_VALUE, Long.MIN_VALUE));
		assertEquals(MathUtil.lcm(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE);
		assertThrown(() -> MathUtil.lcm(Long.MIN_VALUE, Long.MAX_VALUE));
	}

	@Test
	public void testMeanInt() {
		assertEquals(MathUtil.mean(-1), -1.0);
		assertEquals(MathUtil.mean(1, -1), 0.0);
		assertEquals(MathUtil.mean(1, -1, 3), 1.0);
		assertEquals(MathUtil.mean(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
			(double) Integer.MAX_VALUE);
		assertEquals(MathUtil.mean(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
			(double) Integer.MIN_VALUE);
		assertEquals(MathUtil.mean(Integer.MAX_VALUE, Integer.MIN_VALUE), -0.5);
		assertThrown(() -> MathUtil.mean(new int[0]));
		assertThrown(() -> MathUtil.mean(new int[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((int[]) null, 0, 1));
	}

	@Test
	public void testMeanLong() {
		assertEquals(MathUtil.mean(-1L), -1.0);
		assertEquals(MathUtil.mean(1L, -1L), 0.0);
		assertEquals(MathUtil.mean(1L, -1L, 3L), 1.0);
		assertEquals(MathUtil.mean(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE),
			(double) Long.MAX_VALUE);
		assertEquals(MathUtil.mean(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE),
			(double) Long.MIN_VALUE);
		assertEquals(MathUtil.mean(Long.MAX_VALUE, Long.MIN_VALUE), -0.5);
		assertThrown(() -> MathUtil.mean(new long[0]));
		assertThrown(() -> MathUtil.mean(new long[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((long[]) null, 0, 1));
	}

	@Test
	public void testMeanFloat() {
		assertEquals(MathUtil.mean(-1.0f), -1.0f);
		assertEquals(MathUtil.mean(1.0f, -1.0f), 0.0f);
		assertEquals(MathUtil.mean(1.0f, -1.0f, 3.0f), 1.0f);
		assertEquals(MathUtil.mean(Float.MAX_VALUE, -Float.MAX_VALUE), 0.0f);
		assertEquals(MathUtil.mean(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
			Float.POSITIVE_INFINITY);
		assertEquals(MathUtil.mean(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NaN);
		assertThrown(() -> MathUtil.mean(new float[0]));
		assertThrown(() -> MathUtil.mean(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		assertThrown(() -> MathUtil.mean((float[]) null, 0, 1));
	}

	@Test
	public void testMeanDouble() {
		assertEquals(MathUtil.mean(-1.0), -1.0);
		assertEquals(MathUtil.mean(1.0, -1.0), 0.0);
		assertEquals(MathUtil.mean(1.0, -1.0, 3.0), 1.0);
		assertEquals(MathUtil.mean(Double.MAX_VALUE, -Double.MAX_VALUE), 0.0);
		assertEquals(MathUtil.mean(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
			Double.POSITIVE_INFINITY);
		assertEquals(MathUtil.mean(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NaN);
		assertThrown(() -> MathUtil.mean(new double[0]));
		assertThrown(() -> MathUtil.mean(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((double[]) null, 0, 1));
	}

	@Test
	public void testMedianInt() {
		assertEquals(MathUtil.median(-1), -1.0);
		assertEquals(MathUtil.median(1, -1), 0.0);
		assertEquals(MathUtil.median(1, -1, 3), 1.0);
		assertEquals(MathUtil.median(Integer.MAX_VALUE, Integer.MIN_VALUE), -0.5);
		assertEquals(MathUtil.median(Integer.MAX_VALUE, Integer.MAX_VALUE),
			(double) Integer.MAX_VALUE);
		assertEquals(MathUtil.median(Integer.MIN_VALUE, Integer.MIN_VALUE),
			(double) Integer.MIN_VALUE);
		assertEquals(MathUtil.median(Integer.MAX_VALUE - 1, Integer.MAX_VALUE),
			Integer.MAX_VALUE - 0.5);
		assertEquals(MathUtil.median(Integer.MIN_VALUE + 1, Integer.MIN_VALUE),
			Integer.MIN_VALUE + 0.5);
		assertThrown(() -> MathUtil.median(new int[0]));
		assertThrown(() -> MathUtil.median(new int[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.median((int[]) null, 0, 1));
	}

	@Test
	public void testMedianLong() {
		assertEquals(MathUtil.median(-1L), -1.0);
		assertEquals(MathUtil.median(1L, -1L), 0.0);
		assertEquals(MathUtil.median(1L, -1L, 3L), 1.0);
		assertEquals(MathUtil.median(Long.MAX_VALUE, Long.MIN_VALUE), -0.5);
		assertEquals(MathUtil.median(Long.MAX_VALUE, Long.MAX_VALUE), (double) Long.MAX_VALUE);
		assertEquals(MathUtil.median(Long.MIN_VALUE, Long.MIN_VALUE), (double) Long.MIN_VALUE);
		assertEquals(MathUtil.median(Long.MAX_VALUE - 1, Long.MAX_VALUE), Long.MAX_VALUE - 0.5);
		assertEquals(MathUtil.median(Long.MIN_VALUE + 1, Long.MIN_VALUE), Long.MIN_VALUE + 0.5);
		assertThrown(() -> MathUtil.median(new long[0]));
		assertThrown(() -> MathUtil.median(new long[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.median((long[]) null, 0, 1));
	}

	@Test
	public void testMedianFloat() {
		assertEquals(MathUtil.median(-1.0f), -1.0f);
		assertEquals(MathUtil.median(1.0f, -1.0f), 0.0f);
		assertEquals(MathUtil.median(1.0f, -1.0f, 3.0f), 1.0f);
		assertEquals(MathUtil.median(Float.MAX_VALUE, -Float.MAX_VALUE), 0.0f);
		assertEquals(MathUtil.median(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
			Float.POSITIVE_INFINITY);
		assertEquals(MathUtil.median(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NaN);
		assertThrown(() -> MathUtil.median(new float[0]));
		assertThrown(() -> MathUtil.median(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		assertThrown(() -> MathUtil.median((float[]) null, 0, 1));
	}

	@Test
	public void testMedianDouble() {
		assertEquals(MathUtil.median(-1.0), -1.0);
		assertEquals(MathUtil.median(1.0, -1.0), 0.0);
		assertEquals(MathUtil.median(1.0, -1.0, 3.0), 1.0);
		assertEquals(MathUtil.median(Double.MAX_VALUE, -Double.MAX_VALUE), 0.0);
		assertEquals(MathUtil.median(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
			Double.POSITIVE_INFINITY);
		assertEquals(MathUtil.median(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
			Double.NaN);
		assertThrown(() -> MathUtil.median(new double[0]));
		assertThrown(() -> MathUtil.median(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		assertThrown(() -> MathUtil.median((double[]) null, 0, 1));
	}

	@Test
	public void testLimitInt() {
		assertEquals(MathUtil.limit(2, -1, 1), 1);
		assertEquals(MathUtil.limit(-2, -1, 1), -1);
		assertEquals(MathUtil.limit(0, -1, 1), 0);
		assertEquals(MathUtil.limit(Integer.MAX_VALUE, 0, Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(MathUtil.limit(Integer.MIN_VALUE, 0, Integer.MAX_VALUE), 0);
		assertThrown(() -> MathUtil.limit(0, 1, 0));
	}

	@Test
	public void testLimitLong() {
		assertEquals(MathUtil.limit(2L, -1L, 1L), 1L);
		assertEquals(MathUtil.limit(-2L, -1L, 1L), -1L);
		assertEquals(MathUtil.limit(0L, -1L, 1L), 0L);
		assertEquals(MathUtil.limit(Long.MAX_VALUE, 0L, Long.MAX_VALUE), Long.MAX_VALUE);
		assertEquals(MathUtil.limit(Long.MIN_VALUE, 0L, Long.MAX_VALUE), 0L);
		assertThrown(() -> MathUtil.limit(0L, 1L, 0L));
	}

	@Test
	public void testLimitFloat() {
		assertEquals(MathUtil.limit(1.0f, -0.5f, -0.5f), -0.5f);
		assertEquals(MathUtil.limit(1.0f, -0.5f, 0.5f), 0.5f);
		assertEquals(MathUtil.limit(-1.0f, -0.5f, 0.5f), -0.5f);
		assertEquals(MathUtil.limit(0.0f, -0.5f, 0.5f), 0.0f);
		assertEquals(MathUtil.limit(Float.MAX_VALUE, 0.0f, Float.MAX_VALUE), Float.MAX_VALUE);
		assertEquals(MathUtil.limit(-Float.MAX_VALUE, 0.0f, Float.MAX_VALUE), 0.0f);
		assertEquals(MathUtil.limit(Float.POSITIVE_INFINITY, 0.0f, Float.MAX_VALUE),
			Float.MAX_VALUE);
		assertEquals(MathUtil.limit(Float.NEGATIVE_INFINITY, 0.0f, Float.MAX_VALUE), 0.0f);
		assertThrown(() -> MathUtil.limit(0.0f, 1.0f, 0.0f));
	}

	@Test
	public void testLimitDouble() {
		assertEquals(MathUtil.limit(1.0, -0.5, -0.5), -0.5);
		assertEquals(MathUtil.limit(1.0, -0.5, 0.5), 0.5);
		assertEquals(MathUtil.limit(-1.0, -0.5, 0.5), -0.5);
		assertEquals(MathUtil.limit(0.0, -0.5, 0.5), 0.0);
		assertEquals(MathUtil.limit(Double.MAX_VALUE, 0.0, Double.MAX_VALUE), Double.MAX_VALUE);
		assertEquals(MathUtil.limit(-Double.MAX_VALUE, 0.0, Double.MAX_VALUE), 0.0);
		assertEquals(MathUtil.limit(Double.POSITIVE_INFINITY, 0.0, Double.MAX_VALUE),
			Double.MAX_VALUE);
		assertEquals(MathUtil.limit(Double.NEGATIVE_INFINITY, 0.0, Double.MAX_VALUE), 0.0);
		assertThrown(() -> MathUtil.limit(0.0, 1.0, 0.0));
	}

	@Test
	public void testPeriodicLimitInt() {
		assertEquals(MathUtil.periodicLimit(100, 10, inclusive), 10);
		assertEquals(MathUtil.periodicLimit(100, 10, exclusive), 0);
		assertEquals(MathUtil.periodicLimit(-100, 10, inclusive), 0);
		assertEquals(MathUtil.periodicLimit(-100, 10, exclusive), 0);
		assertEquals(MathUtil.periodicLimit(7, 10, inclusive), 7);
		assertEquals(MathUtil.periodicLimit(-7, 10, inclusive), 3);
		assertEquals(MathUtil.periodicLimit(0, Integer.MAX_VALUE, inclusive), 0);
		assertEquals(MathUtil.periodicLimit(0, Integer.MAX_VALUE, exclusive), 0);
		assertEquals(MathUtil.periodicLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, inclusive),
			Integer.MAX_VALUE);
		assertEquals(MathUtil.periodicLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, exclusive), 0);
		assertEquals(MathUtil.periodicLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, inclusive),
			Integer.MAX_VALUE - 1);
		assertEquals(MathUtil.periodicLimit(Integer.MIN_VALUE, Integer.MAX_VALUE, exclusive),
			Integer.MAX_VALUE - 1);
		assertThrown(() -> MathUtil.periodicLimit(100, 10, null));
		assertThrown(() -> MathUtil.periodicLimit(100, 0, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100, -10, inclusive));
	}

	@Test
	public void testPeriodicLimitLong() {
		assertEquals(MathUtil.periodicLimit(100L, 10L, inclusive), 10L);
		assertEquals(MathUtil.periodicLimit(100L, 10L, exclusive), 0L);
		assertEquals(MathUtil.periodicLimit(-100L, 10L, inclusive), 0L);
		assertEquals(MathUtil.periodicLimit(-100L, 10L, exclusive), 0L);
		assertEquals(MathUtil.periodicLimit(7L, 10L, inclusive), 7L);
		assertEquals(MathUtil.periodicLimit(-7L, 10L, inclusive), 3L);
		assertEquals(MathUtil.periodicLimit(0L, Long.MAX_VALUE, inclusive), 0L);
		assertEquals(MathUtil.periodicLimit(0L, Long.MAX_VALUE, exclusive), 0L);
		assertEquals(MathUtil.periodicLimit(Long.MAX_VALUE, Long.MAX_VALUE, inclusive),
			Long.MAX_VALUE);
		assertEquals(MathUtil.periodicLimit(Long.MAX_VALUE, Long.MAX_VALUE, exclusive), 0L);
		assertEquals(MathUtil.periodicLimit(Long.MIN_VALUE, Long.MAX_VALUE, inclusive),
			Long.MAX_VALUE - 1);
		assertEquals(MathUtil.periodicLimit(Long.MIN_VALUE, Long.MAX_VALUE, exclusive),
			Long.MAX_VALUE - 1);
		assertThrown(() -> MathUtil.periodicLimit(100L, 10L, null));
		assertThrown(() -> MathUtil.periodicLimit(100L, 0L, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100L, -10L, inclusive));
	}

	@Test
	public void testPeriodicLimitFloat() {
		assertEquals(MathUtil.periodicLimit(100.0f, 10.0f, inclusive), 10.0f);
		assertEquals(MathUtil.periodicLimit(100.0f, 10.0f, exclusive), 0.0f);
		assertEquals(MathUtil.periodicLimit(-100.0f, 10.0f, inclusive), 0.0f);
		assertEquals(MathUtil.periodicLimit(-100.0f, 10.0f, exclusive), 0.0f);
		assertEquals(MathUtil.periodicLimit(7.0f, 10.0f, inclusive), 7.0f);
		assertEquals(MathUtil.periodicLimit(-7.0f, 10.0f, inclusive), 3.0f);
		assertEquals(MathUtil.periodicLimit(0.0f, Float.POSITIVE_INFINITY, inclusive), 0.0f);
		assertEquals(MathUtil.periodicLimit(0.0f, Float.POSITIVE_INFINITY, exclusive), 0.0f);
		assertEquals(
			MathUtil.periodicLimit(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, inclusive),
			Float.POSITIVE_INFINITY);
		assertEquals(
			MathUtil.periodicLimit(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, exclusive),
			Float.NaN);
		assertEquals(
			MathUtil.periodicLimit(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, inclusive),
			Float.NaN);
		assertThrown(() -> MathUtil.periodicLimit(100.0f, 10.0f, null));
		assertThrown(() -> MathUtil.periodicLimit(100.0f, 0.0f, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0f, -10.0f, inclusive));
	}

	@Test
	public void testPeriodicLimitDouble() {
		assertEquals(MathUtil.periodicLimit(100.0, 10.0, inclusive), 10.0);
		assertEquals(MathUtil.periodicLimit(100.0, 10.0, exclusive), 0.0);
		assertEquals(MathUtil.periodicLimit(-100.0, 10.0, inclusive), 0.0);
		assertEquals(MathUtil.periodicLimit(-100.0, 10.0, exclusive), 0.0);
		assertEquals(MathUtil.periodicLimit(7.0, 10.0, inclusive), 7.0);
		assertEquals(MathUtil.periodicLimit(-7.0, 10.0, inclusive), 3.0);
		assertEquals(MathUtil.periodicLimit(0.0, Double.POSITIVE_INFINITY, inclusive), 0.0);
		assertEquals(MathUtil.periodicLimit(0.0, Double.POSITIVE_INFINITY, exclusive), 0.0);
		assertEquals(MathUtil.periodicLimit( //
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, inclusive),
			Double.POSITIVE_INFINITY);
		assertNaN(MathUtil.periodicLimit( //
			Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, exclusive));
		assertNaN(
			MathUtil.periodicLimit(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0, 10.0, null));
		assertThrown(() -> MathUtil.periodicLimit(100.0, 0.0, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0, -10.0, inclusive));
	}

	@Test
	public void testMinByte() {
		byte[] array = { Byte.MIN_VALUE, -Byte.MAX_VALUE, -1, 0, 1, Byte.MAX_VALUE };
		assertEquals(MathUtil.min(array), Byte.MIN_VALUE);
		assertEquals(MathUtil.min(array, 1, 5), (byte) -Byte.MAX_VALUE);
		assertEquals(MathUtil.min(array, 2, 3), (byte) -1);
		assertThrown(() -> MathUtil.min(new byte[0]));
		assertThrown(() -> MathUtil.min(new byte[2], 1, 0));
		assertThrown(() -> MathUtil.min((byte[]) null));
		assertThrown(() -> MathUtil.min(new byte[3], 1, 3));
	}

	@Test
	public void testMinShort() {
		short[] array = { Short.MIN_VALUE, -Short.MAX_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertEquals(MathUtil.min(array), Short.MIN_VALUE);
		assertEquals(MathUtil.min(array, 1, 5), (short) -Short.MAX_VALUE);
		assertEquals(MathUtil.min(array, 2, 3), (short) -1);
		assertThrown(() -> MathUtil.min(new short[0]));
		assertThrown(() -> MathUtil.min(new short[2], 1, 0));
		assertThrown(() -> MathUtil.min((short[]) null));
		assertThrown(() -> MathUtil.min(new short[3], 1, 3));
	}

	@Test
	public void testMinInt() {
		int[] array = { Integer.MIN_VALUE, -Integer.MAX_VALUE, -1, 0, 1, Integer.MAX_VALUE };
		assertEquals(MathUtil.min(array), Integer.MIN_VALUE);
		assertEquals(MathUtil.min(array, 1, 5), -Integer.MAX_VALUE);
		assertEquals(MathUtil.min(array, 2, 3), -1);
		assertThrown(() -> MathUtil.min(new int[0]));
		assertThrown(() -> MathUtil.min(new int[2], 1, 0));
		assertThrown(() -> MathUtil.min((int[]) null));
		assertThrown(() -> MathUtil.min(new int[3], 1, 3));
	}

	@Test
	public void testMinLong() {
		long[] array = { Long.MIN_VALUE, -Long.MAX_VALUE, -1, 0, 1, Long.MAX_VALUE };
		assertEquals(MathUtil.min(array), Long.MIN_VALUE);
		assertEquals(MathUtil.min(array, 1, 5), -Long.MAX_VALUE);
		assertEquals(MathUtil.min(array, 2, 3), -1L);
		assertThrown(() -> MathUtil.min(new long[0]));
		assertThrown(() -> MathUtil.min(new long[2], 1, 0));
		assertThrown(() -> MathUtil.min((long[]) null));
		assertThrown(() -> MathUtil.min(new long[3], 1, 3));
	}

	@Test
	public void testMinFloat() {
		float[] d = { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, Float.MAX_VALUE, -1, 0, 1,
			Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NaN };
		assertEquals(MathUtil.min(d), Float.NaN);
		assertEquals(MathUtil.min(d, 0, 8), Float.NEGATIVE_INFINITY);
		assertEquals(MathUtil.min(d, 1, 7), -Float.MAX_VALUE);
		assertEquals(MathUtil.min(d, 3, 3), -1.0f);
		assertThrown(() -> MathUtil.min(new float[0]));
		assertThrown(() -> MathUtil.min(new float[2], 1, 0));
		assertThrown(() -> MathUtil.min((float[]) null));
		assertThrown(() -> MathUtil.min(new float[3], 1, 3));
	}

	@Test
	public void testMinDouble() {
		double[] d = { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, Double.MAX_VALUE, -1, 0, 1,
			Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NaN };
		assertEquals(MathUtil.min(d), Double.NaN);
		assertEquals(MathUtil.min(d, 0, 8), Double.NEGATIVE_INFINITY);
		assertEquals(MathUtil.min(d, 1, 7), -Double.MAX_VALUE);
		assertEquals(MathUtil.min(d, 3, 3), -1.0);
		assertThrown(() -> MathUtil.min(new double[0]));
		assertThrown(() -> MathUtil.min(new double[2], 1, 0));
		assertThrown(() -> MathUtil.min((double[]) null));
		assertThrown(() -> MathUtil.min(new double[3], 1, 3));
	}

	@Test
	public void testMaxByte() {
		byte[] array = { Byte.MIN_VALUE, -Byte.MAX_VALUE, -1, 0, 1, Byte.MAX_VALUE };
		assertEquals(MathUtil.max(array), Byte.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 2), (byte) -Byte.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 5), (byte) 1);
		assertThrown(() -> MathUtil.max(new byte[0]));
		assertThrown(() -> MathUtil.max(new byte[2], 1, 0));
		assertThrown(() -> MathUtil.max((byte[]) null));
		assertThrown(() -> MathUtil.max(new byte[3], 1, 3));
	}

	@Test
	public void testMaxShort() {
		short[] array = { Short.MIN_VALUE, -Short.MAX_VALUE, -1, 0, 1, Short.MAX_VALUE };
		assertEquals(MathUtil.max(array), Short.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 2), (short) -Short.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 5), (short) 1);
		assertThrown(() -> MathUtil.max(new short[0]));
		assertThrown(() -> MathUtil.max(new short[2], 1, 0));
		assertThrown(() -> MathUtil.max((short[]) null));
		assertThrown(() -> MathUtil.max(new short[3], 1, 3));
	}

	@Test
	public void testMaxInt() {
		int[] array = { Integer.MIN_VALUE, -Integer.MAX_VALUE, -1, 0, 1, Integer.MAX_VALUE };
		assertEquals(MathUtil.max(array), Integer.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 2), -Integer.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 5), 1);
		assertThrown(() -> MathUtil.max(new int[0]));
		assertThrown(() -> MathUtil.max(new int[2], 1, 0));
		assertThrown(() -> MathUtil.max((int[]) null));
		assertThrown(() -> MathUtil.max(new int[3], 1, 3));
	}

	@Test
	public void testMaxLong() {
		long[] array = { Long.MIN_VALUE, -Long.MAX_VALUE, -1, 0, 1, Long.MAX_VALUE };
		assertEquals(MathUtil.max(array), Long.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 2), -Long.MAX_VALUE);
		assertEquals(MathUtil.max(array, 0, 5), 1L);
		assertThrown(() -> MathUtil.max(new long[0]));
		assertThrown(() -> MathUtil.max(new long[2], 1, 0));
		assertThrown(() -> MathUtil.max((long[]) null));
		assertThrown(() -> MathUtil.max(new long[3], 1, 3));
	}

	@Test
	public void testMaxFloat() {
		float[] array = { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, Float.MAX_VALUE, -1, 0, 1,
			Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NaN };
		assertEquals(MathUtil.max(array), Float.NaN);
		assertEquals(MathUtil.max(array, 0, 8), Float.POSITIVE_INFINITY);
		assertEquals(MathUtil.max(array, 0, 7), Float.MAX_VALUE);
		assertEquals(MathUtil.max(array, 3, 3), 1.0f);
		assertThrown(() -> MathUtil.max(new float[0]));
		assertThrown(() -> MathUtil.max(new float[2], 1, 0));
		assertThrown(() -> MathUtil.max((float[]) null));
		assertThrown(() -> MathUtil.max(new float[3], 1, 3));
	}

	@Test
	public void testMaxDouble() {
		double[] array = { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, Double.MAX_VALUE, -1, 0, 1,
			Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NaN };
		assertEquals(MathUtil.max(array), Double.NaN);
		assertEquals(MathUtil.max(array, 0, 8), Double.POSITIVE_INFINITY);
		assertEquals(MathUtil.max(array, 0, 7), Double.MAX_VALUE);
		assertEquals(MathUtil.max(array, 3, 3), 1.0);
		assertThrown(() -> MathUtil.max(new double[0]));
		assertThrown(() -> MathUtil.max(new double[2], 1, 0));
		assertThrown(() -> MathUtil.max((double[]) null));
		assertThrown(() -> MathUtil.max(new double[3], 1, 3));
	}

}