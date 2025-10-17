package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.Set;
import org.junit.Test;
import ceri.common.collect.Sets;
import ceri.common.function.Excepts;

public class ValidateTest {
	private static final String TEST = "test";
	private static final Object OBJ = new Object();
	private static final String nullStr = null;
	private static final Object[] nullArr = null;
	private static final Set<Object> nullSet = null;
	private static final int IMIN = Integer.MIN_VALUE;
	private static final int IMAX = Integer.MAX_VALUE;
	private static final long LMIN = Long.MIN_VALUE;
	private static final long LMAX = Long.MAX_VALUE;
	private static final double DNINF = Double.NEGATIVE_INFINITY;
	private static final double DPINF = Double.POSITIVE_INFINITY;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Validate.class);
	}

	@Test
	public void testcondition() {
		assertInvalid(() -> Validate.condition(false));
		assertInvalid(() -> Validate.condition(false, TEST));
		assertEquals(Validate.condition(true), true);
		assertEquals(Validate.condition(true, TEST), true);
	}

	@Test
	public void testEqual() {
		assertEquals(Validate.equals(null, null), null);
		assertEquals(Validate.equals(OBJ, OBJ), OBJ);
		assertInvalid(() -> Validate.equals(OBJ, null));
		assertInvalid(() -> Validate.equals(null, OBJ));
	}

	@Test
	public void testNotEqual() {
		assertEquals(Validate.notEqual(null, OBJ), null);
		assertEquals(Validate.notEqual(OBJ, null), OBJ);
		assertInvalid(() -> Validate.notEqual(null, null));
		assertInvalid(() -> Validate.notEqual(OBJ, OBJ));
	}

	@Test
	public void testNonNull() {
		Validate.nonNull(OBJ);
		Validate.nonNull(OBJ, TEST);
		assertInvalid(() -> Validate.nonNull(null));
		assertInvalid(() -> Validate.nonNull(null, TEST));
	}

	@Test
	public void testEqualAny() {
		assertInvalid(() -> Validate.equalAnyOf(null));
		assertInvalid(() -> Validate.equalAnyOf(OBJ));
		assertInvalid(() -> Validate.equalAnyOf(OBJ, null, ""));
		assertEquals(Validate.equalAnyOf(null, OBJ, null, ""), null);
		assertEquals(Validate.equalAnyOf("", OBJ, null, ""), "");
	}

	@Test
	public void testEqualNone() {
		assertInvalid(() -> Validate.equalNoneOf(null, null, OBJ, ""));
		assertInvalid(() -> Validate.equalNoneOf(OBJ, null, OBJ, ""));
		assertEquals(Validate.equalNoneOf(null), null);
		assertEquals(Validate.equalNoneOf(OBJ, null, ""), OBJ);
	}

	@Test
	public void testAllNonNull() {
		Validate.allNonNull();
		Validate.allNonNull(OBJ, "");
		assertInvalid(() -> Validate.allNonNull(OBJ, null, ""));
		assertInvalid(() -> Validate.allNonNull(nullArr));
	}

	@Test
	public void testIndex() {
		int[] array = { 1, 2, 3, 4 };
		Validate.index(array.length, 0);
		Validate.index(array.length, 3);
		assertInvalid(() -> Validate.index(array.length, -1));
		assertInvalid(() -> Validate.index(array.length, 4));
	}

	@Test
	public void testNonEmpty() {
		assertInvalid(() -> Validate.nonEmpty(nullSet));
		assertInvalid(() -> Validate.nonEmpty(Set.of()));
		assertUnordered(Validate.nonEmpty(Sets.ofAll(nullStr)), nullStr);
	}

	@Test
	public void testEqualInt() {
		assertEquals(Validate.equal(IMIN, IMIN), IMIN);
		assertEquals(Validate.equal(IMAX, IMAX), IMAX);
		assertInvalid(() -> Validate.equal(IMIN, IMIN + 1));
		assertEquals(Validate.equal(LMIN, LMIN), LMIN);
		assertEquals(Validate.equal(LMAX, LMAX), LMAX);
		assertInvalid(() -> Validate.equal(LMIN, LMIN + 1));
	}

	@Test
	public void testNotEqualInt() {
		assertEquals(Validate.notEqual(IMIN, IMIN + 1), IMIN);
		assertEquals(Validate.notEqual(IMAX, IMAX - 1), IMAX);
		assertInvalid(() -> Validate.notEqual(IMIN, IMIN));
		assertEquals(Validate.notEqual(LMIN, LMIN + 1), LMIN);
		assertEquals(Validate.notEqual(LMAX, LMAX - 1), LMAX);
		assertInvalid(() -> Validate.notEqual(LMIN, LMIN));
	}

	@Test
	public void testMinInt() {
		assertEquals(Validate.min(IMIN, IMIN), IMIN);
		assertEquals(Validate.min(IMAX, IMAX), IMAX);
		assertInvalid(() -> Validate.min(IMIN, IMIN + 1));
		assertEquals(Validate.min(LMIN, LMIN), LMIN);
		assertEquals(Validate.min(LMAX, LMAX), LMAX);
		assertInvalid(() -> Validate.min(LMIN, LMIN + 1));
	}

	@Test
	public void testMaxInt() {
		assertEquals(Validate.max(IMIN, IMIN), IMIN);
		assertEquals(Validate.max(IMAX, IMAX), IMAX);
		assertInvalid(() -> Validate.max(IMAX, IMAX - 1));
		assertEquals(Validate.max(LMIN, LMIN), LMIN);
		assertEquals(Validate.max(LMAX, LMAX), LMAX);
		assertInvalid(() -> Validate.max(LMAX, LMAX - 1));
	}

	@Test
	public void testUbyte() {
		assertEquals(Validate.ubyte(0), 0);
		assertEquals(Validate.ubyte(0xff), 0xff);
		assertInvalid(() -> Validate.ubyte(-1));
		assertInvalid(() -> Validate.ubyte(0x100));
		assertEquals(Validate.ubyte(-1, -1), 0xff);
		assertInvalid(() -> Validate.ubyte(-2, -1));
	}

	@Test
	public void testUshort() {
		assertEquals(Validate.ushort(0), 0);
		assertEquals(Validate.ushort(0xffff), 0xffff);
		assertInvalid(() -> Validate.ushort(-1));
		assertInvalid(() -> Validate.ushort(0x10000));
		assertEquals(Validate.ushort(-1, -1), 0xffff);
		assertInvalid(() -> Validate.ushort(-2, -1));
	}

	@Test
	public void testUint() {
		assertEquals(Validate.uint(0), 0L);
		assertEquals(Validate.uint(0xffffffffL), 0xffffffffL);
		assertInvalid(() -> Validate.uint(-1));
		assertInvalid(() -> Validate.uint(0x100000000L));
		assertEquals(Validate.uint(-1, -1), 0xffffffffL);
		assertInvalid(() -> Validate.uint(-2, -1));
	}

	@Test
	public void testUmin() {
		assertEquals(Validate.umin(-1L, -1L), -1L);
		assertEquals(Validate.umin(-1L, 0xffffffff_00000000L), -1L);
		assertInvalid(() -> Validate.umin(0xffffffff_00000000L, -1));
	}

	@Test
	public void testUmax() {
		assertEquals(Validate.umax(-1L, -1L), -1L);
		assertEquals(Validate.umax(0xffffffff_00000000L, -1L), 0xffffffff_00000000L);
		assertInvalid(() -> Validate.umax(-1L, 0xffffffff_00000000L));
	}

	@Test
	public void testUrange() {
		assertEquals(Validate.urange(-1L, -1L, -1L), -1L);
		assertEquals(Validate.urange(0xffffffff_00000000L, 0L, -1L), 0xffffffff_00000000L);
		assertInvalid(() -> Validate.urange(-1L, 0L, 0xffffffff_00000000L));
		assertInvalid(() -> Validate.urange(0L, 0xffffffff_00000000L, -1L));
	}

	@Test
	public void testEqualDouble() {
		assertEquals(Validate.equal(Double.NaN, Double.NaN), Double.NaN);
		assertEquals(Validate.equal(DNINF, DNINF), DNINF);
		assertEquals(Validate.equal(DPINF, DPINF), DPINF);
		assertEquals(Validate.equal(-0.0, 0.0), 0.0);
		assertEquals(Validate.equal(0.0, -0.0), 0.0);
		assertEquals(Validate.equal(Math.PI, Math.PI), Math.PI);
		assertInvalid(() -> Validate.equal(DNINF, DPINF));
		assertInvalid(() -> Validate.equal(0.0000100001, 0.00001));
	}

	@Test
	public void testApprox() {
		assertEquals(Validate.approx(123.2345, 123.2345), 123.2345);
		assertEquals(Validate.approx(123.2345, 123.235), 123.2345);
		assertEquals(Validate.approx(Double.NaN, Double.NaN), Double.NaN);
		assertEquals(Validate.approx(DNINF, DNINF), DNINF);
		assertInvalid(() -> Validate.approx(123.2345, 123.234));
	}

	@Test
	public void testMinDouble() {
		assertEquals(Validate.min(0.0001, 0.0001), 0.0001);
		assertEquals(Validate.min(-0.0001, -0.0001), -0.0001);
		assertEquals(Validate.min(DPINF, 0), DPINF);
		assertInvalid(() -> Validate.min(0.0001, 0.0001001));
		assertInvalid(() -> Validate.min(-0.0001001, -0.0001));
	}

	@Test
	public void testMaxDouble() {
		assertEquals(Validate.max(0.0001, 0.0001), 0.0001);
		assertEquals(Validate.max(-0.0001, -0.0001), -0.0001);
		assertEquals(Validate.max(DNINF, 0), DNINF);
		assertInvalid(() -> Validate.max(0.0001001, 0.0001));
		assertInvalid(() -> Validate.max(-0.0001, -0.0001001));
	}

	@Test
	public void testNotNaN() {
		assertEquals(Validate.notNaN(0.00001), 0.00001);
		assertEquals(Validate.notNaN(DNINF), DNINF);
		assertInvalid(() -> Validate.notNaN(Double.NaN));
	}

	@Test
	public void testFiniteMin() {
		assertEquals(Validate.finiteMin(0.0001, 0.0001), 0.0001);
		assertEquals(Validate.finiteMin(-0.0001, -0.0001), -0.0001);
		assertInvalid(() -> Validate.finiteMin(DPINF, 0));
		assertInvalid(() -> Validate.finiteMin(0.0001, 0.0001001));
		assertInvalid(() -> Validate.finiteMin(-0.0001001, -0.0001));
	}

	@Test
	public void testFinitMax() {
		assertEquals(Validate.finiteMax(0.0001, 0.0001), 0.0001);
		assertEquals(Validate.finiteMax(-0.0001, -0.0001), -0.0001);
		assertInvalid(() -> Validate.finiteMax(DNINF, 0));
		assertInvalid(() -> Validate.finiteMax(0.0001001, 0.0001));
		assertInvalid(() -> Validate.finiteMax(-0.0001, -0.0001001));
	}

	private static void assertInvalid(Excepts.Runnable<?> runnable) {
		assertIllegalArg(runnable);
	}
}
