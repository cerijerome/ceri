package ceri.common.validation;

import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import org.junit.Test;
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
		ValidationUtil.validate(1 > 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validate(1 < 0));
		TestUtil.assertThrown(() -> ValidationUtil.validate(1 < 0, "test"));
	}

	@Test
	public void testValidateWithFormattedException() {
		int i0 = -1;
		ValidationUtil.validate(i0 < 0, "%d >= 0", i0);
		int i1 = 1;
		TestUtil.assertThrown(() -> ValidationUtil.validate(i1 < 0, "%d >= 0", i1));
	}

	@Test
	public void testValidateNull() {
		ValidationUtil.validateNull(null);
		TestUtil.assertThrown(() -> ValidationUtil.validateNull(""));
	}

	@Test
	public void testValidateEqualUnsigned() {
		ValidationUtil.validateEqualUnsigned(0xffffffff12345678L, 0xffffffff12345678L);
		ValidationUtil.validateEqualUnsigned(0xffffffffffffffffL, -1L);
		ValidationUtil.validateEqualUnsigned(Long.MIN_VALUE, Long.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateEqualUnsigned(-1L, -2L, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqualUnsigned( //
			0xffffffff12345678L, 0xffffffff12345670L));
	}

	@Test
	public void testValidateMaxUnsigned() {
		ValidationUtil.validateMaxUnsigned(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMaxUnsigned(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMaxUnsigned(Long.MAX_VALUE, Long.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxUnsigned(Long.MIN_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxUnsigned(Long.MIN_VALUE, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateMaxUnsigned(-1, 0, "test"));
	}

	@Test
	public void testValidateMinUnsigned() {
		ValidationUtil.validateMinUnsigned(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMinUnsigned(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMinUnsigned(Long.MIN_VALUE, Long.MAX_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMinUnsigned(Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinUnsigned(0, Long.MIN_VALUE, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateMinUnsigned(0, -1, "test"));
	}

	@Test
	public void testValidateRangeUnsigned() {
		ValidationUtil.validateRangeUnsigned(Long.MAX_VALUE, 0, Long.MAX_VALUE);
		ValidationUtil.validateRangeUnsigned(Long.MIN_VALUE, 0, Long.MIN_VALUE);
		ValidationUtil.validateRangeUnsigned(Long.MAX_VALUE, 0, Long.MIN_VALUE, "test");
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRangeUnsigned(Long.MIN_VALUE, 0, Long.MAX_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRangeUnsigned(0, Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil
			.assertThrown(() -> ValidationUtil.validateRangeUnsigned(0, Long.MIN_VALUE, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRangeUnsigned(-1, 0, -2, "test"));
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
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(
			() -> ValidationUtil.validateRange(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(-1, 0, 1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(1, -1, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(0, Double.MIN_VALUE, Double.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(Double.MIN_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(-1.0, 0.0, 1.0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateRange(1.0, -1.0, 0.0, "test"));
	}

	@Test
	public void testValidateMax() {
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMax(-1, 0, "test");
		ValidationUtil.validateMax(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMax(Double.MIN_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMax(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMax(0, Double.MIN_VALUE, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(0, -1, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Double.MAX_VALUE, Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Double.MIN_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMax(Double.MIN_NORMAL, -0.0, "test"));
	}

	@Test
	public void testValidateMin() {
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMin(Long.MAX_VALUE, 0, "test");
		ValidationUtil.validateMin(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMin(Double.MAX_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMin(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMin(Double.MIN_VALUE, 0, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(-1, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(Double.MIN_VALUE, Double.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(0, Double.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateMin(-0.0, Double.MIN_NORMAL, "test"));
	}

	@Test
	public void testValidateNotNull() {
		ValidationUtil.validateNotNull(OBJ);
		ValidationUtil.validateNotNull(OBJ, "test");
		TestUtil.assertThrown(() -> ValidationUtil.validateNotNull(null));
		TestUtil.assertThrown(() -> ValidationUtil.validateNotNull(null, "test"));
	}

	@Test
	public void testValidateEqual() {
		ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateEqual(Long.MAX_VALUE, Long.MAX_VALUE, "test");
		ValidationUtil.validateEqual(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateEqual(Double.MAX_VALUE, Double.MAX_VALUE, "test");
		ValidationUtil.validateEqual(OBJ, OBJ);
		ValidationUtil.validateEqual(null, null);
		ValidationUtil.validateEqual(I0, I1);
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Long.MAX_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Long.MAX_VALUE, Long.MIN_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MAX_VALUE));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Long.MIN_VALUE, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Double.MAX_VALUE, 0));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(Double.MIN_VALUE, 0, "test"));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(null, OBJ));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(OBJ, null));
		TestUtil.assertThrown(() -> ValidationUtil.validateEqual(OBJ, I0));
	}

}
