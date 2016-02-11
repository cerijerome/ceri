package ceri.common.validation;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import org.junit.Test;

public class ValidationUtilTest {
	private static final Object OBJ = new Object();
	private static final Integer I0 = 999;
	private static final Integer I1 = 999;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ValidationUtil.class);
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
		assertException(() -> ValidationUtil.validateRange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		assertException(() -> ValidationUtil.validateRange(Long.MAX_VALUE, Long.MIN_VALUE,
			Long.MIN_VALUE));
		assertException(() -> ValidationUtil.validateRange(Long.MIN_VALUE, Long.MAX_VALUE,
			Long.MAX_VALUE));
		assertException(() -> ValidationUtil.validateRange(-1, 0, 1, "test"));
		assertException(() -> ValidationUtil.validateRange(1, -1, 0, "test"));
		assertException(() -> ValidationUtil.validateRange(0, Double.MIN_VALUE, Double.MAX_VALUE));
		assertException(() -> ValidationUtil.validateRange(Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		assertException(() -> ValidationUtil.validateRange(Double.MIN_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		assertException(() -> ValidationUtil.validateRange(-1.0, 0.0, 1.0, "test"));
		assertException(() -> ValidationUtil.validateRange(1.0, -1.0, 0.0, "test"));
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
		assertException(() -> ValidationUtil.validateMax(Long.MAX_VALUE, Long.MIN_VALUE));
		assertException(() -> ValidationUtil.validateMax(Long.MAX_VALUE, 0));
		assertException(() -> ValidationUtil.validateMax(0, -1, "test"));
		assertException(() -> ValidationUtil.validateMax(Double.MAX_VALUE, Double.MIN_VALUE));
		assertException(() -> ValidationUtil.validateMax(Double.MIN_VALUE, 0));
		assertException(() -> ValidationUtil.validateMax(Double.MIN_NORMAL, -0.0, "test"));
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
		assertException(() -> ValidationUtil.validateMin(Long.MIN_VALUE, 0));
		assertException(() -> ValidationUtil.validateMin(Long.MIN_VALUE, Long.MAX_VALUE));
		assertException(() -> ValidationUtil.validateMin(-1, 0, "test"));
		assertException(() -> ValidationUtil.validateMin(Double.MIN_VALUE, Double.MAX_VALUE));
		assertException(() -> ValidationUtil.validateMin(0, Double.MIN_VALUE));
		assertException(() -> ValidationUtil.validateMin(-0.0, Double.MIN_NORMAL, "test"));
	}

	@Test
	public void testValidateNotNull() {
		ValidationUtil.validateNotNull(OBJ);
		ValidationUtil.validateNotNull(OBJ, "test");
		assertException(() -> ValidationUtil.validateNotNull(null));
		assertException(() -> ValidationUtil.validateNotNull(null, "test"));
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
		assertException(() -> ValidationUtil.validateEqual(Long.MAX_VALUE, 0));
		assertException(() -> ValidationUtil.validateEqual(Long.MAX_VALUE, Long.MIN_VALUE));
		assertException(() -> ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MAX_VALUE));
		assertException(() -> ValidationUtil.validateEqual(Long.MIN_VALUE, 0, "test"));
		assertException(() -> ValidationUtil.validateEqual(Double.MAX_VALUE, 0));
		assertException(() -> ValidationUtil.validateEqual(Double.MIN_VALUE, 0, "test"));
		assertException(() -> ValidationUtil.validateEqual(null, OBJ));
		assertException(() -> ValidationUtil.validateEqual(OBJ, null));
		assertException(() -> ValidationUtil.validateEqual(OBJ, I0));
	}

}
