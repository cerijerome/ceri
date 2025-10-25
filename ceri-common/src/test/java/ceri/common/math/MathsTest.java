package ceri.common.math;

import static ceri.common.math.Bound.Type.exc;
import static ceri.common.math.Bound.Type.inc;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertRange;
import static ceri.common.test.Assert.assertTrue;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import ceri.common.test.Assert;

public class MathsTest {
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
		assertPrivateConstructor(Maths.class);
	}

	@Test
	public void testDecimalDigits() {
		assertEquals(Maths.decimalDigits(Long.MIN_VALUE), 19);
		assertEquals(Maths.decimalDigits(Integer.MIN_VALUE), 10);
		assertEquals(Maths.decimalDigits(Byte.MIN_VALUE), 3);
		assertEquals(Maths.decimalDigits(-1), 1);
		assertEquals(Maths.decimalDigits(0), 0);
		assertEquals(Maths.decimalDigits(1), 1);
		assertEquals(Maths.decimalDigits(Byte.MAX_VALUE), 3);
		assertEquals(Maths.decimalDigits(Integer.MAX_VALUE), 10);
		assertEquals(Maths.decimalDigits(Long.MAX_VALUE), 19);
	}

	@Test
	public void testIntSin() {
		for (int i = -720; i <= 720; i += 1) {
			var x0 = (int) Math.round(Math.sin(Math.toRadians(i)) * 1000);
			var x = Maths.intSin(i, 1000);
			assertRange(x, x0 - 2, x0 + 2, "%d degrees", i); // within +/-.2%
		}
	}

	@Test
	public void testIntCos() {
		for (int i = -720; i <= 720; i += 1) {
			var x0 = (int) Math.round(Math.cos(Math.toRadians(i)) * 1000);
			var x = Maths.intCos(i, 1000);
			assertRange(x, x0 - 2, x0 + 2); // within +/-.2%
		}
	}

	@Test
	public void testIntSinFromRatio() {
		for (int i = -4000; i <= 4000; i += 5) {
			var x0 = Maths.intRoundExact(Math.sin(Math.toRadians(i * 180 / 1000.0)) * 1000);
			var x = Maths.intSinFromRatio(i, 1000, 1000);
			assertRange(x, x0 - 2, x0 + 2, "%d/%d", i, 1000); // within +/-.2%
		}
	}

	@Test
	public void testIntCosFromRatio() {
		for (int i = -4000; i <= 4000; i += 5) {
			var x0 = Maths.intRoundExact(Math.cos(Math.toRadians(i * 180 / 1000.0)) * 1000);
			var x = Maths.intCosFromRatio(i, 1000, 1000);
			assertRange(x, x0 - 2, x0 + 2, "%d/%d", i, 1000); // within +/-.2%
		}
	}

	@Test
	public void testDoublePolynomial() {
		assertEquals(Maths.polynomial(2.0), 0.0);
		assertEquals(Maths.polynomial(0.5, 3.0, 0.5, 0.1, 0.2), 3.3);
	}

	@Test
	public void testLongPolynomial() {
		assertEquals(Maths.polynomial(5), 0L);
		assertEquals(Maths.polynomial(2, 3, 0, 5, 1), 31L);
	}

	@Test
	public void testUlog2() {
		assertEquals(Maths.ulog2(0), -1);
		assertEquals(Maths.ulog2(1), 0);
		assertEquals(Maths.ulog2(2), 1);
		assertEquals(Maths.ulog2(3), 1);
		assertEquals(Maths.ulog2(4), 2);
		assertEquals(Maths.ulog2(7), 2);
		assertEquals(Maths.ulog2(8), 3);
		assertEquals(Maths.ulog2(0x00000020), 5);
		assertEquals(Maths.ulog2(0x00000300), 9);
		assertEquals(Maths.ulog2(0x00004000), 14);
		assertEquals(Maths.ulog2(0x00050000), 18);
		assertEquals(Maths.ulog2(0x00600000), 22);
		assertEquals(Maths.ulog2(0x07000000), 26);
		assertEquals(Maths.ulog2(IMAX), 30);
		assertEquals(Maths.ulog2(0x80000000), 31);
		assertEquals(Maths.ulog2(0xffffffff), 31);
	}

	@Test
	public void testToInt() {
		assertEquals(Maths.toInt(true), 1);
		assertEquals(Maths.toInt(false), 0);
	}

	@Test
	public void testAbsLimitInt() {
		assertEquals(Maths.absLimit(-1), 1);
		assertEquals(Maths.absLimit(IMIN), IMAX);
		assertEquals(Maths.absLimit(IMAX), IMAX);
	}

	@Test
	public void testAbsLimitLong() {
		assertEquals(Maths.absLimit(-1L), 1L);
		assertEquals(Maths.absLimit(LMIN), LMAX);
		assertEquals(Maths.absLimit(LMAX), LMAX);
	}

	@Test
	public void testAddLimitInt() {
		assertEquals(Maths.addLimit(IMAX, IMIN), -1);
		assertEquals(Maths.addLimit(IMAX, IMAX), IMAX);
		assertEquals(Maths.addLimit(IMIN, IMIN), IMIN);
	}

	@Test
	public void testAddLimitLong() {
		assertEquals(Maths.addLimit(LMAX, LMIN), -1L);
		assertEquals(Maths.addLimit(LMAX, LMAX), LMAX);
		assertEquals(Maths.addLimit(LMIN, LMIN), LMIN);
	}

	@Test
	public void testSubtractLimitInt() {
		assertEquals(Maths.subtractLimit(IMIN, IMIN), 0);
		assertEquals(Maths.subtractLimit(IMAX, IMIN), IMAX);
		assertEquals(Maths.subtractLimit(IMIN, IMAX), IMIN);
	}

	@Test
	public void testSubtractLimitLong() {
		assertEquals(Maths.subtractLimit(LMIN, LMIN), 0L);
		assertEquals(Maths.subtractLimit(LMAX, LMIN), LMAX);
		assertEquals(Maths.subtractLimit(LMIN, LMAX), LMIN);
	}

	@Test
	public void testMultiplyLimitInt() {
		assertEquals(Maths.multiplyLimit(IMIN, IMIN), IMAX);
		assertEquals(Maths.multiplyLimit(IMIN, IMAX), IMIN);
		assertEquals(Maths.multiplyLimit(IMAX, IMAX), IMAX);
		assertEquals(Maths.multiplyLimit(IMIN, 0), 0);
		assertEquals(Maths.multiplyLimit(IMIN, -1), IMAX);
	}

	@Test
	public void testMultiplyLimitLong() {
		assertEquals(Maths.multiplyLimit(LMIN, LMIN), LMAX);
		assertEquals(Maths.multiplyLimit(LMIN, LMAX), LMIN);
		assertEquals(Maths.multiplyLimit(LMAX, LMAX), LMAX);
		assertEquals(Maths.multiplyLimit(LMIN, 0), 0L);
		assertEquals(Maths.multiplyLimit(LMIN, -1), LMAX);
		assertEquals(Maths.multiplyLimit(0x100000000L, 1), 0x100000000L);
	}

	@Test
	public void testDecrementLimitInt() {
		assertEquals(Maths.decrementLimit(IMAX), IMAX - 1);
		assertEquals(Maths.decrementLimit(IMIN), IMIN);
	}

	@Test
	public void testDecrementLimitLong() {
		assertEquals(Maths.decrementLimit(LMAX), LMAX - 1);
		assertEquals(Maths.decrementLimit(LMIN), LMIN);
	}

	@Test
	public void testIncrementLimitInt() {
		assertEquals(Maths.incrementLimit(IMIN), IMIN + 1);
		assertEquals(Maths.incrementLimit(IMAX), IMAX);
	}

	@Test
	public void testIncrementLimitLong() {
		assertEquals(Maths.incrementLimit(LMIN), LMIN + 1);
		assertEquals(Maths.incrementLimit(LMAX), LMAX);
	}

	@Test
	public void testNegateInt() {
		assertEquals(Maths.negateLimit(IMAX), IMIN + 1);
		assertEquals(Maths.negateLimit(IMIN), IMAX);
	}

	@Test
	public void testNegateLong() {
		assertEquals(Maths.negateLimit(LMAX), LMIN + 1);
		assertEquals(Maths.negateLimit(LMIN), LMAX);
	}

	@Test
	public void testToIntLimit() {
		assertEquals(Maths.toIntLimit(IMIN), IMIN);
		assertEquals(Maths.toIntLimit(LMAX), IMAX);
		assertEquals(Maths.toIntLimit(LMIN), IMIN);
	}

	@Test
	public void testToIntLimitDouble() {
		assertEquals(Maths.toIntLimit((double) IMAX), IMAX);
		assertEquals(Maths.toIntLimit(DMAX), IMAX);
		assertEquals(Maths.toIntLimit(-DMAX), IMIN);
	}

	@Test
	public void testToLongLimitDouble() {
		assertEquals(Maths.toLongLimit(LMAX), LMAX);
		assertEquals(Maths.toLongLimit(DMAX), LMAX);
		assertEquals(Maths.toLongLimit(-DMAX), LMIN);
	}

	@Test
	public void testSafeToInt() {
		double d0 = IMIN;
		double d1 = IMAX;
		assertEquals(Maths.safeToInt(d0), IMIN);
		assertEquals(Maths.safeToInt(d1), IMAX);
		Assert.thrown(() -> Maths.safeToInt(Double.NaN));
		Assert.thrown(() -> Maths.safeToInt(DPINF));
		Assert.thrown(() -> Maths.safeToInt(DNINF));
		Assert.thrown(() -> Maths.safeToInt(DMAX));
	}

	@Test
	public void testSafeToLong() {
		double d0 = LMIN;
		double d1 = LMAX;
		assertEquals(Maths.safeToLong(d0), LMIN);
		assertEquals(Maths.safeToLong(d1), LMAX);
		Assert.thrown(() -> Maths.safeToLong(Double.NaN));
		Assert.thrown(() -> Maths.safeToLong(DPINF));
		Assert.thrown(() -> Maths.safeToLong(DNINF));
		Assert.thrown(() -> Maths.safeToLong(DMAX));
	}

	@Test
	public void testByteExact() {
		long l0 = BMIN;
		long l1 = BMAX;
		assertEquals(Maths.byteExact(l0), BMIN);
		assertEquals(Maths.byteExact(l1), BMAX);
		Assert.thrown(() -> Maths.byteExact(BMIN - 1));
		Assert.thrown(() -> Maths.byteExact(BMAX + 1));
	}

	@Test
	public void testShortExact() {
		long l0 = SMIN;
		long l1 = SMAX;
		assertEquals(Maths.shortExact(l0), SMIN);
		assertEquals(Maths.shortExact(l1), SMAX);
		Assert.thrown(() -> Maths.shortExact(SMIN - 1));
		Assert.thrown(() -> Maths.shortExact(SMAX + 1));
	}

	@Test
	public void testUbyteExact() {
		assertEquals(Maths.ubyteExact(0xff), (byte) 0xff);
		Assert.thrown(() -> Maths.ubyteExact(-1));
		Assert.thrown(() -> Maths.ubyteExact(0x100));
	}

	@Test
	public void testUshortExact() {
		assertEquals(Maths.ushortExact(0xffff), (short) 0xffff);
		Assert.thrown(() -> Maths.ushortExact(-1));
		Assert.thrown(() -> Maths.ushortExact(0x10000));
	}

	@Test
	public void testUintExact() {
		assertEquals(Maths.uintExact(0xffffffffL), 0xffffffff);
		Assert.thrown(() -> Maths.uintExact(-1));
		Assert.thrown(() -> Maths.uintExact(0x100000000L));
	}

	@Test
	public void testUbyte() {
		assertEquals(Maths.ubyte((byte) 0xff), (short) 0xff);
	}

	@Test
	public void testUshort() {
		assertEquals(Maths.ushort((short) 0xffff), 0xffff);
	}

	@Test
	public void testUint() {
		assertEquals(Maths.uint(0xffffffff), 0xffffffffL);
	}

	@Test
	public void testIntRoundExact() {
		double d0 = IMIN;
		double d1 = IMAX;
		assertEquals(Maths.intRoundExact(d0), IMIN);
		assertEquals(Maths.intRoundExact(d1), IMAX);
		Assert.thrown(() -> Maths.intRoundExact(d0 - 1));
		Assert.thrown(() -> Maths.intRoundExact(d1 + 1));
	}

	@Test
	public void testSafeRound() {
		assertEquals(Maths.safeRound(LMAX), LMAX);
		assertEquals(Maths.safeRound(LMIN), LMIN);
		assertEquals(Maths.safeRound(LMAX + 1024.0), LMAX);
		assertEquals(Maths.safeRound(LMIN - 1024.0), LMIN);
		Assert.thrown(ArithmeticException.class, () -> Maths.safeRound(LMAX + 1025.0));
		Assert.thrown(ArithmeticException.class, () -> Maths.safeRound(LMIN - 1025.0));
	}

	@Test
	public void testSafeRoundInt() {
		assertEquals(Maths.safeRoundInt(IMAX), IMAX);
		assertEquals(Maths.safeRoundInt(IMIN), IMIN);
		assertEquals(Maths.safeRoundInt(IMAX + 0.499999), IMAX);
		assertEquals(Maths.safeRoundInt(IMIN - 0.5), IMIN);
		Assert.thrown(ArithmeticException.class, () -> Maths.safeRoundInt(IMAX + 0.5));
		Assert.thrown(ArithmeticException.class, () -> Maths.safeRoundInt(IMIN - 0.51));
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
		assertEquals(Maths.roundDiv(IHMAX + 1, IMIN), 0);
		assertEquals(Maths.roundDiv(IHMAX + 2, IMIN), -1);
		assertEquals(Maths.roundDiv(-IHMAX, IMIN), 0);
		assertEquals(Maths.roundDiv(-IHMAX - 1, IMIN), 1);
		assertEquals(Maths.roundDiv(IHMIN + 1, IMIN), 0);
		assertEquals(Maths.roundDiv(IHMIN, IMIN), 1);
		assertEquals(Maths.roundDiv(-IHMIN, IMIN), 0);
		assertEquals(Maths.roundDiv(-IHMIN + 1, IMIN), -1);
		//
		assertRoundDiv(IMAX, IHMAX + 1, 2, -2, -2, 2);
		assertEquals(Maths.roundDiv(IMIN, IHMAX + 1), -2);
		assertEquals(Maths.roundDiv(IMIN, -IHMAX - 1), 2);
		assertRoundDiv(IMAX, IHMIN - 1, -2, 2, 2, -2);
		assertEquals(Maths.roundDiv(IMIN, IHMIN - 1), 2);
		assertEquals(Maths.roundDiv(IMIN, -IHMIN + 1), -2);
		//
		assertRoundDiv(IMAX, 1, IMAX, -IMAX, -IMAX, IMAX);
		assertEquals(Maths.roundDiv(IMIN, 1), IMIN);
		assertEquals(Maths.roundDiv(IMIN, -1), IMIN); // overflow?
		assertRoundDiv(IMAX, IMAX, 1, -1, -1, 1);
		assertEquals(Maths.roundDiv(IMAX, IMIN), -1);
		assertEquals(Maths.roundDiv(IMIN, IMAX), -1);
		assertEquals(Maths.roundDiv(IMIN, IMIN), 1);
		Assert.thrown(() -> Maths.roundDiv(1, 0));
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
		assertEquals(Maths.roundDiv(LHMAX + 1, LMIN), 0L);
		assertEquals(Maths.roundDiv(LHMAX + 2, LMIN), -1L);
		assertEquals(Maths.roundDiv(-LHMAX, LMIN), 0L);
		assertEquals(Maths.roundDiv(-LHMAX - 1, LMIN), 1L);
		assertEquals(Maths.roundDiv(LHMIN + 1, LMIN), 0L);
		assertEquals(Maths.roundDiv(LHMIN, LMIN), 1L);
		assertEquals(Maths.roundDiv(-LHMIN, LMIN), 0L);
		assertEquals(Maths.roundDiv(-LHMIN + 1, LMIN), -1L);
		//
		assertRoundDiv(LMAX, LHMAX + 1, 2, -2, -2, 2);
		assertEquals(Maths.roundDiv(LMIN, LHMAX + 1), -2L);
		assertEquals(Maths.roundDiv(LMIN, -LHMAX - 1), 2L);
		assertRoundDiv(LMAX, LHMIN - 1, -2, 2, 2, -2);
		assertEquals(Maths.roundDiv(LMIN, LHMIN - 1), 2L);
		assertEquals(Maths.roundDiv(LMIN, -LHMIN + 1), -2L);
		//
		assertRoundDiv(LMAX, 1, LMAX, -LMAX, -LMAX, LMAX);
		assertEquals(Maths.roundDiv(LMIN, 1), LMIN);
		assertEquals(Maths.roundDiv(LMIN, -1), LMIN); // overflow?
		assertRoundDiv(LMAX, LMAX, 1, -1, -1, 1);
		assertEquals(Maths.roundDiv(LMAX, LMIN), -1L);
		assertEquals(Maths.roundDiv(LMIN, LMAX), -1L);
		assertEquals(Maths.roundDiv(LMIN, LMIN), 1L);
		Assert.thrown(() -> Maths.roundDiv(1L, 0L));
	}

	@Test
	public void testRound() {
		assertEquals(Maths.round(1, 1000000.15), 1000000.2);
		assertEquals(Maths.round(1, -1000000.15), -1000000.1);
		assertEquals(Maths.round(1, 1000000000.15), 1000000000.2);
		assertEquals(Maths.round(1, -1000000000.15), -1000000000.1);
		assertEquals(Maths.round(1, 1000000000000.15), 1000000000000.2);
		assertEquals(Maths.round(1, -1000000000000.15), -1000000000000.1);
		assertEquals(Maths.round(1, DPINF), DPINF);
		assertEquals(Maths.round(1, DNINF), DNINF);
		assertEquals(Maths.round(1, Double.NaN), Double.NaN);
		Assert.thrown(() -> Maths.round(-1, 777.7777));
	}

	@Test
	public void testSimpleRound() {
		assertEquals(Maths.simpleRound(2, 11111.11111), 11111.11);
		assertEquals(Maths.simpleRound(10, 11111.11111), 11111.11111);
		assertEquals(Maths.simpleRound(2, 11111.111111111111111), 11111.11);
		assertEquals(Maths.simpleRound(3, 777.7777), 777.778);
		assertEquals(Maths.simpleRound(3, -777.7777), -777.778);
		assertTrue(Double.isNaN(Maths.simpleRound(0, Double.NaN)));
		Assert.thrown(() -> Maths.simpleRound(-1, 777.7777));
		Assert.thrown(() -> Maths.simpleRound(11, 777.7777));
	}

	@Test
	public void testSimpleRoundForOutOfRangeValues() {
		// Original values returned if out of range
		assertEquals(Maths.simpleRound(1, 1000000000.15), 1000000000.15);
		assertEquals(Maths.simpleRound(1, -1000000000.15), -1000000000.15);
		assertEquals(Maths.simpleRound(1, DPINF), DPINF);
		assertEquals(Maths.simpleRound(1, DNINF), DNINF);
	}

	@Test
	public void testEqualsDouble() {
		assertTrue(Maths.equals(Double.MIN_VALUE, Double.MIN_VALUE));
		assertTrue(Maths.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
		assertTrue(Maths.equals(Double.NaN, Double.NaN));
		assertFalse(Maths.equals(Double.MIN_VALUE, Double.MIN_NORMAL));
		assertFalse(Maths.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		assertFalse(Maths.equals(Double.NaN, Double.POSITIVE_INFINITY));
	}

	@Test
	public void testEqualsFloat() {
		assertTrue(Maths.equals(Float.MIN_VALUE, Float.MIN_VALUE));
		assertTrue(Maths.equals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		assertTrue(Maths.equals(Float.NaN, Float.NaN));
		assertFalse(Maths.equals(Float.MIN_VALUE, Float.MIN_NORMAL));
		assertFalse(Maths.equals(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
		assertFalse(Maths.equals(Float.NaN, Float.POSITIVE_INFINITY));
	}

	@Test
	public void testApproxEqual() {
		assertTrue(Maths.approxEqual(DMIN, DMIN, DMIN));
		assertTrue(Maths.approxEqual(0.001, 0.0001, 0.1));
		assertFalse(Maths.approxEqual(0.001, 0.0001, 0.0001));
		assertTrue(Maths.approxEqual(0.0011, 0.0012, 0.0001));
		assertFalse(Maths.approxEqual(0.0011, 0.0012, 0.00009));
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
			bits |= 1 << Maths.random(0, 7);
		assertEquals(bits, 0xff);
	}

	@Test
	public void testRandomInt() {
		assertRange(Maths.random(0), 0, 0);
		assertRange(Maths.random(1), 0, 1);
		assertRange(Maths.random(IMAX), 0, IMAX);
		assertRange(Maths.random(IMIN, IMAX), IMIN, IMAX);
		assertRange(Maths.random(0, IMAX), 0, IMAX);
		assertRange(Maths.random(IMIN, 0), IMIN, 0);
		assertEquals(Maths.random(IMAX, IMAX), IMAX);
		assertEquals(Maths.random(IMIN, IMIN), IMIN);
	}

	@Test
	public void testRandomLongFill() {
		int bits = 0;
		for (int i = 0; i < 1000 && bits != 0xff; i++)
			bits |= 1 << Maths.random(0L, 7L);
		assertEquals(bits, 0xff);
	}

	@Test
	public void testRandomLong() {
		assertRange(Maths.random(0L), 0, 0);
		assertRange(Maths.random(1L), 0, 1);
		assertRange(Maths.random(LMAX), 0, LMAX);
		assertRange(Maths.random(LMIN, LMAX), LMIN, LMAX);
		assertRange(Maths.random(0L, LMAX), 0L, LMAX);
		assertRange(Maths.random(LMIN, 0L), LMIN, 0L);
		assertEquals(Maths.random(LMAX, LMAX), LMAX);
		assertEquals(Maths.random(LMIN, LMIN), LMIN);
	}

	@Test
	public void testRandomDouble() {
		assertRange(Maths.random(), 0.0, 1.0);
		assertRange(Maths.random(0.1), 0.0, 0.1);
		assertRange(Maths.random(Double.MAX_VALUE), 0.0, Double.MAX_VALUE);
		assertRange(Maths.random(-1.0, 2.0), -1.0, 2.0);
		assertRange(Maths.random(-1.0, 0.0), -1.0, 0.0);
	}

	@Test
	public void testRandomList() {
		String s = Maths.random("1", "2", "3");
		assertTrue(Set.of("1", "2", "3").contains(s));
		assertEquals(Maths.random("1"), "1");
		Assert.isNull(Maths.random(List.of()));
	}

	@Test
	public void testRandomSet() {
		Set<String> set = Set.of("1", "2", "3");
		String s = Maths.random(set);
		assertTrue(set.contains(s));
		assertEquals(Maths.random(Set.of("1")), "1");
		Assert.isNull(Maths.random(Set.of()));
	}

	@Test
	public void testToPercent() {
		assertEquals(Maths.toPercent(0.9), 90.0);
		assertEquals(Maths.toPercent(90, 90), 100.0);
		assertEquals(Maths.toPercent(DMAX, DMAX), 100.0);
		assertTrue(Double.isNaN(Maths.toPercent(LMAX, 0)));
	}

	@Test
	public void testFromPercent() {
		assertEquals(Maths.fromPercent(50), 0.5);
		assertEquals(Maths.fromPercent(50, 90), 45.0);
		assertEquals(Maths.fromPercent(100, DMAX), DMAX);
		assertEquals(Maths.fromPercent(DMAX, 100), DMAX);
	}

	@Test
	public void testGcdForInts() {
		assertEquals(Maths.gcd(60, 90, 45), 15);
		assertEquals(Maths.gcd(45, 60, 90), 15);
		assertEquals(Maths.gcd(90, 45, 60), 15);
		assertEquals(Maths.gcd(0, 0), 0);
		assertEquals(Maths.gcd(1, 0), 1);
		assertEquals(Maths.gcd(0, 1), 1);
		assertEquals(Maths.gcd(-1, 2), 1);
		assertEquals(Maths.gcd(1, 2), 1);
		assertEquals(Maths.gcd(2, -1), 1);
		assertEquals(Maths.gcd(2, 1), 1);
		assertEquals(Maths.gcd(4, 2), 2);
		assertEquals(Maths.gcd(2, 4), 2);
		assertEquals(Maths.gcd(99, -44), 11);
		assertEquals(Maths.gcd(99999, 22222), 11111);
		assertEquals(Maths.gcd(22222, 99999), 11111);
		assertEquals(Maths.gcd(-99999, 22222), 11111);
		assertEquals(Maths.gcd(99999, -22222), 11111);
		assertEquals(Maths.gcd(-99999, -22222), 11111);
		Assert.thrown(() -> Maths.gcd(IMIN, 0));
		assertEquals(Maths.gcd(IMIN, 1), 1);
		assertEquals(Maths.gcd(IMAX, 0), IMAX);
		assertEquals(Maths.gcd(IMAX, 1), 1);
		Assert.thrown(() -> Maths.gcd(IMIN, IMIN));
		assertEquals(Maths.gcd(IMAX, IMAX), IMAX);
		assertEquals(Maths.gcd(IMIN, IMAX), 1);
	}

	@Test
	public void testGcdForLongs() {
		Assert.thrown(() -> Maths.gcd(LMIN, 0));
		assertEquals(Maths.gcd(LMIN, 1), 1L);
		assertEquals(Maths.gcd(LMAX, 0), LMAX);
		assertEquals(Maths.gcd(LMAX, 1), 1L);
		Assert.thrown(() -> Maths.gcd(LMIN, LMIN));
		assertEquals(Maths.gcd(LMAX, LMAX), LMAX);
		assertEquals(Maths.gcd(LMIN, LMAX), 1L);
	}

	@Test
	public void testLcmForInts() {
		assertEquals(Maths.lcm(8, 12, 16, 24, 32), 96);
		assertEquals(Maths.lcm(16, 24, 32, 8, 12), 96);
		assertEquals(Maths.lcm(0, 0), 0);
		assertEquals(Maths.lcm(1, 0), 0);
		assertEquals(Maths.lcm(0, 1), 0);
		assertEquals(Maths.lcm(-1, 2), 2);
		assertEquals(Maths.lcm(1, -2), 2);
		assertEquals(Maths.lcm(2, -1), 2);
		assertEquals(Maths.lcm(-2, 1), 2);
		assertEquals(Maths.lcm(12, 4), 12);
		assertEquals(Maths.lcm(8, 16), 16);
		assertEquals(Maths.lcm(99, -44), 396);
		assertEquals(Maths.lcm(99999, 22222), 199998);
		assertEquals(Maths.lcm(22222, 99999), 199998);
		assertEquals(Maths.lcm(-99999, 22222), 199998);
		assertEquals(Maths.lcm(99999, -22222), 199998);
		assertEquals(Maths.lcm(-99999, -22222), 199998);
		assertEquals(Maths.lcm(IMIN, 0), 0);
		Assert.thrown(() -> Maths.lcm(IMIN, 1));
		assertEquals(Maths.lcm(IMAX, 0), 0);
		assertEquals(Maths.lcm(IMAX, 1), IMAX);
		Assert.thrown(() -> Maths.lcm(IMIN, IMIN));
		assertEquals(Maths.lcm(IMAX, IMAX), IMAX);
		Assert.thrown(() -> Maths.lcm(IMIN, IMAX));
	}

	@Test
	public void testLcmForLongs() {
		assertEquals(Maths.lcm(LMIN, 0), 0L);
		Assert.thrown(() -> Maths.lcm(LMIN, 1L));
		assertEquals(Maths.lcm(LMAX, 0), 0L);
		assertEquals(Maths.lcm(LMAX, 1), LMAX);
		Assert.thrown(() -> Maths.lcm(LMIN, LMIN));
		assertEquals(Maths.lcm(LMAX, LMAX), LMAX);
		Assert.thrown(() -> Maths.lcm(LMIN, LMAX));
	}

	@Test
	public void testMeanInt() {
		assertEquals(Maths.mean(-1), -1.0);
		assertEquals(Maths.mean(1, -1), 0.0);
		assertEquals(Maths.mean(1, -1, 3), 1.0);
		assertEquals(Maths.mean(IMAX, IMAX, IMAX), (double) IMAX);
		assertEquals(Maths.mean(IMIN, IMIN, IMIN), (double) IMIN);
		assertEquals(Maths.mean(IMAX, IMIN), -0.5);
		Assert.thrown(() -> Maths.mean(new int[0]));
		Assert.thrown(() -> Maths.mean(new int[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.mean((int[]) null, 0, 1));
	}

	@Test
	public void testMeanLong() {
		assertEquals(Maths.mean(-1L), -1.0);
		assertEquals(Maths.mean(1L, -1L), 0.0);
		assertEquals(Maths.mean(1L, -1L, 3L), 1.0);
		assertEquals(Maths.mean(LMAX, LMAX, LMAX), (double) LMAX);
		assertEquals(Maths.mean(LMIN, LMIN, LMIN), (double) LMIN);
		assertEquals(Maths.mean(LMAX, LMIN), -0.5);
		Assert.thrown(() -> Maths.mean(new long[0]));
		Assert.thrown(() -> Maths.mean(new long[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.mean((long[]) null, 0, 1));
	}

	@Test
	public void testMeanFloat() {
		assertEquals(Maths.mean(-1.0f), -1.0f);
		assertEquals(Maths.mean(1.0f, -1.0f), 0.0f);
		assertEquals(Maths.mean(1.0f, -1.0f, 3.0f), 1.0f);
		assertEquals(Maths.mean(FMAX, -FMAX), 0.0f);
		assertEquals(Maths.mean(FPINF, FPINF), FPINF);
		assertEquals(Maths.mean(FNINF, FPINF), Float.NaN);
		Assert.thrown(() -> Maths.mean(new float[0]));
		Assert.thrown(() -> Maths.mean(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		Assert.thrown(() -> Maths.mean((float[]) null, 0, 1));
	}

	@Test
	public void testMeanDouble() {
		assertEquals(Maths.mean(-1.0), -1.0);
		assertEquals(Maths.mean(1.0, -1.0), 0.0);
		assertEquals(Maths.mean(1.0, -1.0, 3.0), 1.0);
		assertEquals(Maths.mean(DMAX, -DMAX), 0.0);
		assertEquals(Maths.mean(DPINF, DPINF), DPINF);
		assertEquals(Maths.mean(DNINF, DPINF), Double.NaN);
		Assert.thrown(() -> Maths.mean(new double[0]));
		Assert.thrown(() -> Maths.mean(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		Assert.thrown(() -> Maths.mean((double[]) null, 0, 1));
	}

	@Test
	public void testMedianInt() {
		assertEquals(Maths.median(-1), -1.0);
		assertEquals(Maths.median(1, -1), 0.0);
		assertEquals(Maths.median(1, -1, 3), 1.0);
		assertEquals(Maths.median(IMAX, IMIN), -0.5);
		assertEquals(Maths.median(IMAX, IMAX), (double) IMAX);
		assertEquals(Maths.median(IMIN, IMIN), (double) IMIN);
		assertEquals(Maths.median(IMAX - 1, IMAX), IMAX - 0.5);
		assertEquals(Maths.median(IMIN + 1, IMIN), IMIN + 0.5);
		Assert.thrown(() -> Maths.median(new int[0]));
		Assert.thrown(() -> Maths.median(new int[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.median((int[]) null, 0, 1));
	}

	@Test
	public void testMedianLong() {
		assertEquals(Maths.median(-1L), -1.0);
		assertEquals(Maths.median(1L, -1L), 0.0);
		assertEquals(Maths.median(1L, -1L, 3L), 1.0);
		assertEquals(Maths.median(LMAX, LMIN), -0.5);
		assertEquals(Maths.median(LMAX, LMAX), (double) LMAX);
		assertEquals(Maths.median(LMIN, LMIN), (double) LMIN);
		assertEquals(Maths.median(LMAX - 1, LMAX), LMAX - 0.5);
		assertEquals(Maths.median(LMIN + 1, LMIN), LMIN + 0.5);
		Assert.thrown(() -> Maths.median(new long[0]));
		Assert.thrown(() -> Maths.median(new long[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.median((long[]) null, 0, 1));
	}

	@Test
	public void testMedianFloat() {
		assertEquals(Maths.median(-1.0f), -1.0f);
		assertEquals(Maths.median(1.0f, -1.0f), 0.0f);
		assertEquals(Maths.median(1.0f, -1.0f, 3.0f), 1.0f);
		assertEquals(Maths.median(FMAX, -FMAX), 0.0f);
		assertEquals(Maths.median(FPINF, FPINF), FPINF);
		assertEquals(Maths.median(FNINF, FPINF), Float.NaN);
		Assert.thrown(() -> Maths.median(new float[0]));
		Assert.thrown(() -> Maths.median(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		Assert.thrown(() -> Maths.median((float[]) null, 0, 1));
	}

	@Test
	public void testMedianDouble() {
		assertEquals(Maths.median(-1.0), -1.0);
		assertEquals(Maths.median(1.0, -1.0), 0.0);
		assertEquals(Maths.median(1.0, -1.0, 3.0), 1.0);
		assertEquals(Maths.median(DMAX, -DMAX), 0.0);
		assertEquals(Maths.median(DPINF, DPINF), DPINF);
		assertEquals(Maths.median(DNINF, DPINF), Double.NaN);
		Assert.thrown(() -> Maths.median(new double[0]));
		Assert.thrown(() -> Maths.median(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		Assert.thrown(() -> Maths.median((double[]) null, 0, 1));
	}

	@Test
	public void testWithinInt() {
		assertEquals(Maths.within(0, 0, 0), true);
		assertEquals(Maths.within(0, -1, 1), true);
		assertEquals(Maths.within(0, 0, 1), true);
		assertEquals(Maths.within(0, -1, 0), true);
		assertEquals(Maths.within(1, -1, 0), false);
		assertEquals(Maths.within(-1, 0, 1), false);
		assertEquals(Maths.within(IMAX, IMIN, IMAX), true);
		assertEquals(Maths.within(IMAX, IMAX, IMAX), true);
		assertEquals(Maths.within(IMAX, IMIN, IMAX - 1), false);
		assertEquals(Maths.within(IMIN, IMIN, IMAX), true);
		assertEquals(Maths.within(IMIN, IMIN, IMIN), true);
		assertEquals(Maths.within(IMIN, IMIN + 1, IMAX), false);
	}

	@Test
	public void testWithinLong() {
		assertEquals(Maths.within(0L, 0, 0), true);
		assertEquals(Maths.within(0L, -1, 1), true);
		assertEquals(Maths.within(0L, 0, 1), true);
		assertEquals(Maths.within(0L, -1, 0), true);
		assertEquals(Maths.within(1L, -1, 0), false);
		assertEquals(Maths.within(-1L, 0, 1), false);
		assertEquals(Maths.within(LMAX, LMIN, LMAX), true);
		assertEquals(Maths.within(LMAX, LMAX, LMAX), true);
		assertEquals(Maths.within(LMAX, LMIN, LMAX - 1), false);
		assertEquals(Maths.within(LMIN, LMIN, LMAX), true);
		assertEquals(Maths.within(LMIN, LMIN, LMIN), true);
		assertEquals(Maths.within(LMIN, LMIN + 1, LMAX), false);
	}

	@Test
	public void testLimitInt() {
		assertEquals(Maths.limit(2, -1, 1), 1);
		assertEquals(Maths.limit(-2, -1, 1), -1);
		assertEquals(Maths.limit(0, -1, 1), 0);
		assertEquals(Maths.limit(IMAX, 0, IMAX), IMAX);
		assertEquals(Maths.limit(IMIN, 0, IMAX), 0);
		Assert.thrown(() -> Maths.limit(0, 1, 0));
	}

	@Test
	public void testLimitLong() {
		assertEquals(Maths.limit(2L, -1L, 1L), 1L);
		assertEquals(Maths.limit(-2L, -1L, 1L), -1L);
		assertEquals(Maths.limit(0L, -1L, 1L), 0L);
		assertEquals(Maths.limit(LMAX, 0L, LMAX), LMAX);
		assertEquals(Maths.limit(LMIN, 0L, LMAX), 0L);
		Assert.thrown(() -> Maths.limit(0L, 1L, 0L));
	}

	@Test
	public void testLimitFloat() {
		assertEquals(Maths.limit(1.0f, -0.5f, -0.5f), -0.5f);
		assertEquals(Maths.limit(1.0f, -0.5f, 0.5f), 0.5f);
		assertEquals(Maths.limit(-1.0f, -0.5f, 0.5f), -0.5f);
		assertEquals(Maths.limit(0.0f, -0.5f, 0.5f), 0.0f);
		assertEquals(Maths.limit(FMAX, 0.0f, FMAX), FMAX);
		assertEquals(Maths.limit(-FMAX, 0.0f, FMAX), 0.0f);
		assertEquals(Maths.limit(FPINF, 0.0f, FMAX), FMAX);
		assertEquals(Maths.limit(FNINF, 0.0f, FMAX), 0.0f);
		Assert.thrown(() -> Maths.limit(0.0f, 1.0f, 0.0f));
	}

	@Test
	public void testLimitDouble() {
		assertEquals(Maths.limit(1.0, -0.5, -0.5), -0.5);
		assertEquals(Maths.limit(1.0, -0.5, 0.5), 0.5);
		assertEquals(Maths.limit(-1.0, -0.5, 0.5), -0.5);
		assertEquals(Maths.limit(0.0, -0.5, 0.5), 0.0);
		assertEquals(Maths.limit(DMAX, 0.0, DMAX), DMAX);
		assertEquals(Maths.limit(-DMAX, 0.0, DMAX), 0.0);
		assertEquals(Maths.limit(DPINF, 0.0, DMAX), DMAX);
		assertEquals(Maths.limit(DNINF, 0.0, DMAX), 0.0);
		Assert.thrown(() -> Maths.limit(0.0, 1.0, 0.0));
	}

	@Test
	public void testPeriodicLimitInt() {
		assertEquals(Maths.periodicLimit(100, 10, inc), 10);
		assertEquals(Maths.periodicLimit(100, 10, exc), 0);
		assertEquals(Maths.periodicLimit(-100, 10, inc), 0);
		assertEquals(Maths.periodicLimit(-100, 10, exc), 0);
		assertEquals(Maths.periodicLimit(7, 10, inc), 7);
		assertEquals(Maths.periodicLimit(-7, 10, inc), 3);
		assertEquals(Maths.periodicLimit(0, IMAX, inc), 0);
		assertEquals(Maths.periodicLimit(0, IMAX, exc), 0);
		assertEquals(Maths.periodicLimit(IMAX, IMAX, inc), IMAX);
		assertEquals(Maths.periodicLimit(IMAX, IMAX, exc), 0);
		assertEquals(Maths.periodicLimit(IMIN, IMAX, inc), IMAX - 1);
		assertEquals(Maths.periodicLimit(IMIN, IMAX, exc), IMAX - 1);
		Assert.thrown(() -> Maths.periodicLimit(100, 10, null));
		Assert.thrown(() -> Maths.periodicLimit(100, 0, inc));
		Assert.thrown(() -> Maths.periodicLimit(100, -10, inc));
	}

	@Test
	public void testPeriodicLimitLong() {
		assertEquals(Maths.periodicLimit(100L, 10L, inc), 10L);
		assertEquals(Maths.periodicLimit(100L, 10L, exc), 0L);
		assertEquals(Maths.periodicLimit(-100L, 10L, inc), 0L);
		assertEquals(Maths.periodicLimit(-100L, 10L, exc), 0L);
		assertEquals(Maths.periodicLimit(7L, 10L, inc), 7L);
		assertEquals(Maths.periodicLimit(-7L, 10L, inc), 3L);
		assertEquals(Maths.periodicLimit(0L, LMAX, inc), 0L);
		assertEquals(Maths.periodicLimit(0L, LMAX, exc), 0L);
		assertEquals(Maths.periodicLimit(LMAX, LMAX, inc), LMAX);
		assertEquals(Maths.periodicLimit(LMAX, LMAX, exc), 0L);
		assertEquals(Maths.periodicLimit(LMIN, LMAX, inc), LMAX - 1);
		assertEquals(Maths.periodicLimit(LMIN, LMAX, exc), LMAX - 1);
		Assert.thrown(() -> Maths.periodicLimit(100L, 10L, null));
		Assert.thrown(() -> Maths.periodicLimit(100L, 0L, inc));
		Assert.thrown(() -> Maths.periodicLimit(100L, -10L, inc));
	}

	@Test
	public void testPeriodicLimitFloat() {
		assertEquals(Maths.periodicLimit(100.0f, 10.0f, inc), 10.0f);
		assertEquals(Maths.periodicLimit(100.0f, 10.0f, exc), 0.0f);
		assertEquals(Maths.periodicLimit(-100.0f, 10.0f, inc), 0.0f);
		assertEquals(Maths.periodicLimit(-100.0f, 10.0f, exc), 0.0f);
		assertEquals(Maths.periodicLimit(7.0f, 10.0f, inc), 7.0f);
		assertEquals(Maths.periodicLimit(-7.0f, 10.0f, inc), 3.0f);
		assertEquals(Maths.periodicLimit(0.0f, FPINF, inc), 0.0f);
		assertEquals(Maths.periodicLimit(0.0f, FPINF, exc), 0.0f);
		assertEquals(Maths.periodicLimit(FPINF, FPINF, inc), FPINF);
		assertEquals(Maths.periodicLimit(FPINF, FPINF, exc), Float.NaN);
		assertEquals(Maths.periodicLimit(FNINF, FPINF, inc), Float.NaN);
		assertEquals(Maths.periodicLimit(FNINF, FPINF, exc), Float.NaN);
		Assert.thrown(() -> Maths.periodicLimit(100.0f, 10.0f, null));
		Assert.thrown(() -> Maths.periodicLimit(100.0f, 0.0f, inc));
		Assert.thrown(() -> Maths.periodicLimit(100.0f, -10.0f, inc));
	}

	@Test
	public void testPeriodicLimitDouble() {
		assertEquals(Maths.periodicLimit(100.0, 10.0, inc), 10.0);
		assertEquals(Maths.periodicLimit(100.0, 10.0, exc), 0.0);
		assertEquals(Maths.periodicLimit(-100.0, 10.0, inc), 0.0);
		assertEquals(Maths.periodicLimit(-100.0, 10.0, exc), 0.0);
		assertEquals(Maths.periodicLimit(7.0, 10.0, inc), 7.0);
		assertEquals(Maths.periodicLimit(-7.0, 10.0, inc), 3.0);
		assertEquals(Maths.periodicLimit(0.0, DPINF, inc), 0.0);
		assertEquals(Maths.periodicLimit(0.0, DPINF, exc), 0.0);
		assertEquals(Maths.periodicLimit(DPINF, DPINF, inc), DPINF);
		assertEquals(Maths.periodicLimit(DPINF, DPINF, exc), Double.NaN);
		assertEquals(Maths.periodicLimit(DNINF, DPINF, inc), Double.NaN);
		assertEquals(Maths.periodicLimit(DNINF, DPINF, exc), Double.NaN);
		Assert.thrown(() -> Maths.periodicLimit(100.0, 10.0, null));
		Assert.thrown(() -> Maths.periodicLimit(100.0, 0.0, inc));
		Assert.thrown(() -> Maths.periodicLimit(100.0, -10.0, inc));
	}

	@Test
	public void testMinByte() {
		byte[] array = { BMIN, -BMAX, -1, 0, 1, BMAX };
		assertEquals(Maths.min(array), BMIN);
		assertEquals(Maths.min(array, 1, 5), (byte) -BMAX);
		assertEquals(Maths.min(array, 2, 3), (byte) -1);
		Assert.thrown(() -> Maths.min(new byte[0]));
		Assert.thrown(() -> Maths.min(new byte[2], 1, 0));
		Assert.thrown(() -> Maths.min((byte[]) null));
		Assert.thrown(() -> Maths.min(new byte[3], 1, 3));
	}

	@Test
	public void testMinShort() {
		short[] array = { SMIN, -SMAX, -1, 0, 1, SMAX };
		assertEquals(Maths.min(array), SMIN);
		assertEquals(Maths.min(array, 1, 5), (short) -SMAX);
		assertEquals(Maths.min(array, 2, 3), (short) -1);
		Assert.thrown(() -> Maths.min(new short[0]));
		Assert.thrown(() -> Maths.min(new short[2], 1, 0));
		Assert.thrown(() -> Maths.min((short[]) null));
		Assert.thrown(() -> Maths.min(new short[3], 1, 3));
	}

	@Test
	public void testMinInt() {
		int[] array = { IMIN, -IMAX, -1, 0, 1, IMAX };
		assertEquals(Maths.min(array), IMIN);
		assertEquals(Maths.min(array, 1, 5), -IMAX);
		assertEquals(Maths.min(array, 2, 3), -1);
		Assert.thrown(() -> Maths.min(new int[0]));
		Assert.thrown(() -> Maths.min(new int[2], 1, 0));
		Assert.thrown(() -> Maths.min((int[]) null));
		Assert.thrown(() -> Maths.min(new int[3], 1, 3));
	}

	@Test
	public void testMinLong() {
		long[] array = { LMIN, -LMAX, -1, 0, 1, LMAX };
		assertEquals(Maths.min(array), LMIN);
		assertEquals(Maths.min(array, 1, 5), -LMAX);
		assertEquals(Maths.min(array, 2, 3), -1L);
		Assert.thrown(() -> Maths.min(new long[0]));
		Assert.thrown(() -> Maths.min(new long[2], 1, 0));
		Assert.thrown(() -> Maths.min((long[]) null));
		Assert.thrown(() -> Maths.min(new long[3], 1, 3));
	}

	@Test
	public void testMinFloat() {
		float[] d = { FNINF, -FMAX, FMAX, -1, 0, 1, FMIN, FPINF, Float.NaN };
		assertEquals(Maths.min(d), Float.NaN);
		assertEquals(Maths.min(d, 0, 8), FNINF);
		assertEquals(Maths.min(d, 1, 7), -FMAX);
		assertEquals(Maths.min(d, 3, 3), -1.0f);
		Assert.thrown(() -> Maths.min(new float[0]));
		Assert.thrown(() -> Maths.min(new float[2], 1, 0));
		Assert.thrown(() -> Maths.min((float[]) null));
		Assert.thrown(() -> Maths.min(new float[3], 1, 3));
	}

	@Test
	public void testMinDouble() {
		double[] d = { DNINF, -DMAX, DMAX, -1, 0, 1, DMIN, DPINF, Double.NaN };
		assertEquals(Maths.min(d), Double.NaN);
		assertEquals(Maths.min(d, 0, 8), DNINF);
		assertEquals(Maths.min(d, 1, 7), -DMAX);
		assertEquals(Maths.min(d, 3, 3), -1.0);
		Assert.thrown(() -> Maths.min(new double[0]));
		Assert.thrown(() -> Maths.min(new double[2], 1, 0));
		Assert.thrown(() -> Maths.min((double[]) null));
		Assert.thrown(() -> Maths.min(new double[3], 1, 3));
	}

	@Test
	public void testMaxByte() {
		byte[] array = { BMIN, -BMAX, -1, 0, 1, BMAX };
		assertEquals(Maths.max(array), BMAX);
		assertEquals(Maths.max(array, 0, 2), (byte) -BMAX);
		assertEquals(Maths.max(array, 0, 5), (byte) 1);
		Assert.thrown(() -> Maths.max(new byte[0]));
		Assert.thrown(() -> Maths.max(new byte[2], 1, 0));
		Assert.thrown(() -> Maths.max((byte[]) null));
		Assert.thrown(() -> Maths.max(new byte[3], 1, 3));
	}

	@Test
	public void testMaxShort() {
		short[] array = { SMIN, -SMAX, -1, 0, 1, SMAX };
		assertEquals(Maths.max(array), SMAX);
		assertEquals(Maths.max(array, 0, 2), (short) -SMAX);
		assertEquals(Maths.max(array, 0, 5), (short) 1);
		Assert.thrown(() -> Maths.max(new short[0]));
		Assert.thrown(() -> Maths.max(new short[2], 1, 0));
		Assert.thrown(() -> Maths.max((short[]) null));
		Assert.thrown(() -> Maths.max(new short[3], 1, 3));
	}

	@Test
	public void testMaxInt() {
		int[] array = { IMIN, -IMAX, -1, 0, 1, IMAX };
		assertEquals(Maths.max(array), IMAX);
		assertEquals(Maths.max(array, 0, 2), -IMAX);
		assertEquals(Maths.max(array, 0, 5), 1);
		Assert.thrown(() -> Maths.max(new int[0]));
		Assert.thrown(() -> Maths.max(new int[2], 1, 0));
		Assert.thrown(() -> Maths.max((int[]) null));
		Assert.thrown(() -> Maths.max(new int[3], 1, 3));
	}

	@Test
	public void testMaxLong() {
		long[] array = { LMIN, -LMAX, -1, 0, 1, LMAX };
		assertEquals(Maths.max(array), LMAX);
		assertEquals(Maths.max(array, 0, 2), -LMAX);
		assertEquals(Maths.max(array, 0, 5), 1L);
		Assert.thrown(() -> Maths.max(new long[0]));
		Assert.thrown(() -> Maths.max(new long[2], 1, 0));
		Assert.thrown(() -> Maths.max((long[]) null));
		Assert.thrown(() -> Maths.max(new long[3], 1, 3));
	}

	@Test
	public void testMaxFloat() {
		float[] array = { FNINF, -FMAX, FMAX, -1, 0, 1, FMIN, FPINF, Float.NaN };
		assertEquals(Maths.max(array), Float.NaN);
		assertEquals(Maths.max(array, 0, 8), FPINF);
		assertEquals(Maths.max(array, 0, 7), FMAX);
		assertEquals(Maths.max(array, 3, 3), 1.0f);
		Assert.thrown(() -> Maths.max(new float[0]));
		Assert.thrown(() -> Maths.max(new float[2], 1, 0));
		Assert.thrown(() -> Maths.max((float[]) null));
		Assert.thrown(() -> Maths.max(new float[3], 1, 3));
	}

	@Test
	public void testMaxDouble() {
		double[] array = { DNINF, -DMAX, DMAX, -1, 0, 1, DMIN, DPINF, Double.NaN };
		assertEquals(Maths.max(array), Double.NaN);
		assertEquals(Maths.max(array, 0, 8), DPINF);
		assertEquals(Maths.max(array, 0, 7), DMAX);
		assertEquals(Maths.max(array, 3, 3), 1.0);
		Assert.thrown(() -> Maths.max(new double[0]));
		Assert.thrown(() -> Maths.max(new double[2], 1, 0));
		Assert.thrown(() -> Maths.max((double[]) null));
		Assert.thrown(() -> Maths.max(new double[3], 1, 3));
	}

	/**
	 * Tests positive and negative combinations of numerator and denominator.
	 */
	private void assertRoundDiv(int x, int y, int pp, int np, int pn, int nn) {
		assertEquals(Maths.roundDiv(x, y), pp);
		assertEquals(Maths.roundDiv(-x, y), np);
		assertEquals(Maths.roundDiv(x, -y), pn);
		assertEquals(Maths.roundDiv(-x, -y), nn);
	}

	/**
	 * Tests positive and negative combinations of numerator and denominator.
	 */
	private void assertRoundDiv(long x, long y, long pp, long np, long pn, long nn) {
		assertEquals(Maths.roundDiv(x, y), pp);
		assertEquals(Maths.roundDiv(-x, y), np);
		assertEquals(Maths.roundDiv(x, -y), pn);
		assertEquals(Maths.roundDiv(-x, -y), nn);
	}

	private static void assertOverflow(int l, int r, boolean overflow) {
		assertEquals(Maths.overflow(l + r, l, r), overflow);
	}

	private static void assertOverflow(long l, long r, boolean overflow) {
		assertEquals(Maths.overflow(l + r, l, r), overflow);
	}

}