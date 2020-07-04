package ceri.common.validation;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static ceri.common.math.Bound.Type.*;
import ceri.common.math.Interval;
import ceri.common.test.TestUtil;

public class ValidationUtilTest {
	private static final Object OBJ = new Object();
	private static final Integer I0 = 999;
	private static final Integer I1 = 999;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ValidationUtil.class);
	}

	@Test
	public void testValidatePredicate() {
		ValidationUtil.validate(i -> i < 0, -1);
		TestUtil.assertThrown(() -> ValidationUtil.validate(i -> i < 0, 0));
		ValidationUtil.validate(i -> i < 0, -1, "int");
		TestUtil.assertThrown(() -> ValidationUtil.validate(i -> i < 0, 0, "int"));
	}

	@Test
	public void testValidateExpression() {
		ValidationUtil.validate(1 > 0);
		ValidationUtil.validate(1 > 0, null);
		ValidationUtil.validate(1 > 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validate(1 < 0));
		TestUtil.assertThrown(() -> ValidationUtil.validate(1 < 0, null));
		TestUtil.assertThrown(() -> ValidationUtil.validate(1 < 0, "test"));
	}

	@Test
	public void testValidateWithFormattedException() {
		int i0 = -1, i1 = 1;
		ValidationUtil.validatef(i0 < 0, "%d >= 0", i0);
		TestUtil.assertThrown(() -> ValidationUtil.validatef(i1 < 0, "%d >= 0", i1));
	}

	@Test
	public void testValidateNotNull() {
		ValidationUtil.validateNotNull(OBJ);
		ValidationUtil.validateNotNull(OBJ, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateNotNull(null));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotNull(null, "test"));
	}

	@Test
	public void testValidateNull() {
		ValidationUtil.validateNull(null);
		TestUtil.assertThrown(() -> ValidationUtil.validateNull(""));
	}

	@Test
	public void testValidateObjectEquality() {
		ValidationUtil.validateEqualObj(OBJ, OBJ);
		ValidationUtil.validateEqualObj(null, null);
		ValidationUtil.validateEqualObj(I0, I1);
		TestUtil.assertThrown(() -> ValidationUtil.validateEqualObj(null, OBJ));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqualObj(OBJ, null));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqualObj(OBJ, I0));
	}

	@Test
	public void testValidateObjectInequality() {
		ValidationUtil.validateNotEqualObj(null, OBJ);
		ValidationUtil.validateNotEqualObj(OBJ, null);
		ValidationUtil.validateNotEqualObj(I0, OBJ);
		TestUtil.assertThrown(() -> ValidationUtil.validateNotEqualObj(null, null));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotEqualObj(OBJ, OBJ));
	}

	@Test
	public void testValidateNumberEquality() {
		ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MIN_VALUE, DisplayLong.hex);
		ValidationUtil.validateEqual(Double.MIN_VALUE, Double.MIN_VALUE, DisplayDouble.round1);
		TestUtil.assertThrown(
			() -> ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MAX_VALUE, DisplayLong.hex16));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Double.MAX_VALUE, Double.MIN_VALUE,
			DisplayDouble.std));
	}

	@Test
	public void testValidateNumberEqualityFormat() {
		assertThat(
			TestUtil.thrown(() -> ValidationUtil.validateEqual(-1, 0xff, (String) null,
				DisplayLong.dec, DisplayLong.hex4)).getMessage(),
			is("Value != (255, 0x00ff): (-1, 0xffff)"));
		assertThat(TestUtil
			.thrown(() -> ValidationUtil.validateEqual(1, 1.111, "Num", DisplayDouble.round1))
			.getMessage(), is("Num != 1.1: 1.0"));
	}

	@Test
	public void testValidateNumberInequality() {
		ValidationUtil.validateNotEqual(Long.MIN_VALUE, Long.MAX_VALUE, DisplayLong.udec);
		ValidationUtil.validateNotEqual(Double.MIN_VALUE, Double.MAX_VALUE, DisplayDouble.round);
		TestUtil.assertThrown(() -> ValidationUtil.validateNotEqual(Long.MIN_VALUE, Long.MIN_VALUE,
			DisplayLong.hex4));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateNotEqual(1.00001, 1.00001, DisplayDouble.round1));
	}

	@Test
	public void testValidateUbyte() {
		ValidationUtil.validateUbyte(0xff, -1);
		ValidationUtil.validateUbyte(0xffff, -1);
		TestUtil.assertThrown(() -> ValidationUtil.validateUbyte(0xf, -1, "test"));
		ValidationUtil.validateUbyte(0);
		ValidationUtil.validateUbyte(0xff);
		assertThrown(() -> ValidationUtil.validateUbyte(-1));
		assertThrown(() -> ValidationUtil.validateUbyte(0x100));
		assertThat(TestUtil.thrown(() -> ValidationUtil.validateUbyte(-1, "Byte", DisplayLong.dec))
			.getMessage(), is("Byte is not within [0, 255]: -1"));
	}

	@Test
	public void testValidateUshort() {
		ValidationUtil.validateUshort(0xffff, -1);
		ValidationUtil.validateUshort(0xffffff, -1);
		TestUtil.assertThrown(() -> ValidationUtil.validateUshort(0xff, -1, "test"));
		ValidationUtil.validateUshort(0);
		ValidationUtil.validateUshort(0xffff);
		assertThrown(() -> ValidationUtil.validateUshort(-1));
		assertThrown(() -> ValidationUtil.validateUshort(0x10000));
	}
	
	@Test
	public void testValidateUint() {
		ValidationUtil.validateUint(0xffffffff, -1);
		ValidationUtil.validateUint(0xffffffffffL, -1);
		TestUtil.assertThrown(() -> ValidationUtil.validateUint(0xffff, -1, "test"));
		ValidationUtil.validateUint(0);
		ValidationUtil.validateUint(0xffffffffL);
		assertThrown(() -> ValidationUtil.validateUint(-1));
		assertThrown(() -> ValidationUtil.validateUint(0x100000000L));
	}
	
	@Test
	public void testValidateUlong() {
		ValidationUtil.validateUlong(0xffffffff12345678L, 0xffffffff12345678L);
		ValidationUtil.validateUlong(0xffffffffffffffffL, -1L);
		ValidationUtil.validateUlong(Long.MIN_VALUE, Long.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateUlong(-1L, -2L, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUlong( //
			0xffffffff12345678L, 0xffffffff12345670L));
	}

	@Test
	public void testValidateWithinObj() {
		ValidationUtil.validateWithinObj(1, Interval.inclusive(0, 1));
		ValidationUtil.validateWithinObj(0.5, Interval.exclusive(0.0, 1.0));
		assertThrown(() -> ValidationUtil.validateWithinObj(1, Interval.exclusive(0, 1)));
		assertThrown(() -> ValidationUtil.validateWithinObj(0, Interval.exclusive(0, 1)));
		assertThrown(() -> ValidationUtil.validateWithinObj(1d, Interval.exclusive(0d, 1d)));
		assertThrown(() -> ValidationUtil.validateWithinObj(0d, Interval.exclusive(0d, 1d)));
	}

	@Test
	public void testValidateWithoutObj() {
		ValidationUtil.validateWithoutObj(1, Interval.exclusive(0, 1));
		assertThrown(() -> ValidationUtil.validateWithoutObj(1, Interval.inclusive(0, 1)));
	}

	@Test
	public void testValidateWithin() {
		assertThrown(() -> ValidationUtil.validateWithin(1, Interval.exclusive(0L, 1L)));
		ValidationUtil.validateWithin(1, Interval.inclusive(0L, 1L), DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateWithin(1, Interval.exclusive(0L, 1L), DisplayLong.hex));
		ValidationUtil.validateWithin(1, Interval.inclusive(0.0, 1.0), DisplayDouble.round);
		assertThrown(() -> ValidationUtil.validateWithin(1, Interval.exclusive(0.0, 1.0),
			DisplayDouble.round));
	}

	@Test
	public void testValidateWithout() {
		ValidationUtil.validateWithout(1, Interval.exclusive(0L, 1L), DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateWithout(1, Interval.inclusive(0L, 1L), DisplayLong.hex));
		ValidationUtil.validateWithout(1, Interval.exclusive(0.0, 1.0), DisplayDouble.round);
		assertThrown(() -> ValidationUtil.validateWithout(1, Interval.inclusive(0.0, 1.0),
			DisplayDouble.round));
	}

	@Test
	public void testvalidateMinong() {
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMin(Long.MAX_VALUE, 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(-1, 0, "test"));
	}

	@Test
	public void testValidateMinDouble() {
		ValidationUtil.validateMin(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMin(Double.MAX_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMin(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMin(Double.MIN_VALUE, 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Double.MIN_VALUE, Double.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(0, Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(-0.0, Double.MIN_NORMAL, "test"));
	}

	@Test
	public void testValidateMinWithBound() {
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE, inclusive);
		ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE, inclusive);
		ValidationUtil.validateMin(1.0, 0.999, exclusive);
		ValidationUtil.validateMin(-0.999, -1.0, exclusive);
		ValidationUtil.validateMin(1.0, 1.0, inclusive);
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE, exclusive));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE, exclusive));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(1.0, 1.0, exclusive));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(-1.0, -1.0, exclusive));
	}
	
	@Test
	public void testvalidateMaxong() {
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMax(-1, 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(0, -1, "test"));
	}

	@Test
	public void testValidateMaxDouble() {
		ValidationUtil.validateMax(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMax(Double.MIN_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMax(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMax(0, Double.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Double.MAX_VALUE, Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Double.MIN_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Double.MIN_NORMAL, -0.0, "test"));
	}

	@Test
	public void testValidateMaxWithBound() {
		ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE, inclusive);
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE, inclusive);
		ValidationUtil.validateMax(0.999, 1.0, exclusive);
		ValidationUtil.validateMax(-1.0, -0.999, exclusive);
		ValidationUtil.validateMax(1.0, 1.0, inclusive);
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE, exclusive));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE, exclusive));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(1.0, 1.0, exclusive));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(-1.0, -1.0, exclusive));
	}
	
	@Test
	public void testValidateRange() {
		ValidationUtil.validateRange(0, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateRange(0, 0, 0);
		ValidationUtil.validateRange(0, -1, 1, "test");
		ValidationUtil.validateRange(Double.MIN_VALUE, 0, Double.MAX_VALUE);
		ValidationUtil.validateRange(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateRange(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateRange(-0.0, 0.0, 0.0);
		ValidationUtil.validateRange(0.0, -1.0, 1.0, "test");
		TestUtil
			.assertThrown(() -> ValidationUtil.validateRange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRange(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(-1, 0, 1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(1, -1, 0, "test"));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRange(0, Double.MIN_VALUE, Double.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(Double.MIN_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(-1.0, 0.0, 1.0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(1.0, -1.0, 0.0, "test"));
	}

	@Test
	public void testValidateMinUnsigned() {
		ValidationUtil.validateUmin(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateUmin(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateUmin(Long.MIN_VALUE, Long.MAX_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateUmin(Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateUmin(0, Long.MIN_VALUE, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUmin(0, -1, "test"));
	}

	@Test
	public void testValidateMaxUnsigned() {
		ValidationUtil.validateUmax(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateUmax(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateUmax(Long.MAX_VALUE, Long.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateUmax(Long.MIN_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateUmax(Long.MIN_VALUE, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUmax(-1, 0, "test"));
	}

	@Test
	public void testValidateRangeUnsigned() {
		ValidationUtil.validateUrange(Long.MAX_VALUE, 0, Long.MAX_VALUE);
		ValidationUtil.validateUrange(Long.MIN_VALUE, 0, Long.MIN_VALUE);
		ValidationUtil.validateUrange(Long.MAX_VALUE, 0, Long.MIN_VALUE, "test");
		TestUtil
			.assertThrown(() -> ValidationUtil.validateUrange(Long.MIN_VALUE, 0, Long.MAX_VALUE));
		TestUtil
			.assertThrown(() -> ValidationUtil.validateUrange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateUrange(0, Long.MIN_VALUE, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUrange(-1, 0, -2, "test"));
	}

}
