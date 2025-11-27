package ceri.common.util;

import java.util.Set;
import org.junit.Test;
import ceri.common.collect.Sets;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;

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
		Assert.privateConstructor(Validate.class);
	}

	@Test
	public void testcondition() {
		assertInvalid(() -> Validate.condition(false));
		assertInvalid(() -> Validate.condition(false, TEST));
		Assert.equal(Validate.condition(true), true);
		Assert.equal(Validate.condition(true, TEST), true);
	}

	@Test
	public void testInstance() {
		assertInvalid(() -> Validate.instance(null, Object.class));
		assertInvalid(() -> Validate.instance("test", Number.class));
		Assert.equal(Validate.instance("test", Object.class), "test");
		Assert.equal(Validate.instance("test", CharSequence.class), "test");
		Assert.equal(Validate.instance("test", String.class), "test");
	}

	@Test
	public void testEqual() {
		Assert.equal(Validate.equals(null, null), null);
		Assert.equal(Validate.equals(OBJ, OBJ), OBJ);
		assertInvalid(() -> Validate.equals(OBJ, null));
		assertInvalid(() -> Validate.equals(null, OBJ));
	}

	@Test
	public void testNotEqual() {
		Assert.equal(Validate.notEqual(null, OBJ), null);
		Assert.equal(Validate.notEqual(OBJ, null), OBJ);
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
		Assert.equal(Validate.equalAnyOf(null, OBJ, null, ""), null);
		Assert.equal(Validate.equalAnyOf("", OBJ, null, ""), "");
	}

	@Test
	public void testEqualNone() {
		assertInvalid(() -> Validate.equalNoneOf(null, null, OBJ, ""));
		assertInvalid(() -> Validate.equalNoneOf(OBJ, null, OBJ, ""));
		Assert.equal(Validate.equalNoneOf(null), null);
		Assert.equal(Validate.equalNoneOf(OBJ, null, ""), OBJ);
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
		Assert.unordered(Validate.nonEmpty(Sets.ofAll(nullStr)), nullStr);
	}

	@Test
	public void testEqualInt() {
		Assert.equal(Validate.equal(IMIN, IMIN), IMIN);
		Assert.equal(Validate.equal(IMAX, IMAX), IMAX);
		assertInvalid(() -> Validate.equal(IMIN, IMIN + 1));
		Assert.equal(Validate.equal(LMIN, LMIN), LMIN);
		Assert.equal(Validate.equal(LMAX, LMAX), LMAX);
		assertInvalid(() -> Validate.equal(LMIN, LMIN + 1));
	}

	@Test
	public void testNotEqualInt() {
		Assert.equal(Validate.notEqual(IMIN, IMIN + 1), IMIN);
		Assert.equal(Validate.notEqual(IMAX, IMAX - 1), IMAX);
		assertInvalid(() -> Validate.notEqual(IMIN, IMIN));
		Assert.equal(Validate.notEqual(LMIN, LMIN + 1), LMIN);
		Assert.equal(Validate.notEqual(LMAX, LMAX - 1), LMAX);
		assertInvalid(() -> Validate.notEqual(LMIN, LMIN));
	}

	@Test
	public void testMinInt() {
		Assert.equal(Validate.min(IMIN, IMIN), IMIN);
		Assert.equal(Validate.min(IMAX, IMAX), IMAX);
		assertInvalid(() -> Validate.min(IMIN, IMIN + 1));
		Assert.equal(Validate.min(LMIN, LMIN), LMIN);
		Assert.equal(Validate.min(LMAX, LMAX), LMAX);
		assertInvalid(() -> Validate.min(LMIN, LMIN + 1));
	}

	@Test
	public void testMaxInt() {
		Assert.equal(Validate.max(IMIN, IMIN), IMIN);
		Assert.equal(Validate.max(IMAX, IMAX), IMAX);
		assertInvalid(() -> Validate.max(IMAX, IMAX - 1));
		Assert.equal(Validate.max(LMIN, LMIN), LMIN);
		Assert.equal(Validate.max(LMAX, LMAX), LMAX);
		assertInvalid(() -> Validate.max(LMAX, LMAX - 1));
	}

	@Test
	public void testUbyte() {
		Assert.equal(Validate.ubyte(0), 0);
		Assert.equal(Validate.ubyte(0xff), 0xff);
		assertInvalid(() -> Validate.ubyte(-1));
		assertInvalid(() -> Validate.ubyte(0x100));
		Assert.equal(Validate.ubyte(-1, -1), 0xff);
		assertInvalid(() -> Validate.ubyte(-2, -1));
	}

	@Test
	public void testUshort() {
		Assert.equal(Validate.ushort(0), 0);
		Assert.equal(Validate.ushort(0xffff), 0xffff);
		assertInvalid(() -> Validate.ushort(-1));
		assertInvalid(() -> Validate.ushort(0x10000));
		Assert.equal(Validate.ushort(-1, -1), 0xffff);
		assertInvalid(() -> Validate.ushort(-2, -1));
	}

	@Test
	public void testUint() {
		Assert.equal(Validate.uint(0), 0L);
		Assert.equal(Validate.uint(0xffffffffL), 0xffffffffL);
		assertInvalid(() -> Validate.uint(-1));
		assertInvalid(() -> Validate.uint(0x100000000L));
		Assert.equal(Validate.uint(-1, -1), 0xffffffffL);
		assertInvalid(() -> Validate.uint(-2, -1));
	}

	@Test
	public void testUmin() {
		Assert.equal(Validate.umin(-1L, -1L), -1L);
		Assert.equal(Validate.umin(-1L, 0xffffffff_00000000L), -1L);
		assertInvalid(() -> Validate.umin(0xffffffff_00000000L, -1));
	}

	@Test
	public void testUmax() {
		Assert.equal(Validate.umax(-1L, -1L), -1L);
		Assert.equal(Validate.umax(0xffffffff_00000000L, -1L), 0xffffffff_00000000L);
		assertInvalid(() -> Validate.umax(-1L, 0xffffffff_00000000L));
	}

	@Test
	public void testUrange() {
		Assert.equal(Validate.urange(-1L, -1L, -1L), -1L);
		Assert.equal(Validate.urange(0xffffffff_00000000L, 0L, -1L), 0xffffffff_00000000L);
		assertInvalid(() -> Validate.urange(-1L, 0L, 0xffffffff_00000000L));
		assertInvalid(() -> Validate.urange(0L, 0xffffffff_00000000L, -1L));
	}

	@Test
	public void testEqualDouble() {
		Assert.equal(Validate.equal(Double.NaN, Double.NaN), Double.NaN);
		Assert.equal(Validate.equal(DNINF, DNINF), DNINF);
		Assert.equal(Validate.equal(DPINF, DPINF), DPINF);
		Assert.equal(Validate.equal(-0.0, 0.0), 0.0);
		Assert.equal(Validate.equal(0.0, -0.0), 0.0);
		Assert.equal(Validate.equal(Math.PI, Math.PI), Math.PI);
		assertInvalid(() -> Validate.equal(DNINF, DPINF));
		assertInvalid(() -> Validate.equal(0.0000100001, 0.00001));
	}

	@Test
	public void testApprox() {
		Assert.equal(Validate.approx(123.2345, 123.2345), 123.2345);
		Assert.equal(Validate.approx(123.2345, 123.235), 123.2345);
		Assert.equal(Validate.approx(Double.NaN, Double.NaN), Double.NaN);
		Assert.equal(Validate.approx(DNINF, DNINF), DNINF);
		assertInvalid(() -> Validate.approx(123.2345, 123.234));
	}

	@Test
	public void testMinDouble() {
		Assert.equal(Validate.min(0.0001, 0.0001), 0.0001);
		Assert.equal(Validate.min(-0.0001, -0.0001), -0.0001);
		Assert.equal(Validate.min(DPINF, 0), DPINF);
		assertInvalid(() -> Validate.min(0.0001, 0.0001001));
		assertInvalid(() -> Validate.min(-0.0001001, -0.0001));
	}

	@Test
	public void testMaxDouble() {
		Assert.equal(Validate.max(0.0001, 0.0001), 0.0001);
		Assert.equal(Validate.max(-0.0001, -0.0001), -0.0001);
		Assert.equal(Validate.max(DNINF, 0), DNINF);
		assertInvalid(() -> Validate.max(0.0001001, 0.0001));
		assertInvalid(() -> Validate.max(-0.0001, -0.0001001));
	}

	@Test
	public void testNotNaN() {
		Assert.equal(Validate.notNaN(0.00001), 0.00001);
		Assert.equal(Validate.notNaN(DNINF), DNINF);
		assertInvalid(() -> Validate.notNaN(Double.NaN));
	}

	@Test
	public void testFiniteMin() {
		Assert.equal(Validate.finiteMin(0.0001, 0.0001), 0.0001);
		Assert.equal(Validate.finiteMin(-0.0001, -0.0001), -0.0001);
		assertInvalid(() -> Validate.finiteMin(DPINF, 0));
		assertInvalid(() -> Validate.finiteMin(0.0001, 0.0001001));
		assertInvalid(() -> Validate.finiteMin(-0.0001001, -0.0001));
	}

	@Test
	public void testFinitMax() {
		Assert.equal(Validate.finiteMax(0.0001, 0.0001), 0.0001);
		Assert.equal(Validate.finiteMax(-0.0001, -0.0001), -0.0001);
		assertInvalid(() -> Validate.finiteMax(DNINF, 0));
		assertInvalid(() -> Validate.finiteMax(0.0001001, 0.0001));
		assertInvalid(() -> Validate.finiteMax(-0.0001, -0.0001001));
	}

	private static void assertInvalid(Excepts.Runnable<?> runnable) {
		Assert.illegalArg(runnable);
	}
}
