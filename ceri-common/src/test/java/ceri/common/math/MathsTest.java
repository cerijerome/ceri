package ceri.common.math;

import static ceri.common.math.Bound.Type.exc;
import static ceri.common.math.Bound.Type.inc;
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
		Assert.privateConstructor(Maths.class);
	}

	@Test
	public void testDecimalDigits() {
		Assert.equal(Maths.decimalDigits(Long.MIN_VALUE), 19);
		Assert.equal(Maths.decimalDigits(Integer.MIN_VALUE), 10);
		Assert.equal(Maths.decimalDigits(Byte.MIN_VALUE), 3);
		Assert.equal(Maths.decimalDigits(-1), 1);
		Assert.equal(Maths.decimalDigits(0), 0);
		Assert.equal(Maths.decimalDigits(1), 1);
		Assert.equal(Maths.decimalDigits(Byte.MAX_VALUE), 3);
		Assert.equal(Maths.decimalDigits(Integer.MAX_VALUE), 10);
		Assert.equal(Maths.decimalDigits(Long.MAX_VALUE), 19);
	}

	@Test
	public void testIntSin() {
		for (int i = -720; i <= 720; i += 1) {
			var x0 = (int) Math.round(Math.sin(Math.toRadians(i)) * 1000);
			var x = Maths.intSin(i, 1000);
			Assert.range(x, x0 - 2, x0 + 2, "%d degrees", i); // within +/-.2%
		}
	}

	@Test
	public void testIntCos() {
		for (int i = -720; i <= 720; i += 1) {
			var x0 = (int) Math.round(Math.cos(Math.toRadians(i)) * 1000);
			var x = Maths.intCos(i, 1000);
			Assert.range(x, x0 - 2, x0 + 2); // within +/-.2%
		}
	}

	@Test
	public void testIntSinFromRatio() {
		for (int i = -4000; i <= 4000; i += 5) {
			var x0 = Maths.intRoundExact(Math.sin(Math.toRadians(i * 180 / 1000.0)) * 1000);
			var x = Maths.intSinFromRatio(i, 1000, 1000);
			Assert.range(x, x0 - 2, x0 + 2, "%d/%d", i, 1000); // within +/-.2%
		}
	}

	@Test
	public void testIntCosFromRatio() {
		for (int i = -4000; i <= 4000; i += 5) {
			var x0 = Maths.intRoundExact(Math.cos(Math.toRadians(i * 180 / 1000.0)) * 1000);
			var x = Maths.intCosFromRatio(i, 1000, 1000);
			Assert.range(x, x0 - 2, x0 + 2, "%d/%d", i, 1000); // within +/-.2%
		}
	}

	@Test
	public void testDoublePolynomial() {
		Assert.equal(Maths.polynomial(2.0), 0.0);
		Assert.equal(Maths.polynomial(0.5, 3.0, 0.5, 0.1, 0.2), 3.3);
	}

	@Test
	public void testLongPolynomial() {
		Assert.equal(Maths.polynomial(5), 0L);
		Assert.equal(Maths.polynomial(2, 3, 0, 5, 1), 31L);
	}

	@Test
	public void testUlog2() {
		Assert.equal(Maths.ulog2(0), -1);
		Assert.equal(Maths.ulog2(1), 0);
		Assert.equal(Maths.ulog2(2), 1);
		Assert.equal(Maths.ulog2(3), 1);
		Assert.equal(Maths.ulog2(4), 2);
		Assert.equal(Maths.ulog2(7), 2);
		Assert.equal(Maths.ulog2(8), 3);
		Assert.equal(Maths.ulog2(0x00000020), 5);
		Assert.equal(Maths.ulog2(0x00000300), 9);
		Assert.equal(Maths.ulog2(0x00004000), 14);
		Assert.equal(Maths.ulog2(0x00050000), 18);
		Assert.equal(Maths.ulog2(0x00600000), 22);
		Assert.equal(Maths.ulog2(0x07000000), 26);
		Assert.equal(Maths.ulog2(IMAX), 30);
		Assert.equal(Maths.ulog2(0x80000000), 31);
		Assert.equal(Maths.ulog2(0xffffffff), 31);
	}

	@Test
	public void testToInt() {
		Assert.equal(Maths.toInt(true), 1);
		Assert.equal(Maths.toInt(false), 0);
	}

	@Test
	public void testAbsLimitInt() {
		Assert.equal(Maths.absLimit(-1), 1);
		Assert.equal(Maths.absLimit(IMIN), IMAX);
		Assert.equal(Maths.absLimit(IMAX), IMAX);
	}

	@Test
	public void testAbsLimitLong() {
		Assert.equal(Maths.absLimit(-1L), 1L);
		Assert.equal(Maths.absLimit(LMIN), LMAX);
		Assert.equal(Maths.absLimit(LMAX), LMAX);
	}

	@Test
	public void testAddLimitInt() {
		Assert.equal(Maths.addLimit(IMAX, IMIN), -1);
		Assert.equal(Maths.addLimit(IMAX, IMAX), IMAX);
		Assert.equal(Maths.addLimit(IMIN, IMIN), IMIN);
	}

	@Test
	public void testAddLimitLong() {
		Assert.equal(Maths.addLimit(LMAX, LMIN), -1L);
		Assert.equal(Maths.addLimit(LMAX, LMAX), LMAX);
		Assert.equal(Maths.addLimit(LMIN, LMIN), LMIN);
	}

	@Test
	public void testSubtractLimitInt() {
		Assert.equal(Maths.subtractLimit(IMIN, IMIN), 0);
		Assert.equal(Maths.subtractLimit(IMAX, IMIN), IMAX);
		Assert.equal(Maths.subtractLimit(IMIN, IMAX), IMIN);
	}

	@Test
	public void testSubtractLimitLong() {
		Assert.equal(Maths.subtractLimit(LMIN, LMIN), 0L);
		Assert.equal(Maths.subtractLimit(LMAX, LMIN), LMAX);
		Assert.equal(Maths.subtractLimit(LMIN, LMAX), LMIN);
	}

	@Test
	public void testMultiplyLimitInt() {
		Assert.equal(Maths.multiplyLimit(IMIN, IMIN), IMAX);
		Assert.equal(Maths.multiplyLimit(IMIN, IMAX), IMIN);
		Assert.equal(Maths.multiplyLimit(IMAX, IMAX), IMAX);
		Assert.equal(Maths.multiplyLimit(IMIN, 0), 0);
		Assert.equal(Maths.multiplyLimit(IMIN, -1), IMAX);
	}

	@Test
	public void testMultiplyLimitLong() {
		Assert.equal(Maths.multiplyLimit(LMIN, LMIN), LMAX);
		Assert.equal(Maths.multiplyLimit(LMIN, LMAX), LMIN);
		Assert.equal(Maths.multiplyLimit(LMAX, LMAX), LMAX);
		Assert.equal(Maths.multiplyLimit(LMIN, 0), 0L);
		Assert.equal(Maths.multiplyLimit(LMIN, -1), LMAX);
		Assert.equal(Maths.multiplyLimit(0x100000000L, 1), 0x100000000L);
	}

	@Test
	public void testDecrementLimitInt() {
		Assert.equal(Maths.decrementLimit(IMAX), IMAX - 1);
		Assert.equal(Maths.decrementLimit(IMIN), IMIN);
	}

	@Test
	public void testDecrementLimitLong() {
		Assert.equal(Maths.decrementLimit(LMAX), LMAX - 1);
		Assert.equal(Maths.decrementLimit(LMIN), LMIN);
	}

	@Test
	public void testIncrementLimitInt() {
		Assert.equal(Maths.incrementLimit(IMIN), IMIN + 1);
		Assert.equal(Maths.incrementLimit(IMAX), IMAX);
	}

	@Test
	public void testIncrementLimitLong() {
		Assert.equal(Maths.incrementLimit(LMIN), LMIN + 1);
		Assert.equal(Maths.incrementLimit(LMAX), LMAX);
	}

	@Test
	public void testNegateInt() {
		Assert.equal(Maths.negateLimit(IMAX), IMIN + 1);
		Assert.equal(Maths.negateLimit(IMIN), IMAX);
	}

	@Test
	public void testNegateLong() {
		Assert.equal(Maths.negateLimit(LMAX), LMIN + 1);
		Assert.equal(Maths.negateLimit(LMIN), LMAX);
	}

	@Test
	public void testToIntLimit() {
		Assert.equal(Maths.toIntLimit(IMIN), IMIN);
		Assert.equal(Maths.toIntLimit(LMAX), IMAX);
		Assert.equal(Maths.toIntLimit(LMIN), IMIN);
	}

	@Test
	public void testToIntLimitDouble() {
		Assert.equal(Maths.toIntLimit((double) IMAX), IMAX);
		Assert.equal(Maths.toIntLimit(DMAX), IMAX);
		Assert.equal(Maths.toIntLimit(-DMAX), IMIN);
	}

	@Test
	public void testToLongLimitDouble() {
		Assert.equal(Maths.toLongLimit(LMAX), LMAX);
		Assert.equal(Maths.toLongLimit(DMAX), LMAX);
		Assert.equal(Maths.toLongLimit(-DMAX), LMIN);
	}

	@Test
	public void testSafeToInt() {
		double d0 = IMIN;
		double d1 = IMAX;
		Assert.equal(Maths.safeToInt(d0), IMIN);
		Assert.equal(Maths.safeToInt(d1), IMAX);
		Assert.thrown(() -> Maths.safeToInt(Double.NaN));
		Assert.thrown(() -> Maths.safeToInt(DPINF));
		Assert.thrown(() -> Maths.safeToInt(DNINF));
		Assert.thrown(() -> Maths.safeToInt(DMAX));
	}

	@Test
	public void testSafeToLong() {
		double d0 = LMIN;
		double d1 = LMAX;
		Assert.equal(Maths.safeToLong(d0), LMIN);
		Assert.equal(Maths.safeToLong(d1), LMAX);
		Assert.thrown(() -> Maths.safeToLong(Double.NaN));
		Assert.thrown(() -> Maths.safeToLong(DPINF));
		Assert.thrown(() -> Maths.safeToLong(DNINF));
		Assert.thrown(() -> Maths.safeToLong(DMAX));
	}

	@Test
	public void testByteExact() {
		long l0 = BMIN;
		long l1 = BMAX;
		Assert.equal(Maths.byteExact(l0), BMIN);
		Assert.equal(Maths.byteExact(l1), BMAX);
		Assert.thrown(() -> Maths.byteExact(BMIN - 1));
		Assert.thrown(() -> Maths.byteExact(BMAX + 1));
	}

	@Test
	public void testShortExact() {
		long l0 = SMIN;
		long l1 = SMAX;
		Assert.equal(Maths.shortExact(l0), SMIN);
		Assert.equal(Maths.shortExact(l1), SMAX);
		Assert.thrown(() -> Maths.shortExact(SMIN - 1));
		Assert.thrown(() -> Maths.shortExact(SMAX + 1));
	}

	@Test
	public void testUbyteExact() {
		Assert.equal(Maths.ubyteExact(0xff), (byte) 0xff);
		Assert.thrown(() -> Maths.ubyteExact(-1));
		Assert.thrown(() -> Maths.ubyteExact(0x100));
	}

	@Test
	public void testUshortExact() {
		Assert.equal(Maths.ushortExact(0xffff), (short) 0xffff);
		Assert.thrown(() -> Maths.ushortExact(-1));
		Assert.thrown(() -> Maths.ushortExact(0x10000));
	}

	@Test
	public void testUintExact() {
		Assert.equal(Maths.uintExact(0xffffffffL), 0xffffffff);
		Assert.thrown(() -> Maths.uintExact(-1));
		Assert.thrown(() -> Maths.uintExact(0x100000000L));
	}

	@Test
	public void testUbyte() {
		Assert.equal(Maths.ubyte((byte) 0xff), (short) 0xff);
	}

	@Test
	public void testUshort() {
		Assert.equal(Maths.ushort((short) 0xffff), 0xffff);
	}

	@Test
	public void testUint() {
		Assert.equal(Maths.uint(0xffffffff), 0xffffffffL);
	}

	@Test
	public void testIntRoundExact() {
		double d0 = IMIN;
		double d1 = IMAX;
		Assert.equal(Maths.intRoundExact(d0), IMIN);
		Assert.equal(Maths.intRoundExact(d1), IMAX);
		Assert.thrown(() -> Maths.intRoundExact(d0 - 1));
		Assert.thrown(() -> Maths.intRoundExact(d1 + 1));
	}

	@Test
	public void testSafeRound() {
		Assert.equal(Maths.safeRound(LMAX), LMAX);
		Assert.equal(Maths.safeRound(LMIN), LMIN);
		Assert.equal(Maths.safeRound(LMAX + 1024.0), LMAX);
		Assert.equal(Maths.safeRound(LMIN - 1024.0), LMIN);
		Assert.thrown(ArithmeticException.class, () -> Maths.safeRound(LMAX + 1025.0));
		Assert.thrown(ArithmeticException.class, () -> Maths.safeRound(LMIN - 1025.0));
	}

	@Test
	public void testSafeRoundInt() {
		Assert.equal(Maths.safeRoundInt(IMAX), IMAX);
		Assert.equal(Maths.safeRoundInt(IMIN), IMIN);
		Assert.equal(Maths.safeRoundInt(IMAX + 0.499999), IMAX);
		Assert.equal(Maths.safeRoundInt(IMIN - 0.5), IMIN);
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
		Assert.equal(Maths.roundDiv(IHMAX + 1, IMIN), 0);
		Assert.equal(Maths.roundDiv(IHMAX + 2, IMIN), -1);
		Assert.equal(Maths.roundDiv(-IHMAX, IMIN), 0);
		Assert.equal(Maths.roundDiv(-IHMAX - 1, IMIN), 1);
		Assert.equal(Maths.roundDiv(IHMIN + 1, IMIN), 0);
		Assert.equal(Maths.roundDiv(IHMIN, IMIN), 1);
		Assert.equal(Maths.roundDiv(-IHMIN, IMIN), 0);
		Assert.equal(Maths.roundDiv(-IHMIN + 1, IMIN), -1);
		//
		assertRoundDiv(IMAX, IHMAX + 1, 2, -2, -2, 2);
		Assert.equal(Maths.roundDiv(IMIN, IHMAX + 1), -2);
		Assert.equal(Maths.roundDiv(IMIN, -IHMAX - 1), 2);
		assertRoundDiv(IMAX, IHMIN - 1, -2, 2, 2, -2);
		Assert.equal(Maths.roundDiv(IMIN, IHMIN - 1), 2);
		Assert.equal(Maths.roundDiv(IMIN, -IHMIN + 1), -2);
		//
		assertRoundDiv(IMAX, 1, IMAX, -IMAX, -IMAX, IMAX);
		Assert.equal(Maths.roundDiv(IMIN, 1), IMIN);
		Assert.equal(Maths.roundDiv(IMIN, -1), IMIN); // overflow?
		assertRoundDiv(IMAX, IMAX, 1, -1, -1, 1);
		Assert.equal(Maths.roundDiv(IMAX, IMIN), -1);
		Assert.equal(Maths.roundDiv(IMIN, IMAX), -1);
		Assert.equal(Maths.roundDiv(IMIN, IMIN), 1);
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
		Assert.equal(Maths.roundDiv(LHMAX + 1, LMIN), 0L);
		Assert.equal(Maths.roundDiv(LHMAX + 2, LMIN), -1L);
		Assert.equal(Maths.roundDiv(-LHMAX, LMIN), 0L);
		Assert.equal(Maths.roundDiv(-LHMAX - 1, LMIN), 1L);
		Assert.equal(Maths.roundDiv(LHMIN + 1, LMIN), 0L);
		Assert.equal(Maths.roundDiv(LHMIN, LMIN), 1L);
		Assert.equal(Maths.roundDiv(-LHMIN, LMIN), 0L);
		Assert.equal(Maths.roundDiv(-LHMIN + 1, LMIN), -1L);
		//
		assertRoundDiv(LMAX, LHMAX + 1, 2, -2, -2, 2);
		Assert.equal(Maths.roundDiv(LMIN, LHMAX + 1), -2L);
		Assert.equal(Maths.roundDiv(LMIN, -LHMAX - 1), 2L);
		assertRoundDiv(LMAX, LHMIN - 1, -2, 2, 2, -2);
		Assert.equal(Maths.roundDiv(LMIN, LHMIN - 1), 2L);
		Assert.equal(Maths.roundDiv(LMIN, -LHMIN + 1), -2L);
		//
		assertRoundDiv(LMAX, 1, LMAX, -LMAX, -LMAX, LMAX);
		Assert.equal(Maths.roundDiv(LMIN, 1), LMIN);
		Assert.equal(Maths.roundDiv(LMIN, -1), LMIN); // overflow?
		assertRoundDiv(LMAX, LMAX, 1, -1, -1, 1);
		Assert.equal(Maths.roundDiv(LMAX, LMIN), -1L);
		Assert.equal(Maths.roundDiv(LMIN, LMAX), -1L);
		Assert.equal(Maths.roundDiv(LMIN, LMIN), 1L);
		Assert.thrown(() -> Maths.roundDiv(1L, 0L));
	}

	@Test
	public void testRound() {
		Assert.equal(Maths.round(1, 1000000.15), 1000000.2);
		Assert.equal(Maths.round(1, -1000000.15), -1000000.1);
		Assert.equal(Maths.round(1, 1000000000.15), 1000000000.2);
		Assert.equal(Maths.round(1, -1000000000.15), -1000000000.1);
		Assert.equal(Maths.round(1, 1000000000000.15), 1000000000000.2);
		Assert.equal(Maths.round(1, -1000000000000.15), -1000000000000.1);
		Assert.equal(Maths.round(1, DPINF), DPINF);
		Assert.equal(Maths.round(1, DNINF), DNINF);
		Assert.equal(Maths.round(1, Double.NaN), Double.NaN);
		Assert.thrown(() -> Maths.round(-1, 777.7777));
	}

	@Test
	public void testSimpleRound() {
		Assert.equal(Maths.simpleRound(2, 11111.11111), 11111.11);
		Assert.equal(Maths.simpleRound(10, 11111.11111), 11111.11111);
		Assert.equal(Maths.simpleRound(2, 11111.111111111111111), 11111.11);
		Assert.equal(Maths.simpleRound(3, 777.7777), 777.778);
		Assert.equal(Maths.simpleRound(3, -777.7777), -777.778);
		Assert.yes(Double.isNaN(Maths.simpleRound(0, Double.NaN)));
		Assert.thrown(() -> Maths.simpleRound(-1, 777.7777));
		Assert.thrown(() -> Maths.simpleRound(11, 777.7777));
	}

	@Test
	public void testSimpleRoundForOutOfRangeValues() {
		// Original values returned if out of range
		Assert.equal(Maths.simpleRound(1, 1000000000.15), 1000000000.15);
		Assert.equal(Maths.simpleRound(1, -1000000000.15), -1000000000.15);
		Assert.equal(Maths.simpleRound(1, DPINF), DPINF);
		Assert.equal(Maths.simpleRound(1, DNINF), DNINF);
	}

	@Test
	public void testEqualsDouble() {
		Assert.yes(Maths.equals(Double.MIN_VALUE, Double.MIN_VALUE));
		Assert.yes(Maths.equals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
		Assert.yes(Maths.equals(Double.NaN, Double.NaN));
		Assert.no(Maths.equals(Double.MIN_VALUE, Double.MIN_NORMAL));
		Assert.no(Maths.equals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		Assert.no(Maths.equals(Double.NaN, Double.POSITIVE_INFINITY));
	}

	@Test
	public void testEqualsFloat() {
		Assert.yes(Maths.equals(Float.MIN_VALUE, Float.MIN_VALUE));
		Assert.yes(Maths.equals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		Assert.yes(Maths.equals(Float.NaN, Float.NaN));
		Assert.no(Maths.equals(Float.MIN_VALUE, Float.MIN_NORMAL));
		Assert.no(Maths.equals(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
		Assert.no(Maths.equals(Float.NaN, Float.POSITIVE_INFINITY));
	}

	@Test
	public void testApproxEqual() {
		Assert.yes(Maths.approxEqual(DMIN, DMIN, DMIN));
		Assert.yes(Maths.approxEqual(0.001, 0.0001, 0.1));
		Assert.no(Maths.approxEqual(0.001, 0.0001, 0.0001));
		Assert.yes(Maths.approxEqual(0.0011, 0.0012, 0.0001));
		Assert.no(Maths.approxEqual(0.0011, 0.0012, 0.00009));
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
		Assert.equal(bits, 0xff);
	}

	@Test
	public void testRandomInt() {
		Assert.range(Maths.random(0), 0, 0);
		Assert.range(Maths.random(1), 0, 1);
		Assert.range(Maths.random(IMAX), 0, IMAX);
		Assert.range(Maths.random(IMIN, IMAX), IMIN, IMAX);
		Assert.range(Maths.random(0, IMAX), 0, IMAX);
		Assert.range(Maths.random(IMIN, 0), IMIN, 0);
		Assert.equal(Maths.random(IMAX, IMAX), IMAX);
		Assert.equal(Maths.random(IMIN, IMIN), IMIN);
	}

	@Test
	public void testRandomLongFill() {
		int bits = 0;
		for (int i = 0; i < 1000 && bits != 0xff; i++)
			bits |= 1 << Maths.random(0L, 7L);
		Assert.equal(bits, 0xff);
	}

	@Test
	public void testRandomLong() {
		Assert.range(Maths.random(0L), 0, 0);
		Assert.range(Maths.random(1L), 0, 1);
		Assert.range(Maths.random(LMAX), 0, LMAX);
		Assert.range(Maths.random(LMIN, LMAX), LMIN, LMAX);
		Assert.range(Maths.random(0L, LMAX), 0L, LMAX);
		Assert.range(Maths.random(LMIN, 0L), LMIN, 0L);
		Assert.equal(Maths.random(LMAX, LMAX), LMAX);
		Assert.equal(Maths.random(LMIN, LMIN), LMIN);
	}

	@Test
	public void testRandomDouble() {
		Assert.range(Maths.random(), 0.0, 1.0);
		Assert.range(Maths.random(0.1), 0.0, 0.1);
		Assert.range(Maths.random(Double.MAX_VALUE), 0.0, Double.MAX_VALUE);
		Assert.range(Maths.random(-1.0, 2.0), -1.0, 2.0);
		Assert.range(Maths.random(-1.0, 0.0), -1.0, 0.0);
	}

	@Test
	public void testRandomList() {
		String s = Maths.random("1", "2", "3");
		Assert.yes(Set.of("1", "2", "3").contains(s));
		Assert.equal(Maths.random("1"), "1");
		Assert.isNull(Maths.random(List.of()));
	}

	@Test
	public void testRandomSet() {
		Set<String> set = Set.of("1", "2", "3");
		String s = Maths.random(set);
		Assert.yes(set.contains(s));
		Assert.equal(Maths.random(Set.of("1")), "1");
		Assert.isNull(Maths.random(Set.of()));
	}

	@Test
	public void testToPercent() {
		Assert.equal(Maths.toPercent(0.9), 90.0);
		Assert.equal(Maths.toPercent(90, 90), 100.0);
		Assert.equal(Maths.toPercent(DMAX, DMAX), 100.0);
		Assert.yes(Double.isNaN(Maths.toPercent(LMAX, 0)));
	}

	@Test
	public void testFromPercent() {
		Assert.equal(Maths.fromPercent(50), 0.5);
		Assert.equal(Maths.fromPercent(50, 90), 45.0);
		Assert.equal(Maths.fromPercent(100, DMAX), DMAX);
		Assert.equal(Maths.fromPercent(DMAX, 100), DMAX);
	}

	@Test
	public void testGcdForInts() {
		Assert.equal(Maths.gcd(60, 90, 45), 15);
		Assert.equal(Maths.gcd(45, 60, 90), 15);
		Assert.equal(Maths.gcd(90, 45, 60), 15);
		Assert.equal(Maths.gcd(0, 0), 0);
		Assert.equal(Maths.gcd(1, 0), 1);
		Assert.equal(Maths.gcd(0, 1), 1);
		Assert.equal(Maths.gcd(-1, 2), 1);
		Assert.equal(Maths.gcd(1, 2), 1);
		Assert.equal(Maths.gcd(2, -1), 1);
		Assert.equal(Maths.gcd(2, 1), 1);
		Assert.equal(Maths.gcd(4, 2), 2);
		Assert.equal(Maths.gcd(2, 4), 2);
		Assert.equal(Maths.gcd(99, -44), 11);
		Assert.equal(Maths.gcd(99999, 22222), 11111);
		Assert.equal(Maths.gcd(22222, 99999), 11111);
		Assert.equal(Maths.gcd(-99999, 22222), 11111);
		Assert.equal(Maths.gcd(99999, -22222), 11111);
		Assert.equal(Maths.gcd(-99999, -22222), 11111);
		Assert.thrown(() -> Maths.gcd(IMIN, 0));
		Assert.equal(Maths.gcd(IMIN, 1), 1);
		Assert.equal(Maths.gcd(IMAX, 0), IMAX);
		Assert.equal(Maths.gcd(IMAX, 1), 1);
		Assert.thrown(() -> Maths.gcd(IMIN, IMIN));
		Assert.equal(Maths.gcd(IMAX, IMAX), IMAX);
		Assert.equal(Maths.gcd(IMIN, IMAX), 1);
	}

	@Test
	public void testGcdForLongs() {
		Assert.thrown(() -> Maths.gcd(LMIN, 0));
		Assert.equal(Maths.gcd(LMIN, 1), 1L);
		Assert.equal(Maths.gcd(LMAX, 0), LMAX);
		Assert.equal(Maths.gcd(LMAX, 1), 1L);
		Assert.thrown(() -> Maths.gcd(LMIN, LMIN));
		Assert.equal(Maths.gcd(LMAX, LMAX), LMAX);
		Assert.equal(Maths.gcd(LMIN, LMAX), 1L);
	}

	@Test
	public void testLcmForInts() {
		Assert.equal(Maths.lcm(8, 12, 16, 24, 32), 96);
		Assert.equal(Maths.lcm(16, 24, 32, 8, 12), 96);
		Assert.equal(Maths.lcm(0, 0), 0);
		Assert.equal(Maths.lcm(1, 0), 0);
		Assert.equal(Maths.lcm(0, 1), 0);
		Assert.equal(Maths.lcm(-1, 2), 2);
		Assert.equal(Maths.lcm(1, -2), 2);
		Assert.equal(Maths.lcm(2, -1), 2);
		Assert.equal(Maths.lcm(-2, 1), 2);
		Assert.equal(Maths.lcm(12, 4), 12);
		Assert.equal(Maths.lcm(8, 16), 16);
		Assert.equal(Maths.lcm(99, -44), 396);
		Assert.equal(Maths.lcm(99999, 22222), 199998);
		Assert.equal(Maths.lcm(22222, 99999), 199998);
		Assert.equal(Maths.lcm(-99999, 22222), 199998);
		Assert.equal(Maths.lcm(99999, -22222), 199998);
		Assert.equal(Maths.lcm(-99999, -22222), 199998);
		Assert.equal(Maths.lcm(IMIN, 0), 0);
		Assert.thrown(() -> Maths.lcm(IMIN, 1));
		Assert.equal(Maths.lcm(IMAX, 0), 0);
		Assert.equal(Maths.lcm(IMAX, 1), IMAX);
		Assert.thrown(() -> Maths.lcm(IMIN, IMIN));
		Assert.equal(Maths.lcm(IMAX, IMAX), IMAX);
		Assert.thrown(() -> Maths.lcm(IMIN, IMAX));
	}

	@Test
	public void testLcmForLongs() {
		Assert.equal(Maths.lcm(LMIN, 0), 0L);
		Assert.thrown(() -> Maths.lcm(LMIN, 1L));
		Assert.equal(Maths.lcm(LMAX, 0), 0L);
		Assert.equal(Maths.lcm(LMAX, 1), LMAX);
		Assert.thrown(() -> Maths.lcm(LMIN, LMIN));
		Assert.equal(Maths.lcm(LMAX, LMAX), LMAX);
		Assert.thrown(() -> Maths.lcm(LMIN, LMAX));
	}

	@Test
	public void testMeanInt() {
		Assert.equal(Maths.mean(-1), -1.0);
		Assert.equal(Maths.mean(1, -1), 0.0);
		Assert.equal(Maths.mean(1, -1, 3), 1.0);
		Assert.equal(Maths.mean(IMAX, IMAX, IMAX), (double) IMAX);
		Assert.equal(Maths.mean(IMIN, IMIN, IMIN), (double) IMIN);
		Assert.equal(Maths.mean(IMAX, IMIN), -0.5);
		Assert.thrown(() -> Maths.mean(new int[0]));
		Assert.thrown(() -> Maths.mean(new int[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.mean((int[]) null, 0, 1));
	}

	@Test
	public void testMeanLong() {
		Assert.equal(Maths.mean(-1L), -1.0);
		Assert.equal(Maths.mean(1L, -1L), 0.0);
		Assert.equal(Maths.mean(1L, -1L, 3L), 1.0);
		Assert.equal(Maths.mean(LMAX, LMAX, LMAX), (double) LMAX);
		Assert.equal(Maths.mean(LMIN, LMIN, LMIN), (double) LMIN);
		Assert.equal(Maths.mean(LMAX, LMIN), -0.5);
		Assert.thrown(() -> Maths.mean(new long[0]));
		Assert.thrown(() -> Maths.mean(new long[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.mean((long[]) null, 0, 1));
	}

	@Test
	public void testMeanFloat() {
		Assert.equal(Maths.mean(-1.0f), -1.0f);
		Assert.equal(Maths.mean(1.0f, -1.0f), 0.0f);
		Assert.equal(Maths.mean(1.0f, -1.0f, 3.0f), 1.0f);
		Assert.equal(Maths.mean(FMAX, -FMAX), 0.0f);
		Assert.equal(Maths.mean(FPINF, FPINF), FPINF);
		Assert.equal(Maths.mean(FNINF, FPINF), Float.NaN);
		Assert.thrown(() -> Maths.mean(new float[0]));
		Assert.thrown(() -> Maths.mean(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		Assert.thrown(() -> Maths.mean((float[]) null, 0, 1));
	}

	@Test
	public void testMeanDouble() {
		Assert.equal(Maths.mean(-1.0), -1.0);
		Assert.equal(Maths.mean(1.0, -1.0), 0.0);
		Assert.equal(Maths.mean(1.0, -1.0, 3.0), 1.0);
		Assert.equal(Maths.mean(DMAX, -DMAX), 0.0);
		Assert.equal(Maths.mean(DPINF, DPINF), DPINF);
		Assert.equal(Maths.mean(DNINF, DPINF), Double.NaN);
		Assert.thrown(() -> Maths.mean(new double[0]));
		Assert.thrown(() -> Maths.mean(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		Assert.thrown(() -> Maths.mean((double[]) null, 0, 1));
	}

	@Test
	public void testMedianInt() {
		Assert.equal(Maths.median(-1), -1.0);
		Assert.equal(Maths.median(1, -1), 0.0);
		Assert.equal(Maths.median(1, -1, 3), 1.0);
		Assert.equal(Maths.median(IMAX, IMIN), -0.5);
		Assert.equal(Maths.median(IMAX, IMAX), (double) IMAX);
		Assert.equal(Maths.median(IMIN, IMIN), (double) IMIN);
		Assert.equal(Maths.median(IMAX - 1, IMAX), IMAX - 0.5);
		Assert.equal(Maths.median(IMIN + 1, IMIN), IMIN + 0.5);
		Assert.thrown(() -> Maths.median(new int[0]));
		Assert.thrown(() -> Maths.median(new int[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.median((int[]) null, 0, 1));
	}

	@Test
	public void testMedianLong() {
		Assert.equal(Maths.median(-1L), -1.0);
		Assert.equal(Maths.median(1L, -1L), 0.0);
		Assert.equal(Maths.median(1L, -1L, 3L), 1.0);
		Assert.equal(Maths.median(LMAX, LMIN), -0.5);
		Assert.equal(Maths.median(LMAX, LMAX), (double) LMAX);
		Assert.equal(Maths.median(LMIN, LMIN), (double) LMIN);
		Assert.equal(Maths.median(LMAX - 1, LMAX), LMAX - 0.5);
		Assert.equal(Maths.median(LMIN + 1, LMIN), LMIN + 0.5);
		Assert.thrown(() -> Maths.median(new long[0]));
		Assert.thrown(() -> Maths.median(new long[] { 1, -1, 0 }, 2, 2));
		Assert.thrown(() -> Maths.median((long[]) null, 0, 1));
	}

	@Test
	public void testMedianFloat() {
		Assert.equal(Maths.median(-1.0f), -1.0f);
		Assert.equal(Maths.median(1.0f, -1.0f), 0.0f);
		Assert.equal(Maths.median(1.0f, -1.0f, 3.0f), 1.0f);
		Assert.equal(Maths.median(FMAX, -FMAX), 0.0f);
		Assert.equal(Maths.median(FPINF, FPINF), FPINF);
		Assert.equal(Maths.median(FNINF, FPINF), Float.NaN);
		Assert.thrown(() -> Maths.median(new float[0]));
		Assert.thrown(() -> Maths.median(new float[] { 1.0f, -1.0f, 0.0f }, 2, 2));
		Assert.thrown(() -> Maths.median((float[]) null, 0, 1));
	}

	@Test
	public void testMedianDouble() {
		Assert.equal(Maths.median(-1.0), -1.0);
		Assert.equal(Maths.median(1.0, -1.0), 0.0);
		Assert.equal(Maths.median(1.0, -1.0, 3.0), 1.0);
		Assert.equal(Maths.median(DMAX, -DMAX), 0.0);
		Assert.equal(Maths.median(DPINF, DPINF), DPINF);
		Assert.equal(Maths.median(DNINF, DPINF), Double.NaN);
		Assert.thrown(() -> Maths.median(new double[0]));
		Assert.thrown(() -> Maths.median(new double[] { 1.0, -1.0, 0.0 }, 2, 2));
		Assert.thrown(() -> Maths.median((double[]) null, 0, 1));
	}

	@Test
	public void testWithinInt() {
		Assert.equal(Maths.within(0, 0, 0), true);
		Assert.equal(Maths.within(0, -1, 1), true);
		Assert.equal(Maths.within(0, 0, 1), true);
		Assert.equal(Maths.within(0, -1, 0), true);
		Assert.equal(Maths.within(1, -1, 0), false);
		Assert.equal(Maths.within(-1, 0, 1), false);
		Assert.equal(Maths.within(IMAX, IMIN, IMAX), true);
		Assert.equal(Maths.within(IMAX, IMAX, IMAX), true);
		Assert.equal(Maths.within(IMAX, IMIN, IMAX - 1), false);
		Assert.equal(Maths.within(IMIN, IMIN, IMAX), true);
		Assert.equal(Maths.within(IMIN, IMIN, IMIN), true);
		Assert.equal(Maths.within(IMIN, IMIN + 1, IMAX), false);
	}

	@Test
	public void testWithinLong() {
		Assert.equal(Maths.within(0L, 0, 0), true);
		Assert.equal(Maths.within(0L, -1, 1), true);
		Assert.equal(Maths.within(0L, 0, 1), true);
		Assert.equal(Maths.within(0L, -1, 0), true);
		Assert.equal(Maths.within(1L, -1, 0), false);
		Assert.equal(Maths.within(-1L, 0, 1), false);
		Assert.equal(Maths.within(LMAX, LMIN, LMAX), true);
		Assert.equal(Maths.within(LMAX, LMAX, LMAX), true);
		Assert.equal(Maths.within(LMAX, LMIN, LMAX - 1), false);
		Assert.equal(Maths.within(LMIN, LMIN, LMAX), true);
		Assert.equal(Maths.within(LMIN, LMIN, LMIN), true);
		Assert.equal(Maths.within(LMIN, LMIN + 1, LMAX), false);
	}

	@Test
	public void testLimitInt() {
		Assert.equal(Maths.limit(2, -1, 1), 1);
		Assert.equal(Maths.limit(-2, -1, 1), -1);
		Assert.equal(Maths.limit(0, -1, 1), 0);
		Assert.equal(Maths.limit(IMAX, 0, IMAX), IMAX);
		Assert.equal(Maths.limit(IMIN, 0, IMAX), 0);
		Assert.thrown(() -> Maths.limit(0, 1, 0));
	}

	@Test
	public void testLimitLong() {
		Assert.equal(Maths.limit(2L, -1L, 1L), 1L);
		Assert.equal(Maths.limit(-2L, -1L, 1L), -1L);
		Assert.equal(Maths.limit(0L, -1L, 1L), 0L);
		Assert.equal(Maths.limit(LMAX, 0L, LMAX), LMAX);
		Assert.equal(Maths.limit(LMIN, 0L, LMAX), 0L);
		Assert.thrown(() -> Maths.limit(0L, 1L, 0L));
	}

	@Test
	public void testLimitFloat() {
		Assert.equal(Maths.limit(1.0f, -0.5f, -0.5f), -0.5f);
		Assert.equal(Maths.limit(1.0f, -0.5f, 0.5f), 0.5f);
		Assert.equal(Maths.limit(-1.0f, -0.5f, 0.5f), -0.5f);
		Assert.equal(Maths.limit(0.0f, -0.5f, 0.5f), 0.0f);
		Assert.equal(Maths.limit(FMAX, 0.0f, FMAX), FMAX);
		Assert.equal(Maths.limit(-FMAX, 0.0f, FMAX), 0.0f);
		Assert.equal(Maths.limit(FPINF, 0.0f, FMAX), FMAX);
		Assert.equal(Maths.limit(FNINF, 0.0f, FMAX), 0.0f);
		Assert.thrown(() -> Maths.limit(0.0f, 1.0f, 0.0f));
	}

	@Test
	public void testLimitDouble() {
		Assert.equal(Maths.limit(1.0, -0.5, -0.5), -0.5);
		Assert.equal(Maths.limit(1.0, -0.5, 0.5), 0.5);
		Assert.equal(Maths.limit(-1.0, -0.5, 0.5), -0.5);
		Assert.equal(Maths.limit(0.0, -0.5, 0.5), 0.0);
		Assert.equal(Maths.limit(DMAX, 0.0, DMAX), DMAX);
		Assert.equal(Maths.limit(-DMAX, 0.0, DMAX), 0.0);
		Assert.equal(Maths.limit(DPINF, 0.0, DMAX), DMAX);
		Assert.equal(Maths.limit(DNINF, 0.0, DMAX), 0.0);
		Assert.thrown(() -> Maths.limit(0.0, 1.0, 0.0));
	}

	@Test
	public void testPeriodicLimitInt() {
		Assert.equal(Maths.periodicLimit(100, 10, inc), 10);
		Assert.equal(Maths.periodicLimit(100, 10, exc), 0);
		Assert.equal(Maths.periodicLimit(-100, 10, inc), 0);
		Assert.equal(Maths.periodicLimit(-100, 10, exc), 0);
		Assert.equal(Maths.periodicLimit(7, 10, inc), 7);
		Assert.equal(Maths.periodicLimit(-7, 10, inc), 3);
		Assert.equal(Maths.periodicLimit(0, IMAX, inc), 0);
		Assert.equal(Maths.periodicLimit(0, IMAX, exc), 0);
		Assert.equal(Maths.periodicLimit(IMAX, IMAX, inc), IMAX);
		Assert.equal(Maths.periodicLimit(IMAX, IMAX, exc), 0);
		Assert.equal(Maths.periodicLimit(IMIN, IMAX, inc), IMAX - 1);
		Assert.equal(Maths.periodicLimit(IMIN, IMAX, exc), IMAX - 1);
		Assert.thrown(() -> Maths.periodicLimit(100, 10, null));
		Assert.thrown(() -> Maths.periodicLimit(100, 0, inc));
		Assert.thrown(() -> Maths.periodicLimit(100, -10, inc));
	}

	@Test
	public void testPeriodicLimitLong() {
		Assert.equal(Maths.periodicLimit(100L, 10L, inc), 10L);
		Assert.equal(Maths.periodicLimit(100L, 10L, exc), 0L);
		Assert.equal(Maths.periodicLimit(-100L, 10L, inc), 0L);
		Assert.equal(Maths.periodicLimit(-100L, 10L, exc), 0L);
		Assert.equal(Maths.periodicLimit(7L, 10L, inc), 7L);
		Assert.equal(Maths.periodicLimit(-7L, 10L, inc), 3L);
		Assert.equal(Maths.periodicLimit(0L, LMAX, inc), 0L);
		Assert.equal(Maths.periodicLimit(0L, LMAX, exc), 0L);
		Assert.equal(Maths.periodicLimit(LMAX, LMAX, inc), LMAX);
		Assert.equal(Maths.periodicLimit(LMAX, LMAX, exc), 0L);
		Assert.equal(Maths.periodicLimit(LMIN, LMAX, inc), LMAX - 1);
		Assert.equal(Maths.periodicLimit(LMIN, LMAX, exc), LMAX - 1);
		Assert.thrown(() -> Maths.periodicLimit(100L, 10L, null));
		Assert.thrown(() -> Maths.periodicLimit(100L, 0L, inc));
		Assert.thrown(() -> Maths.periodicLimit(100L, -10L, inc));
	}

	@Test
	public void testPeriodicLimitFloat() {
		Assert.equal(Maths.periodicLimit(100.0f, 10.0f, inc), 10.0f);
		Assert.equal(Maths.periodicLimit(100.0f, 10.0f, exc), 0.0f);
		Assert.equal(Maths.periodicLimit(-100.0f, 10.0f, inc), 0.0f);
		Assert.equal(Maths.periodicLimit(-100.0f, 10.0f, exc), 0.0f);
		Assert.equal(Maths.periodicLimit(7.0f, 10.0f, inc), 7.0f);
		Assert.equal(Maths.periodicLimit(-7.0f, 10.0f, inc), 3.0f);
		Assert.equal(Maths.periodicLimit(0.0f, FPINF, inc), 0.0f);
		Assert.equal(Maths.periodicLimit(0.0f, FPINF, exc), 0.0f);
		Assert.equal(Maths.periodicLimit(FPINF, FPINF, inc), FPINF);
		Assert.equal(Maths.periodicLimit(FPINF, FPINF, exc), Float.NaN);
		Assert.equal(Maths.periodicLimit(FNINF, FPINF, inc), Float.NaN);
		Assert.equal(Maths.periodicLimit(FNINF, FPINF, exc), Float.NaN);
		Assert.thrown(() -> Maths.periodicLimit(100.0f, 10.0f, null));
		Assert.thrown(() -> Maths.periodicLimit(100.0f, 0.0f, inc));
		Assert.thrown(() -> Maths.periodicLimit(100.0f, -10.0f, inc));
	}

	@Test
	public void testPeriodicLimitDouble() {
		Assert.equal(Maths.periodicLimit(100.0, 10.0, inc), 10.0);
		Assert.equal(Maths.periodicLimit(100.0, 10.0, exc), 0.0);
		Assert.equal(Maths.periodicLimit(-100.0, 10.0, inc), 0.0);
		Assert.equal(Maths.periodicLimit(-100.0, 10.0, exc), 0.0);
		Assert.equal(Maths.periodicLimit(7.0, 10.0, inc), 7.0);
		Assert.equal(Maths.periodicLimit(-7.0, 10.0, inc), 3.0);
		Assert.equal(Maths.periodicLimit(0.0, DPINF, inc), 0.0);
		Assert.equal(Maths.periodicLimit(0.0, DPINF, exc), 0.0);
		Assert.equal(Maths.periodicLimit(DPINF, DPINF, inc), DPINF);
		Assert.equal(Maths.periodicLimit(DPINF, DPINF, exc), Double.NaN);
		Assert.equal(Maths.periodicLimit(DNINF, DPINF, inc), Double.NaN);
		Assert.equal(Maths.periodicLimit(DNINF, DPINF, exc), Double.NaN);
		Assert.thrown(() -> Maths.periodicLimit(100.0, 10.0, null));
		Assert.thrown(() -> Maths.periodicLimit(100.0, 0.0, inc));
		Assert.thrown(() -> Maths.periodicLimit(100.0, -10.0, inc));
	}

	@Test
	public void testMinByte() {
		byte[] array = { BMIN, -BMAX, -1, 0, 1, BMAX };
		Assert.equal(Maths.min(array), BMIN);
		Assert.equal(Maths.min(array, 1, 5), (byte) -BMAX);
		Assert.equal(Maths.min(array, 2, 3), (byte) -1);
		Assert.thrown(() -> Maths.min(new byte[0]));
		Assert.thrown(() -> Maths.min(new byte[2], 1, 0));
		Assert.thrown(() -> Maths.min((byte[]) null));
		Assert.thrown(() -> Maths.min(new byte[3], 1, 3));
	}

	@Test
	public void testMinShort() {
		short[] array = { SMIN, -SMAX, -1, 0, 1, SMAX };
		Assert.equal(Maths.min(array), SMIN);
		Assert.equal(Maths.min(array, 1, 5), (short) -SMAX);
		Assert.equal(Maths.min(array, 2, 3), (short) -1);
		Assert.thrown(() -> Maths.min(new short[0]));
		Assert.thrown(() -> Maths.min(new short[2], 1, 0));
		Assert.thrown(() -> Maths.min((short[]) null));
		Assert.thrown(() -> Maths.min(new short[3], 1, 3));
	}

	@Test
	public void testMinInt() {
		int[] array = { IMIN, -IMAX, -1, 0, 1, IMAX };
		Assert.equal(Maths.min(array), IMIN);
		Assert.equal(Maths.min(array, 1, 5), -IMAX);
		Assert.equal(Maths.min(array, 2, 3), -1);
		Assert.thrown(() -> Maths.min(new int[0]));
		Assert.thrown(() -> Maths.min(new int[2], 1, 0));
		Assert.thrown(() -> Maths.min((int[]) null));
		Assert.thrown(() -> Maths.min(new int[3], 1, 3));
	}

	@Test
	public void testMinLong() {
		long[] array = { LMIN, -LMAX, -1, 0, 1, LMAX };
		Assert.equal(Maths.min(array), LMIN);
		Assert.equal(Maths.min(array, 1, 5), -LMAX);
		Assert.equal(Maths.min(array, 2, 3), -1L);
		Assert.thrown(() -> Maths.min(new long[0]));
		Assert.thrown(() -> Maths.min(new long[2], 1, 0));
		Assert.thrown(() -> Maths.min((long[]) null));
		Assert.thrown(() -> Maths.min(new long[3], 1, 3));
	}

	@Test
	public void testMinFloat() {
		float[] d = { FNINF, -FMAX, FMAX, -1, 0, 1, FMIN, FPINF, Float.NaN };
		Assert.equal(Maths.min(d), Float.NaN);
		Assert.equal(Maths.min(d, 0, 8), FNINF);
		Assert.equal(Maths.min(d, 1, 7), -FMAX);
		Assert.equal(Maths.min(d, 3, 3), -1.0f);
		Assert.thrown(() -> Maths.min(new float[0]));
		Assert.thrown(() -> Maths.min(new float[2], 1, 0));
		Assert.thrown(() -> Maths.min((float[]) null));
		Assert.thrown(() -> Maths.min(new float[3], 1, 3));
	}

	@Test
	public void testMinDouble() {
		double[] d = { DNINF, -DMAX, DMAX, -1, 0, 1, DMIN, DPINF, Double.NaN };
		Assert.equal(Maths.min(d), Double.NaN);
		Assert.equal(Maths.min(d, 0, 8), DNINF);
		Assert.equal(Maths.min(d, 1, 7), -DMAX);
		Assert.equal(Maths.min(d, 3, 3), -1.0);
		Assert.thrown(() -> Maths.min(new double[0]));
		Assert.thrown(() -> Maths.min(new double[2], 1, 0));
		Assert.thrown(() -> Maths.min((double[]) null));
		Assert.thrown(() -> Maths.min(new double[3], 1, 3));
	}

	@Test
	public void testMaxByte() {
		byte[] array = { BMIN, -BMAX, -1, 0, 1, BMAX };
		Assert.equal(Maths.max(array), BMAX);
		Assert.equal(Maths.max(array, 0, 2), (byte) -BMAX);
		Assert.equal(Maths.max(array, 0, 5), (byte) 1);
		Assert.thrown(() -> Maths.max(new byte[0]));
		Assert.thrown(() -> Maths.max(new byte[2], 1, 0));
		Assert.thrown(() -> Maths.max((byte[]) null));
		Assert.thrown(() -> Maths.max(new byte[3], 1, 3));
	}

	@Test
	public void testMaxShort() {
		short[] array = { SMIN, -SMAX, -1, 0, 1, SMAX };
		Assert.equal(Maths.max(array), SMAX);
		Assert.equal(Maths.max(array, 0, 2), (short) -SMAX);
		Assert.equal(Maths.max(array, 0, 5), (short) 1);
		Assert.thrown(() -> Maths.max(new short[0]));
		Assert.thrown(() -> Maths.max(new short[2], 1, 0));
		Assert.thrown(() -> Maths.max((short[]) null));
		Assert.thrown(() -> Maths.max(new short[3], 1, 3));
	}

	@Test
	public void testMaxInt() {
		int[] array = { IMIN, -IMAX, -1, 0, 1, IMAX };
		Assert.equal(Maths.max(array), IMAX);
		Assert.equal(Maths.max(array, 0, 2), -IMAX);
		Assert.equal(Maths.max(array, 0, 5), 1);
		Assert.thrown(() -> Maths.max(new int[0]));
		Assert.thrown(() -> Maths.max(new int[2], 1, 0));
		Assert.thrown(() -> Maths.max((int[]) null));
		Assert.thrown(() -> Maths.max(new int[3], 1, 3));
	}

	@Test
	public void testMaxLong() {
		long[] array = { LMIN, -LMAX, -1, 0, 1, LMAX };
		Assert.equal(Maths.max(array), LMAX);
		Assert.equal(Maths.max(array, 0, 2), -LMAX);
		Assert.equal(Maths.max(array, 0, 5), 1L);
		Assert.thrown(() -> Maths.max(new long[0]));
		Assert.thrown(() -> Maths.max(new long[2], 1, 0));
		Assert.thrown(() -> Maths.max((long[]) null));
		Assert.thrown(() -> Maths.max(new long[3], 1, 3));
	}

	@Test
	public void testMaxFloat() {
		float[] array = { FNINF, -FMAX, FMAX, -1, 0, 1, FMIN, FPINF, Float.NaN };
		Assert.equal(Maths.max(array), Float.NaN);
		Assert.equal(Maths.max(array, 0, 8), FPINF);
		Assert.equal(Maths.max(array, 0, 7), FMAX);
		Assert.equal(Maths.max(array, 3, 3), 1.0f);
		Assert.thrown(() -> Maths.max(new float[0]));
		Assert.thrown(() -> Maths.max(new float[2], 1, 0));
		Assert.thrown(() -> Maths.max((float[]) null));
		Assert.thrown(() -> Maths.max(new float[3], 1, 3));
	}

	@Test
	public void testMaxDouble() {
		double[] array = { DNINF, -DMAX, DMAX, -1, 0, 1, DMIN, DPINF, Double.NaN };
		Assert.equal(Maths.max(array), Double.NaN);
		Assert.equal(Maths.max(array, 0, 8), DPINF);
		Assert.equal(Maths.max(array, 0, 7), DMAX);
		Assert.equal(Maths.max(array, 3, 3), 1.0);
		Assert.thrown(() -> Maths.max(new double[0]));
		Assert.thrown(() -> Maths.max(new double[2], 1, 0));
		Assert.thrown(() -> Maths.max((double[]) null));
		Assert.thrown(() -> Maths.max(new double[3], 1, 3));
	}

	/**
	 * Tests positive and negative combinations of numerator and denominator.
	 */
	private void assertRoundDiv(int x, int y, int pp, int np, int pn, int nn) {
		Assert.equal(Maths.roundDiv(x, y), pp);
		Assert.equal(Maths.roundDiv(-x, y), np);
		Assert.equal(Maths.roundDiv(x, -y), pn);
		Assert.equal(Maths.roundDiv(-x, -y), nn);
	}

	/**
	 * Tests positive and negative combinations of numerator and denominator.
	 */
	private void assertRoundDiv(long x, long y, long pp, long np, long pn, long nn) {
		Assert.equal(Maths.roundDiv(x, y), pp);
		Assert.equal(Maths.roundDiv(-x, y), np);
		Assert.equal(Maths.roundDiv(x, -y), pn);
		Assert.equal(Maths.roundDiv(-x, -y), nn);
	}

	private static void assertOverflow(int l, int r, boolean overflow) {
		Assert.equal(Maths.overflow(l + r, l, r), overflow);
	}

	private static void assertOverflow(long l, long r, boolean overflow) {
		Assert.equal(Maths.overflow(l + r, l, r), overflow);
	}

}