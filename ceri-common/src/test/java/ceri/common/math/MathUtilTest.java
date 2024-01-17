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
	private static final byte BMAX = Byte.MAX_VALUE;
	private static final byte BMIN = Byte.MIN_VALUE;
	private static final short SMAX = Short.MAX_VALUE;
	private static final short SMIN = Short.MIN_VALUE;
	private static final int IMAX = Integer.MAX_VALUE;
	private static final int IMIN = Integer.MIN_VALUE;
	private static final long LMAX = Long.MAX_VALUE;
	private static final long LMIN = Long.MIN_VALUE;
	private static final float FMAX = Float.MAX_VALUE;
	private static final float FMIN = Float.MIN_VALUE;
	private static final double DMAX = Double.MAX_VALUE;
	private static final double DMIN = Double.MIN_VALUE;
	private static final float FNINF = Float.NEGATIVE_INFINITY;
	private static final float FPINF = Float.POSITIVE_INFINITY;
	private static final double DNINF = Double.NEGATIVE_INFINITY;
	private static final double DPINF = Double.POSITIVE_INFINITY;

	private static final int IHMAX = IMAX >> 1; // IHMAX << 1 = IMAX - 1
	private static final int IHMIN = IMIN >> 1; // IHMIN << 1 = IMIN
	private static final long LHMAX = LMAX >> 1; // LHMAX << 1 = LMAX - 1
	private static final long LHMIN = LMIN >> 1; // LHMIN << 1 = LMIN

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(MathUtil.class);
	}

	@Test
	public void testIntSin() {
		for (int i = -720; i <= 720; i += 1) {
			var x0 = (int) Math.round(Math.sin(Math.toRadians(i)) * 1000);
			var x = MathUtil.intSin(i, 1000);
			assertRange(x, x0 - 2, x0 + 2, "%d degrees", i); // within +/-.2%
		}
	}

	@Test
	public void testIntCos() {
		for (int i = -720; i <= 720; i += 1) {
			var x0 = (int) Math.round(Math.cos(Math.toRadians(i)) * 1000);
			var x = MathUtil.intCos(i, 1000);
			assertRange(x, x0 - 2, x0 + 2); // within +/-.2%
		}
	}

	@Test
	public void testIntSinFromRatio() {
		for (int i = -4000; i <= 4000; i += 5) {
			var x0 = MathUtil.intRoundExact(Math.sin(Math.toRadians(i * 180 / 1000.0)) * 1000);
			var x = MathUtil.intSinFromRatio(i, 1000, 1000);
			assertRange(x, x0 - 2, x0 + 2, "%d/%d", i, 1000); // within +/-.2%
		}
	}

	@Test
	public void testIntCosFromRatio() {
		for (int i = -4000; i <= 4000; i += 5) {
			var x0 = MathUtil.intRoundExact(Math.cos(Math.toRadians(i * 180 / 1000.0)) * 1000);
			var x = MathUtil.intCosFromRatio(i, 1000, 1000);
			assertRange(x, x0 - 2, x0 + 2, "%d/%d", i, 1000); // within +/-.2%
		}
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
	public void testUlog2() {
		assertEquals(MathUtil.ulog2(0), -1);
		assertEquals(MathUtil.ulog2(1), 0);
		assertEquals(MathUtil.ulog2(2), 1);
		assertEquals(MathUtil.ulog2(3), 1);
		assertEquals(MathUtil.ulog2(4), 2);
		assertEquals(MathUtil.ulog2(7), 2);
		assertEquals(MathUtil.ulog2(8), 3);
		assertEquals(MathUtil.ulog2(0x00000020), 5);
		assertEquals(MathUtil.ulog2(0x00000300), 9);
		assertEquals(MathUtil.ulog2(0x00004000), 14);
		assertEquals(MathUtil.ulog2(0x00050000), 18);
		assertEquals(MathUtil.ulog2(0x00600000), 22);
		assertEquals(MathUtil.ulog2(0x07000000), 26);
		assertEquals(MathUtil.ulog2(IMAX), 30);
		assertEquals(MathUtil.ulog2(0x80000000), 31);
		assertEquals(MathUtil.ulog2(0xffffffff), 31);
	}

	@Test
	public void testToInt() {
		assertEquals(MathUtil.toInt(true), 1);
		assertEquals(MathUtil.toInt(false), 0);
	}

	@Test
	public void testAbsLimitInt() {
		assertEquals(MathUtil.absLimit(-1), 1);
		assertEquals(MathUtil.absLimit(IMIN), IMAX);
		assertEquals(MathUtil.absLimit(IMAX), IMAX);
	}

	@Test
	public void testAbsLimitLong() {
		assertEquals(MathUtil.absLimit(-1L), 1L);
		assertEquals(MathUtil.absLimit(LMIN), LMAX);
		assertEquals(MathUtil.absLimit(LMAX), LMAX);
	}

	@Test
	public void testAddLimitInt() {
		assertEquals(MathUtil.addLimit(IMAX, IMIN), -1);
		assertEquals(MathUtil.addLimit(IMAX, IMAX), IMAX);
		assertEquals(MathUtil.addLimit(IMIN, IMIN), IMIN);
	}

	@Test
	public void testAddLimitLong() {
		assertEquals(MathUtil.addLimit(LMAX, LMIN), -1L);
		assertEquals(MathUtil.addLimit(LMAX, LMAX), LMAX);
		assertEquals(MathUtil.addLimit(LMIN, LMIN), LMIN);
	}

	@Test
	public void testSubtractLimitInt() {
		assertEquals(MathUtil.subtractLimit(IMIN, IMIN), 0);
		assertEquals(MathUtil.subtractLimit(IMAX, IMIN), IMAX);
		assertEquals(MathUtil.subtractLimit(IMIN, IMAX), IMIN);
	}

	@Test
	public void testSubtractLimitLong() {
		assertEquals(MathUtil.subtractLimit(LMIN, LMIN), 0L);
		assertEquals(MathUtil.subtractLimit(LMAX, LMIN), LMAX);
		assertEquals(MathUtil.subtractLimit(LMIN, LMAX), LMIN);
	}

	@Test
	public void testMultiplyLimitInt() {
		assertEquals(MathUtil.multiplyLimit(IMIN, IMIN), IMAX);
		assertEquals(MathUtil.multiplyLimit(IMIN, IMAX), IMIN);
		assertEquals(MathUtil.multiplyLimit(IMAX, IMAX), IMAX);
		assertEquals(MathUtil.multiplyLimit(IMIN, 0), 0);
		assertEquals(MathUtil.multiplyLimit(IMIN, -1), IMAX);
	}

	@Test
	public void testMultiplyLimitLong() {
		assertEquals(MathUtil.multiplyLimit(LMIN, LMIN), LMAX);
		assertEquals(MathUtil.multiplyLimit(LMIN, LMAX), LMIN);
		assertEquals(MathUtil.multiplyLimit(LMAX, LMAX), LMAX);
		assertEquals(MathUtil.multiplyLimit(LMIN, 0), 0L);
		assertEquals(MathUtil.multiplyLimit(LMIN, -1), LMAX);
		assertEquals(MathUtil.multiplyLimit(0x100000000L, 1), 0x100000000L);
	}

	@Test
	public void testDecrementLimitInt() {
		assertEquals(MathUtil.decrementLimit(IMAX), IMAX - 1);
		assertEquals(MathUtil.decrementLimit(IMIN), IMIN);
	}

	@Test
	public void testDecrementLimitLong() {
		assertEquals(MathUtil.decrementLimit(LMAX), LMAX - 1);
		assertEquals(MathUtil.decrementLimit(LMIN), LMIN);
	}

	@Test
	public void testIncrementLimitInt() {
		assertEquals(MathUtil.incrementLimit(IMIN), IMIN + 1);
		assertEquals(MathUtil.incrementLimit(IMAX), IMAX);
	}

	@Test
	public void testIncrementLimitLong() {
		assertEquals(MathUtil.incrementLimit(LMIN), LMIN + 1);
		assertEquals(MathUtil.incrementLimit(LMAX), LMAX);
	}

	@Test
	public void testNegateInt() {
		assertEquals(MathUtil.negateLimit(IMAX), IMIN + 1);
		assertEquals(MathUtil.negateLimit(IMIN), IMAX);
	}

	@Test
	public void testNegateLong() {
		assertEquals(MathUtil.negateLimit(LMAX), LMIN + 1);
		assertEquals(MathUtil.negateLimit(LMIN), LMAX);
	}

	@Test
	public void testToIntLimit() {
		assertEquals(MathUtil.toIntLimit(IMIN), IMIN);
		assertEquals(MathUtil.toIntLimit(LMAX), IMAX);
		assertEquals(MathUtil.toIntLimit(LMIN), IMIN);
	}

	@Test
	public void testToIntLimitDouble() {
		assertEquals(MathUtil.toIntLimit((double) IMAX), IMAX);
		assertEquals(MathUtil.toIntLimit(DMAX), IMAX);
		assertEquals(MathUtil.toIntLimit(-DMAX), IMIN);
	}

	@Test
	public void testToLongLimitDouble() {
		assertEquals(MathUtil.toLongLimit(LMAX), LMAX);
		assertEquals(MathUtil.toLongLimit(DMAX), LMAX);
		assertEquals(MathUtil.toLongLimit(-DMAX), LMIN);
	}

	@Test
	public void testSafeToInt() {
		double d0 = IMIN;
		double d1 = IMAX;
		assertEquals(MathUtil.safeToInt(d0), IMIN);
		assertEquals(MathUtil.safeToInt(d1), IMAX);
		assertThrown(() -> MathUtil.safeToInt(Double.NaN));
		assertThrown(() -> MathUtil.safeToInt(DPINF));
		assertThrown(() -> MathUtil.safeToInt(DNINF));
		assertThrown(() -> MathUtil.safeToInt(DMAX));
	}

	@Test
	public void testSafeToLong() {
		double d0 = LMIN;
		double d1 = LMAX;
		assertEquals(MathUtil.safeToLong(d0), LMIN);
		assertEquals(MathUtil.safeToLong(d1), LMAX);
		assertThrown(() -> MathUtil.safeToLong(Double.NaN));
		assertThrown(() -> MathUtil.safeToLong(DPINF));
		assertThrown(() -> MathUtil.safeToLong(DNINF));
		assertThrown(() -> MathUtil.safeToLong(DMAX));
	}

	@Test
	public void testByteExact() {
		long l0 = BMIN;
		long l1 = BMAX;
		assertEquals(MathUtil.byteExact(l0), BMIN);
		assertEquals(MathUtil.byteExact(l1), BMAX);
		assertThrown(() -> MathUtil.byteExact(BMIN - 1));
		assertThrown(() -> MathUtil.byteExact(BMAX + 1));
	}

	@Test
	public void testShortExact() {
		long l0 = SMIN;
		long l1 = SMAX;
		assertEquals(MathUtil.shortExact(l0), SMIN);
		assertEquals(MathUtil.shortExact(l1), SMAX);
		assertThrown(() -> MathUtil.shortExact(SMIN - 1));
		assertThrown(() -> MathUtil.shortExact(SMAX + 1));
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
		double d0 = IMIN;
		double d1 = IMAX;
		assertEquals(MathUtil.intRoundExact(d0), IMIN);
		assertEquals(MathUtil.intRoundExact(d1), IMAX);
		assertThrown(() -> MathUtil.intRoundExact(d0 - 1));
		assertThrown(() -> MathUtil.intRoundExact(d1 + 1));
	}

	@Test
	public void testRoundDivInt() {
		assertRoundDiv(1, 1, 1, -1, -1, 1);
		assertRoundDiv(1, 2, 1, 0, 0, 1);
		assertRoundDiv(1, 3, 0, 0, 0, 0);
		assertRoundDiv(2, 3, 1, -1, -1, 1);
		assertRoundDiv(1, 4, 0, 0, 0, 0);
		assertRoundDiv(2, 4, 1, 0, 0, 1);
		assertRoundDiv(3, 4, 1, -1, -1, 1);
		assertRoundDiv(2, 5, 0, 0, 0, 0);
		assertRoundDiv(3, 5, 1, -1, -1, 1);
		assertRoundDiv(2, 6, 0, 0, 0, 0);
		assertRoundDiv(3, 6, 1, 0, 0, 1);
		assertRoundDiv(4, 6, 1, -1, -1, 1);
		//
		assertRoundDiv(2, 1, 2, -2, -2, 2);
		assertRoundDiv(3, 2, 2, -1, -1, 2);
		assertRoundDiv(4, 3, 1, -1, -1, 1);
		assertRoundDiv(5, 3, 2, -2, -2, 2);
		assertRoundDiv(5, 4, 1, -1, -1, 1);
		assertRoundDiv(6, 4, 2, -1, -1, 2);
		assertRoundDiv(7, 4, 2, -2, -2, 2);
		assertRoundDiv(7, 5, 1, -1, -1, 1);
		assertRoundDiv(8, 5, 2, -2, -2, 2);
		assertRoundDiv(8, 6, 1, -1, -1, 1);
		assertRoundDiv(9, 6, 2, -1, -1, 2);
		assertRoundDiv(10, 6, 2, -2, -2, 2);
		//
		assertRoundDiv(5, 2, 3, -2, -2, 3);
		assertRoundDiv(7, 3, 2, -2, -2, 2);
		assertRoundDiv(8, 3, 3, -3, -3, 3);
		assertRoundDiv(9, 4, 2, -2, -2, 2);
		assertRoundDiv(10, 4, 3, -2, -2, 3);
		assertRoundDiv(11, 4, 3, -3, -3, 3);
		assertRoundDiv(12, 5, 2, -2, -2, 2);
		assertRoundDiv(13, 5, 3, -3, -3, 3);
		assertRoundDiv(14, 6, 2, -2, -2, 2);
		assertRoundDiv(15, 6, 3, -2, -2, 3);
		assertRoundDiv(16, 6, 3, -3, -3, 3);
	}

	@Test
	public void testRoundDivIntExtremes() {
		assertRoundDiv(IHMAX, 1, IHMAX, -IHMAX, -IHMAX, IHMAX);
		assertRoundDiv(IHMIN, 1, IHMIN, -IHMIN, -IHMIN, IHMIN);
		assertRoundDiv(IHMAX, IHMAX, 1, -1, -1, 1);
		assertRoundDiv(IHMAX, IHMIN, -1, 1, 1, -1);
		assertRoundDiv(IHMIN, IHMAX, -1, 1, 1, -1);
		assertRoundDiv(IHMIN, IHMIN, 1, -1, -1, 1);
		//
		assertRoundDiv(IHMAX, IMAX, 0, 0, 0, 0);
		assertRoundDiv(IHMAX + 1, IMAX, 1, -1, -1, 1);
		assertRoundDiv(IHMIN, IMAX, -1, 1, 1, -1);
		assertRoundDiv(IHMIN + 1, IMAX, 0, 0, 0, 0);
		assertEquals(MathUtil.roundDiv(IHMAX + 1, IMIN), 0);
		assertEquals(MathUtil.roundDiv(IHMAX + 2, IMIN), -1);
		assertEquals(MathUtil.roundDiv(-IHMAX, IMIN), 0);
		assertEquals(MathUtil.roundDiv(-IHMAX - 1, IMIN), 1);
		assertEquals(MathUtil.roundDiv(IHMIN + 1, IMIN), 0);
		assertEquals(MathUtil.roundDiv(IHMIN, IMIN), 1);
		assertEquals(MathUtil.roundDiv(-IHMIN, IMIN), 0);
		assertEquals(MathUtil.roundDiv(-IHMIN + 1, IMIN), -1);
		//
		assertRoundDiv(IMAX, IHMAX + 1, 2, -2, -2, 2);
		assertEquals(MathUtil.roundDiv(IMIN, IHMAX + 1), -2);
		assertEquals(MathUtil.roundDiv(IMIN, -IHMAX - 1), 2);
		assertRoundDiv(IMAX, IHMIN - 1, -2, 2, 2, -2);
		assertEquals(MathUtil.roundDiv(IMIN, IHMIN - 1), 2);
		assertEquals(MathUtil.roundDiv(IMIN, -IHMIN + 1), -2);
		//
		assertRoundDiv(IMAX, 1, IMAX, -IMAX, -IMAX, IMAX);
		assertEquals(MathUtil.roundDiv(IMIN, 1), IMIN);
		assertEquals(MathUtil.roundDiv(IMIN, -1), IMIN); // overflow?
		assertRoundDiv(IMAX, IMAX, 1, -1, -1, 1);
		assertEquals(MathUtil.roundDiv(IMAX, IMIN), -1);
		assertEquals(MathUtil.roundDiv(IMIN, IMAX), -1);
		assertEquals(MathUtil.roundDiv(IMIN, IMIN), 1);
		assertThrown(() -> MathUtil.roundDiv(1, 0));
	}

	@Test
	public void testRoundDivLong() {
		assertRoundDiv(1L, 1L, 1L, -1L, -1L, 1L);
		assertRoundDiv(1L, 2L, 1L, 0L, 0L, 1L);
		assertRoundDiv(1L, 3L, 0L, 0L, 0L, 0L);
		assertRoundDiv(2L, 3L, 1L, -1L, -1L, 1L);
		assertRoundDiv(1L, 4L, 0L, 0L, 0L, 0L);
		assertRoundDiv(2L, 4L, 1L, 0L, 0L, 1L);
		assertRoundDiv(3L, 4L, 1L, -1L, -1L, 1L);
		assertRoundDiv(2L, 5L, 0L, 0L, 0L, 0L);
		assertRoundDiv(3L, 5L, 1L, -1L, -1L, 1L);
		assertRoundDiv(2L, 6L, 0L, 0L, 0L, 0L);
		assertRoundDiv(3L, 6L, 1L, 0L, 0L, 1L);
		assertRoundDiv(4L, 6L, 1L, -1L, -1L, 1L);
		//
		assertRoundDiv(2L, 1L, 2L, -2L, -2L, 2L);
		assertRoundDiv(3L, 2L, 2L, -1L, -1L, 2L);
		assertRoundDiv(4L, 3L, 1L, -1L, -1L, 1L);
		assertRoundDiv(5L, 3L, 2L, -2L, -2L, 2L);
		assertRoundDiv(5L, 4L, 1L, -1L, -1L, 1L);
		assertRoundDiv(6L, 4L, 2L, -1L, -1L, 2L);
		assertRoundDiv(7L, 4L, 2L, -2L, -2L, 2L);
		assertRoundDiv(7L, 5L, 1L, -1L, -1L, 1L);
		assertRoundDiv(8L, 5L, 2L, -2L, -2L, 2L);
		assertRoundDiv(8L, 6L, 1L, -1L, -1L, 1L);
		assertRoundDiv(9L, 6L, 2L, -1L, -1L, 2L);
		assertRoundDiv(10L, 6L, 2L, -2L, -2L, 2L);
		//
		assertRoundDiv(5L, 2L, 3L, -2L, -2L, 3L);
		assertRoundDiv(7L, 3L, 2L, -2L, -2L, 2L);
		assertRoundDiv(8L, 3L, 3L, -3L, -3L, 3L);
		assertRoundDiv(9L, 4L, 2L, -2L, -2L, 2L);
		assertRoundDiv(10L, 4L, 3L, -2L, -2L, 3L);
		assertRoundDiv(11L, 4L, 3L, -3L, -3L, 3L);
		assertRoundDiv(12L, 5L, 2L, -2L, -2L, 2L);
		assertRoundDiv(13L, 5L, 3L, -3L, -3L, 3L);
		assertRoundDiv(14L, 6L, 2L, -2L, -2L, 2L);
		assertRoundDiv(15L, 6L, 3L, -2L, -2L, 3L);
		assertRoundDiv(16L, 6L, 3L, -3L, -3L, 3L);
	}

	@Test
	public void testRoundDivLongExtremes() {
		assertRoundDiv(LHMAX, 1, LHMAX, -LHMAX, -LHMAX, LHMAX);
		assertRoundDiv(LHMIN, 1, LHMIN, -LHMIN, -LHMIN, LHMIN);
		assertRoundDiv(LHMAX, LHMAX, 1, -1, -1, 1);
		assertRoundDiv(LHMAX, LHMIN, -1, 1, 1, -1);
		assertRoundDiv(LHMIN, LHMAX, -1, 1, 1, -1);
		assertRoundDiv(LHMIN, LHMIN, 1, -1, -1, 1);
		//
		assertRoundDiv(LHMAX, LMAX, 0, 0, 0, 0);
		assertRoundDiv(LHMAX + 1, LMAX, 1, -1, -1, 1);
		assertRoundDiv(LHMIN, LMAX, -1, 1, 1, -1);
		assertRoundDiv(LHMIN + 1, LMAX, 0, 0, 0, 0);
		assertEquals(MathUtil.roundDiv(LHMAX + 1, LMIN), 0L);
		assertEquals(MathUtil.roundDiv(LHMAX + 2, LMIN), -1L);
		assertEquals(MathUtil.roundDiv(-LHMAX, LMIN), 0L);
		assertEquals(MathUtil.roundDiv(-LHMAX - 1, LMIN), 1L);
		assertEquals(MathUtil.roundDiv(LHMIN + 1, LMIN), 0L);
		assertEquals(MathUtil.roundDiv(LHMIN, LMIN), 1L);
		assertEquals(MathUtil.roundDiv(-LHMIN, LMIN), 0L);
		assertEquals(MathUtil.roundDiv(-LHMIN + 1, LMIN), -1L);
		//
		assertRoundDiv(LMAX, LHMAX + 1, 2, -2, -2, 2);
		assertEquals(MathUtil.roundDiv(LMIN, LHMAX + 1), -2L);
		assertEquals(MathUtil.roundDiv(LMIN, -LHMAX - 1), 2L);
		assertRoundDiv(LMAX, LHMIN - 1, -2, 2, 2, -2);
		assertEquals(MathUtil.roundDiv(LMIN, LHMIN - 1), 2L);
		assertEquals(MathUtil.roundDiv(LMIN, -LHMIN + 1), -2L);
		//
		assertRoundDiv(LMAX, 1, LMAX, -LMAX, -LMAX, LMAX);
		assertEquals(MathUtil.roundDiv(LMIN, 1), LMIN);
		assertEquals(MathUtil.roundDiv(LMIN, -1), LMIN); // overflow?
		assertRoundDiv(LMAX, LMAX, 1, -1, -1, 1);
		assertEquals(MathUtil.roundDiv(LMAX, LMIN), -1L);
		assertEquals(MathUtil.roundDiv(LMIN, LMAX), -1L);
		assertEquals(MathUtil.roundDiv(LMIN, LMIN), 1L);
		assertThrown(() -> MathUtil.roundDiv(1L, 0L));
	}

	@Test
	public void testRound() {
		assertEquals(MathUtil.round(1, 1000000000.15), 1000000000.2);
		assertEquals(MathUtil.round(1, -1000000000.15), -1000000000.2);
		assertEquals(MathUtil.round(1, DPINF), DPINF);
		assertEquals(MathUtil.round(1, DNINF), DNINF);
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
		assertEquals(MathUtil.simpleRound(1, DPINF), DPINF);
		assertEquals(MathUtil.simpleRound(1, DNINF), DNINF);
	}

	@Test
	public void testApproxEqual() {
		assertTrue(MathUtil.approxEqual(DMIN, DMIN, DMIN));
		assertTrue(MathUtil.approxEqual(0.001, 0.0001, 0.1));
		assertFalse(MathUtil.approxEqual(0.001, 0.0001, 0.0001));
		assertTrue(MathUtil.approxEqual(0.0011, 0.0012, 0.0001));
		assertFalse(MathUtil.approxEqual(0.0011, 0.0012, 0.00009));
	}

	@Test
	public void testOverflowInt() {
		assertOverflow(IMAX, 0, false);
		assertOverflow(IMAX, 1, true);
		assertOverflow(IMAX, IMIN, false);
		assertOverflow(IMAX, IMAX, true);
		assertOverflow(IMIN, 0, false);
		assertOverflow(IMIN, -1, true);
		assertOverflow(IMIN, IMAX, false);
		assertOverflow(IMIN, IMIN, true);
	}

	@Test
	public void testOverflowLong() {
		assertOverflow(LMAX, 0L, false);
		assertOverflow(LMAX, 1L, true);
		assertOverflow(LMAX, LMIN, false);
		assertOverflow(LMAX, LMAX, true);
		assertOverflow(LMIN, 0L, false);
		assertOverflow(LMIN, -1L, true);
		assertOverflow(LMIN, LMAX, false);
		assertOverflow(LMIN, LMIN, true);
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
		assertRange(MathUtil.random(0), 0, 0);
		assertRange(MathUtil.random(1), 0, 1);
		assertRange(MathUtil.random(IMAX), 0, IMAX);
		assertRange(MathUtil.random(IMIN, IMAX), IMIN, IMAX);
		assertRange(MathUtil.random(0, IMAX), 0, IMAX);
		assertRange(MathUtil.random(IMIN, 0), IMIN, 0);
		assertEquals(MathUtil.random(IMAX, IMAX), IMAX);
		assertEquals(MathUtil.random(IMIN, IMIN), IMIN);
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
		assertRange(MathUtil.random(0L), 0, 0);
		assertRange(MathUtil.random(1L), 0, 1);
		assertRange(MathUtil.random(LMAX), 0, LMAX);
		assertRange(MathUtil.random(LMIN, LMAX), LMIN, LMAX);
		assertRange(MathUtil.random(0L, LMAX), 0L, LMAX);
		assertRange(MathUtil.random(LMIN, 0L), LMIN, 0L);
		assertEquals(MathUtil.random(LMAX, LMAX), LMAX);
		assertEquals(MathUtil.random(LMIN, LMIN), LMIN);
	}

	@Test
	public void testRandomDouble() {
		assertRange(MathUtil.random(), 0.0, 1.0);
		assertRange(MathUtil.random(0.1), 0.0, 0.1);
		assertRange(MathUtil.random(Double.MAX_VALUE), 0.0, Double.MAX_VALUE);
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
		assertEquals(MathUtil.toPercent(DMAX, DMAX), 100.0);
		assertTrue(Double.isNaN(MathUtil.toPercent(LMAX, 0)));
	}

	@Test
	public void testFromPercent() {
		assertEquals(MathUtil.fromPercent(50), 0.5);
		assertEquals(MathUtil.fromPercent(50, 90), 45.0);
		assertEquals(MathUtil.fromPercent(100, DMAX), DMAX);
		assertEquals(MathUtil.fromPercent(DMAX, 100), DMAX);
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
		assertThrown(() -> MathUtil.gcd(IMIN, 0));
		assertEquals(MathUtil.gcd(IMIN, 1), 1);
		assertEquals(MathUtil.gcd(IMAX, 0), IMAX);
		assertEquals(MathUtil.gcd(IMAX, 1), 1);
		assertThrown(() -> MathUtil.gcd(IMIN, IMIN));
		assertEquals(MathUtil.gcd(IMAX, IMAX), IMAX);
		assertEquals(MathUtil.gcd(IMIN, IMAX), 1);
	}

	@Test
	public void testGcdForLongs() {
		assertThrown(() -> MathUtil.gcd(LMIN, 0));
		assertEquals(MathUtil.gcd(LMIN, 1), 1L);
		assertEquals(MathUtil.gcd(LMAX, 0), LMAX);
		assertEquals(MathUtil.gcd(LMAX, 1), 1L);
		assertThrown(() -> MathUtil.gcd(LMIN, LMIN));
		assertEquals(MathUtil.gcd(LMAX, LMAX), LMAX);
		assertEquals(MathUtil.gcd(LMIN, LMAX), 1L);
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
		assertEquals(MathUtil.lcm(IMIN, 0), 0);
		assertThrown(() -> MathUtil.lcm(IMIN, 1));
		assertEquals(MathUtil.lcm(IMAX, 0), 0);
		assertEquals(MathUtil.lcm(IMAX, 1), IMAX);
		assertThrown(() -> MathUtil.lcm(IMIN, IMIN));
		assertEquals(MathUtil.lcm(IMAX, IMAX), IMAX);
		assertThrown(() -> MathUtil.lcm(IMIN, IMAX));
	}

	@Test
	public void testLcmForLongs() {
		assertEquals(MathUtil.lcm(LMIN, 0), 0L);
		assertThrown(() -> MathUtil.lcm(LMIN, 1L));
		assertEquals(MathUtil.lcm(LMAX, 0), 0L);
		assertEquals(MathUtil.lcm(LMAX, 1), LMAX);
		assertThrown(() -> MathUtil.lcm(LMIN, LMIN));
		assertEquals(MathUtil.lcm(LMAX, LMAX), LMAX);
		assertThrown(() -> MathUtil.lcm(LMIN, LMAX));
	}

	@Test
	public void testMeanInt() {
		assertEquals(MathUtil.mean(-1), -1.0);
		assertEquals(MathUtil.mean(1, -1), 0.0);
		assertEquals(MathUtil.mean(1, -1, 3), 1.0);
		assertEquals(MathUtil.mean(IMAX, IMAX, IMAX), (double) IMAX);
		assertEquals(MathUtil.mean(IMIN, IMIN, IMIN), (double) IMIN);
		assertEquals(MathUtil.mean(IMAX, IMIN), -0.5);
		assertThrown(() -> MathUtil.mean(new int[0]));
		assertThrown(() -> MathUtil.mean(new int[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((int[]) null, 0, 1));
	}

	@Test
	public void testMeanLong() {
		assertEquals(MathUtil.mean(-1L), -1.0);
		assertEquals(MathUtil.mean(1L, -1L), 0.0);
		assertEquals(MathUtil.mean(1L, -1L, 3L), 1.0);
		assertEquals(MathUtil.mean(LMAX, LMAX, LMAX), (double) LMAX);
		assertEquals(MathUtil.mean(LMIN, LMIN, LMIN), (double) LMIN);
		assertEquals(MathUtil.mean(LMAX, LMIN), -0.5);
		assertThrown(() -> MathUtil.mean(new long[0]));
		assertThrown(() -> MathUtil.mean(new long[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((long[]) null, 0, 1));
	}

	@Test
	public void testMeanFloat() {
		assertEquals(MathUtil.mean(-1.0f), -1.0f);
		assertEquals(MathUtil.mean(1.0f, -1.0f), 0.0f);
		assertEquals(MathUtil.mean(1.0f, -1.0f, 3.0f), 1.0f);
		assertEquals(MathUtil.mean(FMAX, -FMAX), 0.0f);
		assertEquals(MathUtil.mean(FPINF, FPINF), FPINF);
		assertEquals(MathUtil.mean(FNINF, FPINF), Float.NaN);
		assertThrown(() -> MathUtil.mean(new float[0]));
		assertThrown(() -> MathUtil.mean(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		assertThrown(() -> MathUtil.mean((float[]) null, 0, 1));
	}

	@Test
	public void testMeanDouble() {
		assertEquals(MathUtil.mean(-1.0), -1.0);
		assertEquals(MathUtil.mean(1.0, -1.0), 0.0);
		assertEquals(MathUtil.mean(1.0, -1.0, 3.0), 1.0);
		assertEquals(MathUtil.mean(DMAX, -DMAX), 0.0);
		assertEquals(MathUtil.mean(DPINF, DPINF), DPINF);
		assertEquals(MathUtil.mean(DNINF, DPINF), Double.NaN);
		assertThrown(() -> MathUtil.mean(new double[0]));
		assertThrown(() -> MathUtil.mean(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		assertThrown(() -> MathUtil.mean((double[]) null, 0, 1));
	}

	@Test
	public void testMedianInt() {
		assertEquals(MathUtil.median(-1), -1.0);
		assertEquals(MathUtil.median(1, -1), 0.0);
		assertEquals(MathUtil.median(1, -1, 3), 1.0);
		assertEquals(MathUtil.median(IMAX, IMIN), -0.5);
		assertEquals(MathUtil.median(IMAX, IMAX), (double) IMAX);
		assertEquals(MathUtil.median(IMIN, IMIN), (double) IMIN);
		assertEquals(MathUtil.median(IMAX - 1, IMAX), IMAX - 0.5);
		assertEquals(MathUtil.median(IMIN + 1, IMIN), IMIN + 0.5);
		assertThrown(() -> MathUtil.median(new int[0]));
		assertThrown(() -> MathUtil.median(new int[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.median((int[]) null, 0, 1));
	}

	@Test
	public void testMedianLong() {
		assertEquals(MathUtil.median(-1L), -1.0);
		assertEquals(MathUtil.median(1L, -1L), 0.0);
		assertEquals(MathUtil.median(1L, -1L, 3L), 1.0);
		assertEquals(MathUtil.median(LMAX, LMIN), -0.5);
		assertEquals(MathUtil.median(LMAX, LMAX), (double) LMAX);
		assertEquals(MathUtil.median(LMIN, LMIN), (double) LMIN);
		assertEquals(MathUtil.median(LMAX - 1, LMAX), LMAX - 0.5);
		assertEquals(MathUtil.median(LMIN + 1, LMIN), LMIN + 0.5);
		assertThrown(() -> MathUtil.median(new long[0]));
		assertThrown(() -> MathUtil.median(new long[] { 1, -1, 0 }, 2, 2));
		assertThrown(() -> MathUtil.median((long[]) null, 0, 1));
	}

	@Test
	public void testMedianFloat() {
		assertEquals(MathUtil.median(-1.0f), -1.0f);
		assertEquals(MathUtil.median(1.0f, -1.0f), 0.0f);
		assertEquals(MathUtil.median(1.0f, -1.0f, 3.0f), 1.0f);
		assertEquals(MathUtil.median(FMAX, -FMAX), 0.0f);
		assertEquals(MathUtil.median(FPINF, FPINF), FPINF);
		assertEquals(MathUtil.median(FNINF, FPINF), Float.NaN);
		assertThrown(() -> MathUtil.median(new float[0]));
		assertThrown(() -> MathUtil.median(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		assertThrown(() -> MathUtil.median((float[]) null, 0, 1));
	}

	@Test
	public void testMedianDouble() {
		assertEquals(MathUtil.median(-1.0), -1.0);
		assertEquals(MathUtil.median(1.0, -1.0), 0.0);
		assertEquals(MathUtil.median(1.0, -1.0, 3.0), 1.0);
		assertEquals(MathUtil.median(DMAX, -DMAX), 0.0);
		assertEquals(MathUtil.median(DPINF, DPINF), DPINF);
		assertEquals(MathUtil.median(DNINF, DPINF), Double.NaN);
		assertThrown(() -> MathUtil.median(new double[0]));
		assertThrown(() -> MathUtil.median(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		assertThrown(() -> MathUtil.median((double[]) null, 0, 1));
	}

	@Test
	public void testWithinInt() {
		assertEquals(MathUtil.within(0, 0, 0), true);
		assertEquals(MathUtil.within(0, -1, 1), true);
		assertEquals(MathUtil.within(0, 0, 1), true);
		assertEquals(MathUtil.within(0, -1, 0), true);
		assertEquals(MathUtil.within(1, -1, 0), false);
		assertEquals(MathUtil.within(-1, 0, 1), false);
		assertEquals(MathUtil.within(IMAX, IMIN, IMAX), true);
		assertEquals(MathUtil.within(IMAX, IMAX, IMAX), true);
		assertEquals(MathUtil.within(IMAX, IMIN, IMAX - 1), false);
		assertEquals(MathUtil.within(IMIN, IMIN, IMAX), true);
		assertEquals(MathUtil.within(IMIN, IMIN, IMIN), true);
		assertEquals(MathUtil.within(IMIN, IMIN + 1, IMAX), false);
	}

	@Test
	public void testWithinLong() {
		assertEquals(MathUtil.within(0L, 0, 0), true);
		assertEquals(MathUtil.within(0L, -1, 1), true);
		assertEquals(MathUtil.within(0L, 0, 1), true);
		assertEquals(MathUtil.within(0L, -1, 0), true);
		assertEquals(MathUtil.within(1L, -1, 0), false);
		assertEquals(MathUtil.within(-1L, 0, 1), false);
		assertEquals(MathUtil.within(LMAX, LMIN, LMAX), true);
		assertEquals(MathUtil.within(LMAX, LMAX, LMAX), true);
		assertEquals(MathUtil.within(LMAX, LMIN, LMAX - 1), false);
		assertEquals(MathUtil.within(LMIN, LMIN, LMAX), true);
		assertEquals(MathUtil.within(LMIN, LMIN, LMIN), true);
		assertEquals(MathUtil.within(LMIN, LMIN + 1, LMAX), false);
	}

	@Test
	public void testLimitInt() {
		assertEquals(MathUtil.limit(2, -1, 1), 1);
		assertEquals(MathUtil.limit(-2, -1, 1), -1);
		assertEquals(MathUtil.limit(0, -1, 1), 0);
		assertEquals(MathUtil.limit(IMAX, 0, IMAX), IMAX);
		assertEquals(MathUtil.limit(IMIN, 0, IMAX), 0);
		assertThrown(() -> MathUtil.limit(0, 1, 0));
	}

	@Test
	public void testLimitLong() {
		assertEquals(MathUtil.limit(2L, -1L, 1L), 1L);
		assertEquals(MathUtil.limit(-2L, -1L, 1L), -1L);
		assertEquals(MathUtil.limit(0L, -1L, 1L), 0L);
		assertEquals(MathUtil.limit(LMAX, 0L, LMAX), LMAX);
		assertEquals(MathUtil.limit(LMIN, 0L, LMAX), 0L);
		assertThrown(() -> MathUtil.limit(0L, 1L, 0L));
	}

	@Test
	public void testLimitFloat() {
		assertEquals(MathUtil.limit(1.0f, -0.5f, -0.5f), -0.5f);
		assertEquals(MathUtil.limit(1.0f, -0.5f, 0.5f), 0.5f);
		assertEquals(MathUtil.limit(-1.0f, -0.5f, 0.5f), -0.5f);
		assertEquals(MathUtil.limit(0.0f, -0.5f, 0.5f), 0.0f);
		assertEquals(MathUtil.limit(FMAX, 0.0f, FMAX), FMAX);
		assertEquals(MathUtil.limit(-FMAX, 0.0f, FMAX), 0.0f);
		assertEquals(MathUtil.limit(FPINF, 0.0f, FMAX), FMAX);
		assertEquals(MathUtil.limit(FNINF, 0.0f, FMAX), 0.0f);
		assertThrown(() -> MathUtil.limit(0.0f, 1.0f, 0.0f));
	}

	@Test
	public void testLimitDouble() {
		assertEquals(MathUtil.limit(1.0, -0.5, -0.5), -0.5);
		assertEquals(MathUtil.limit(1.0, -0.5, 0.5), 0.5);
		assertEquals(MathUtil.limit(-1.0, -0.5, 0.5), -0.5);
		assertEquals(MathUtil.limit(0.0, -0.5, 0.5), 0.0);
		assertEquals(MathUtil.limit(DMAX, 0.0, DMAX), DMAX);
		assertEquals(MathUtil.limit(-DMAX, 0.0, DMAX), 0.0);
		assertEquals(MathUtil.limit(DPINF, 0.0, DMAX), DMAX);
		assertEquals(MathUtil.limit(DNINF, 0.0, DMAX), 0.0);
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
		assertEquals(MathUtil.periodicLimit(0, IMAX, inclusive), 0);
		assertEquals(MathUtil.periodicLimit(0, IMAX, exclusive), 0);
		assertEquals(MathUtil.periodicLimit(IMAX, IMAX, inclusive), IMAX);
		assertEquals(MathUtil.periodicLimit(IMAX, IMAX, exclusive), 0);
		assertEquals(MathUtil.periodicLimit(IMIN, IMAX, inclusive), IMAX - 1);
		assertEquals(MathUtil.periodicLimit(IMIN, IMAX, exclusive), IMAX - 1);
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
		assertEquals(MathUtil.periodicLimit(0L, LMAX, inclusive), 0L);
		assertEquals(MathUtil.periodicLimit(0L, LMAX, exclusive), 0L);
		assertEquals(MathUtil.periodicLimit(LMAX, LMAX, inclusive), LMAX);
		assertEquals(MathUtil.periodicLimit(LMAX, LMAX, exclusive), 0L);
		assertEquals(MathUtil.periodicLimit(LMIN, LMAX, inclusive), LMAX - 1);
		assertEquals(MathUtil.periodicLimit(LMIN, LMAX, exclusive), LMAX - 1);
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
		assertEquals(MathUtil.periodicLimit(0.0f, FPINF, inclusive), 0.0f);
		assertEquals(MathUtil.periodicLimit(0.0f, FPINF, exclusive), 0.0f);
		assertEquals(MathUtil.periodicLimit(FPINF, FPINF, inclusive), FPINF);
		assertEquals(MathUtil.periodicLimit(FPINF, FPINF, exclusive), Float.NaN);
		assertEquals(MathUtil.periodicLimit(FNINF, FPINF, inclusive), Float.NaN);
		assertEquals(MathUtil.periodicLimit(FNINF, FPINF, exclusive), Float.NaN);
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
		assertEquals(MathUtil.periodicLimit(0.0, DPINF, inclusive), 0.0);
		assertEquals(MathUtil.periodicLimit(0.0, DPINF, exclusive), 0.0);
		assertEquals(MathUtil.periodicLimit(DPINF, DPINF, inclusive), DPINF);
		assertNaN(MathUtil.periodicLimit(DPINF, DPINF, exclusive));
		assertNaN(MathUtil.periodicLimit(DNINF, DPINF, inclusive));
		assertNaN(MathUtil.periodicLimit(DNINF, DPINF, exclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0, 10.0, null));
		assertThrown(() -> MathUtil.periodicLimit(100.0, 0.0, inclusive));
		assertThrown(() -> MathUtil.periodicLimit(100.0, -10.0, inclusive));
	}

	@Test
	public void testMinByte() {
		byte[] array = { BMIN, -BMAX, -1, 0, 1, BMAX };
		assertEquals(MathUtil.min(array), BMIN);
		assertEquals(MathUtil.min(array, 1, 5), (byte) -BMAX);
		assertEquals(MathUtil.min(array, 2, 3), (byte) -1);
		assertThrown(() -> MathUtil.min(new byte[0]));
		assertThrown(() -> MathUtil.min(new byte[2], 1, 0));
		assertThrown(() -> MathUtil.min((byte[]) null));
		assertThrown(() -> MathUtil.min(new byte[3], 1, 3));
	}

	@Test
	public void testMinShort() {
		short[] array = { SMIN, -SMAX, -1, 0, 1, SMAX };
		assertEquals(MathUtil.min(array), SMIN);
		assertEquals(MathUtil.min(array, 1, 5), (short) -SMAX);
		assertEquals(MathUtil.min(array, 2, 3), (short) -1);
		assertThrown(() -> MathUtil.min(new short[0]));
		assertThrown(() -> MathUtil.min(new short[2], 1, 0));
		assertThrown(() -> MathUtil.min((short[]) null));
		assertThrown(() -> MathUtil.min(new short[3], 1, 3));
	}

	@Test
	public void testMinInt() {
		int[] array = { IMIN, -IMAX, -1, 0, 1, IMAX };
		assertEquals(MathUtil.min(array), IMIN);
		assertEquals(MathUtil.min(array, 1, 5), -IMAX);
		assertEquals(MathUtil.min(array, 2, 3), -1);
		assertThrown(() -> MathUtil.min(new int[0]));
		assertThrown(() -> MathUtil.min(new int[2], 1, 0));
		assertThrown(() -> MathUtil.min((int[]) null));
		assertThrown(() -> MathUtil.min(new int[3], 1, 3));
	}

	@Test
	public void testMinLong() {
		long[] array = { LMIN, -LMAX, -1, 0, 1, LMAX };
		assertEquals(MathUtil.min(array), LMIN);
		assertEquals(MathUtil.min(array, 1, 5), -LMAX);
		assertEquals(MathUtil.min(array, 2, 3), -1L);
		assertThrown(() -> MathUtil.min(new long[0]));
		assertThrown(() -> MathUtil.min(new long[2], 1, 0));
		assertThrown(() -> MathUtil.min((long[]) null));
		assertThrown(() -> MathUtil.min(new long[3], 1, 3));
	}

	@Test
	public void testMinFloat() {
		float[] d = { FNINF, -FMAX, FMAX, -1, 0, 1, FMIN, FPINF, Float.NaN };
		assertEquals(MathUtil.min(d), Float.NaN);
		assertEquals(MathUtil.min(d, 0, 8), FNINF);
		assertEquals(MathUtil.min(d, 1, 7), -FMAX);
		assertEquals(MathUtil.min(d, 3, 3), -1.0f);
		assertThrown(() -> MathUtil.min(new float[0]));
		assertThrown(() -> MathUtil.min(new float[2], 1, 0));
		assertThrown(() -> MathUtil.min((float[]) null));
		assertThrown(() -> MathUtil.min(new float[3], 1, 3));
	}

	@Test
	public void testMinDouble() {
		double[] d = { DNINF, -DMAX, DMAX, -1, 0, 1, DMIN, DPINF, Double.NaN };
		assertEquals(MathUtil.min(d), Double.NaN);
		assertEquals(MathUtil.min(d, 0, 8), DNINF);
		assertEquals(MathUtil.min(d, 1, 7), -DMAX);
		assertEquals(MathUtil.min(d, 3, 3), -1.0);
		assertThrown(() -> MathUtil.min(new double[0]));
		assertThrown(() -> MathUtil.min(new double[2], 1, 0));
		assertThrown(() -> MathUtil.min((double[]) null));
		assertThrown(() -> MathUtil.min(new double[3], 1, 3));
	}

	@Test
	public void testMaxByte() {
		byte[] array = { BMIN, -BMAX, -1, 0, 1, BMAX };
		assertEquals(MathUtil.max(array), BMAX);
		assertEquals(MathUtil.max(array, 0, 2), (byte) -BMAX);
		assertEquals(MathUtil.max(array, 0, 5), (byte) 1);
		assertThrown(() -> MathUtil.max(new byte[0]));
		assertThrown(() -> MathUtil.max(new byte[2], 1, 0));
		assertThrown(() -> MathUtil.max((byte[]) null));
		assertThrown(() -> MathUtil.max(new byte[3], 1, 3));
	}

	@Test
	public void testMaxShort() {
		short[] array = { SMIN, -SMAX, -1, 0, 1, SMAX };
		assertEquals(MathUtil.max(array), SMAX);
		assertEquals(MathUtil.max(array, 0, 2), (short) -SMAX);
		assertEquals(MathUtil.max(array, 0, 5), (short) 1);
		assertThrown(() -> MathUtil.max(new short[0]));
		assertThrown(() -> MathUtil.max(new short[2], 1, 0));
		assertThrown(() -> MathUtil.max((short[]) null));
		assertThrown(() -> MathUtil.max(new short[3], 1, 3));
	}

	@Test
	public void testMaxInt() {
		int[] array = { IMIN, -IMAX, -1, 0, 1, IMAX };
		assertEquals(MathUtil.max(array), IMAX);
		assertEquals(MathUtil.max(array, 0, 2), -IMAX);
		assertEquals(MathUtil.max(array, 0, 5), 1);
		assertThrown(() -> MathUtil.max(new int[0]));
		assertThrown(() -> MathUtil.max(new int[2], 1, 0));
		assertThrown(() -> MathUtil.max((int[]) null));
		assertThrown(() -> MathUtil.max(new int[3], 1, 3));
	}

	@Test
	public void testMaxLong() {
		long[] array = { LMIN, -LMAX, -1, 0, 1, LMAX };
		assertEquals(MathUtil.max(array), LMAX);
		assertEquals(MathUtil.max(array, 0, 2), -LMAX);
		assertEquals(MathUtil.max(array, 0, 5), 1L);
		assertThrown(() -> MathUtil.max(new long[0]));
		assertThrown(() -> MathUtil.max(new long[2], 1, 0));
		assertThrown(() -> MathUtil.max((long[]) null));
		assertThrown(() -> MathUtil.max(new long[3], 1, 3));
	}

	@Test
	public void testMaxFloat() {
		float[] array = { FNINF, -FMAX, FMAX, -1, 0, 1, FMIN, FPINF, Float.NaN };
		assertEquals(MathUtil.max(array), Float.NaN);
		assertEquals(MathUtil.max(array, 0, 8), FPINF);
		assertEquals(MathUtil.max(array, 0, 7), FMAX);
		assertEquals(MathUtil.max(array, 3, 3), 1.0f);
		assertThrown(() -> MathUtil.max(new float[0]));
		assertThrown(() -> MathUtil.max(new float[2], 1, 0));
		assertThrown(() -> MathUtil.max((float[]) null));
		assertThrown(() -> MathUtil.max(new float[3], 1, 3));
	}

	@Test
	public void testMaxDouble() {
		double[] array = { DNINF, -DMAX, DMAX, -1, 0, 1, DMIN, DPINF, Double.NaN };
		assertEquals(MathUtil.max(array), Double.NaN);
		assertEquals(MathUtil.max(array, 0, 8), DPINF);
		assertEquals(MathUtil.max(array, 0, 7), DMAX);
		assertEquals(MathUtil.max(array, 3, 3), 1.0);
		assertThrown(() -> MathUtil.max(new double[0]));
		assertThrown(() -> MathUtil.max(new double[2], 1, 0));
		assertThrown(() -> MathUtil.max((double[]) null));
		assertThrown(() -> MathUtil.max(new double[3], 1, 3));
	}

	/**
	 * Tests positive and negative combinations of numerator and denominator.
	 */
	private void assertRoundDiv(int x, int y, int pp, int np, int pn, int nn) {
		assertEquals(MathUtil.roundDiv(x, y), pp);
		assertEquals(MathUtil.roundDiv(-x, y), np);
		assertEquals(MathUtil.roundDiv(x, -y), pn);
		assertEquals(MathUtil.roundDiv(-x, -y), nn);
	}

	/**
	 * Tests positive and negative combinations of numerator and denominator.
	 */
	private void assertRoundDiv(long x, long y, long pp, long np, long pn, long nn) {
		assertEquals(MathUtil.roundDiv(x, y), pp);
		assertEquals(MathUtil.roundDiv(-x, y), np);
		assertEquals(MathUtil.roundDiv(x, -y), pn);
		assertEquals(MathUtil.roundDiv(-x, -y), nn);
	}

	private static void assertOverflow(int l, int r, boolean overflow) {
		assertEquals(MathUtil.overflow(l + r, l, r), overflow);
	}

	private static void assertOverflow(long l, long r, boolean overflow) {
		assertEquals(MathUtil.overflow(l + r, l, r), overflow);
	}

}