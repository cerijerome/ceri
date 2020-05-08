package ceri.common.validation;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
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
	public void testValidate() {
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
	public void testValidateNull() {
		ValidationUtil.validateNull(null);
		TestUtil.assertThrown(() -> ValidationUtil.validateNull(""));
	}

	@Test
	public void testValidateNotNull() {
		ValidationUtil.validateNotNull(OBJ);
		ValidationUtil.validateNotNull(OBJ, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateNotNull(null));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotNull(null, "test"));
	}

	@Test
	public void testValidateObjectEquality() {
		ValidationUtil.validateEqual(OBJ, OBJ);
		ValidationUtil.validateEqual(null, null);
		ValidationUtil.validateEqualL(I0, I1);
		ValidationUtil.validateNotEqual(null, OBJ);
		ValidationUtil.validateNotEqual(OBJ, null);
		ValidationUtil.validateNotEqual(I0, OBJ);
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(null, OBJ));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(OBJ, null));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(OBJ, I0));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotEqual(null, null));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotEqual(OBJ, OBJ));
	}

	@Test
	public void testValidateNumberEquality() {
		ValidationUtil.validateEqualL(Long.MIN_VALUE, Long.MIN_VALUE, DisplayLong.hex);
		ValidationUtil.validateNotEqualL(Long.MIN_VALUE, Long.MAX_VALUE, DisplayLong.udec);
		ValidationUtil.validateEqualD(Double.MIN_VALUE, Double.MIN_VALUE, DisplayDouble.round1);
		ValidationUtil.validateNotEqualD(Double.MIN_VALUE, Double.MAX_VALUE, DisplayDouble.round);
		TestUtil.assertThrown(
			() -> ValidationUtil.validateEqualL(Long.MIN_VALUE, Long.MAX_VALUE, DisplayLong.hex16));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotEqualL(Long.MIN_VALUE, Long.MIN_VALUE,
			DisplayLong.hex4));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqualD(Double.MAX_VALUE, Double.MIN_VALUE,
			DisplayDouble.std));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateNotEqualD(1.00001, 1.00001, DisplayDouble.round1));
	}

	@Test
	public void testValidateNumberEqualityFormat() {
		assertThat(
			TestUtil.thrown(() -> ValidationUtil.validateEqualL(-1, 0xff, (String) null,
				DisplayLong.dec, DisplayLong.hex4)).getMessage(),
			is("Value != (-1, 0xffff): (255, 0x00ff)"));
		assertThat(TestUtil
			.thrown(() -> ValidationUtil.validateEqualD(1, 1.111, "Num", DisplayDouble.round1))
			.getMessage(), is("Num != 1.0: 1.1"));
	}

	@Test
	public void testValidateEqualUnsigned() {
		ValidationUtil.validateUbyte(0xff, -1);
		ValidationUtil.validateUbyte(0xffff, -1);
		ValidationUtil.validateUshort(0xffff, -1);
		ValidationUtil.validateUshort(0xffffff, -1);
		ValidationUtil.validateUint(0xffffffff, -1);
		ValidationUtil.validateUint(0xffffffffffL, -1);
		ValidationUtil.validateUlong(0xffffffff12345678L, 0xffffffff12345678L);
		ValidationUtil.validateUlong(0xffffffffffffffffL, -1L);
		ValidationUtil.validateUlong(Long.MIN_VALUE, Long.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateUbyte(0xf, -1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUshort(0xff, -1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUint(0xffff, -1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUlong(-1L, -2L, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateUlong( //
			0xffffffff12345678L, 0xffffffff12345670L));
	}

	@Test
	public void testValidateUnsignedLimits() {
		ValidationUtil.validateUbyte(0);
		ValidationUtil.validateUbyte(0xff);
		assertThrown(() -> ValidationUtil.validateUbyte(-1));
		assertThrown(() -> ValidationUtil.validateUbyte(0x100));
		ValidationUtil.validateUshort(0);
		ValidationUtil.validateUshort(0xffff);
		assertThrown(() -> ValidationUtil.validateUshort(-1));
		assertThrown(() -> ValidationUtil.validateUshort(0x10000));
		ValidationUtil.validateUint(0);
		ValidationUtil.validateUint(0xffffffffL);
		assertThrown(() -> ValidationUtil.validateUint(-1));
		assertThrown(() -> ValidationUtil.validateUint(0x100000000L));
	}

	@Test
	public void testValidateUnsignedLimitFormat() {
		assertThat(TestUtil.thrown(() -> ValidationUtil.validateUbyte(-1, "Byte", DisplayLong.dec))
			.getMessage(), is("Byte is not within [0, 255]: -1"));

	}

	@Test
	public void testValidateWithin() {
		ValidationUtil.validateWithin(1, Interval.inclusive(0, 1));
		assertThrown(() -> ValidationUtil.validateWithin(1, Interval.exclusive(0, 1)));
		ValidationUtil.validateWithinL(1, Interval.inclusive(0L, 1L), DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateWithinL(1, Interval.exclusive(0L, 1L), DisplayLong.hex));
		ValidationUtil.validateWithinD(1, Interval.inclusive(0.0, 1.0), DisplayDouble.round);
		assertThrown(() -> ValidationUtil.validateWithinD(1, Interval.exclusive(0.0, 1.0),
			DisplayDouble.round));
	}

	@Test
	public void testValidateWithout() {
		ValidationUtil.validateWithout(1, Interval.exclusive(0, 1));
		assertThrown(() -> ValidationUtil.validateWithout(1, Interval.inclusive(0, 1)));
		ValidationUtil.validateWithoutL(1, Interval.exclusive(0L, 1L), DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateWithoutL(1, Interval.inclusive(0L, 1L), DisplayLong.hex));
		ValidationUtil.validateWithoutD(1, Interval.exclusive(0.0, 1.0), DisplayDouble.round);
		assertThrown(() -> ValidationUtil.validateWithoutD(1, Interval.inclusive(0.0, 1.0),
			DisplayDouble.round));
	}

	@Test
	public void testValidateMin() {
		ValidationUtil.validateMinL(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMinL(Long.MAX_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMinL(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMinL(Long.MAX_VALUE, 0, "test");
		ValidationUtil.validateMinD(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMinD(Double.MAX_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMinD(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMinD(Double.MIN_VALUE, 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMinL(Long.MIN_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinL(Long.MIN_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinL(-1, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinD(Double.MIN_VALUE, Double.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinD(0, Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinD(-0.0, Double.MIN_NORMAL, "test"));
	}

	@Test
	public void testValidateMax() {
		ValidationUtil.validateMaxL(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMaxL(Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMaxL(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMaxL(-1, 0, "test");
		ValidationUtil.validateMaxD(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMaxD(Double.MIN_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMaxD(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMaxD(0, Double.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxL(Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxL(Long.MAX_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxL(0, -1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxD(Double.MAX_VALUE, Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxD(Double.MIN_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxD(Double.MIN_NORMAL, -0.0, "test"));
	}

	@Test
	public void testValidateRange() {
		ValidationUtil.validateRangeL(0, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRangeL(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRangeL(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRangeL(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRangeL(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateRangeL(0, 0, 0);
		ValidationUtil.validateRangeL(0, -1, 1, "test");
		ValidationUtil.validateRangeD(Double.MIN_VALUE, 0, Double.MAX_VALUE);
		ValidationUtil.validateRangeD(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateRangeD(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateRangeD(-0.0, 0.0, 0.0);
		ValidationUtil.validateRangeD(0.0, -1.0, 1.0, "test");
		TestUtil
			.assertThrown(() -> ValidationUtil.validateRangeL(0, Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRangeL(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRangeL(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeL(-1, 0, 1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeL(1, -1, 0, "test"));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRangeD(0, Double.MIN_VALUE, Double.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeD(Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeD(Double.MIN_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeD(-1.0, 0.0, 1.0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeD(1.0, -1.0, 0.0, "test"));
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
