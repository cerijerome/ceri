package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.booleanSupplier;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBooleanSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertTrue(booleanSupplier(true).asBooleanSupplier().getAsBoolean());
		assertThrown(RuntimeException.class,
			() -> booleanSupplier(null).asBooleanSupplier().getAsBoolean());
		assertThrown(RuntimeException.class,
			() -> booleanSupplier(false).asBooleanSupplier().getAsBoolean());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertTrue(ExceptionBooleanSupplier.of(Std.booleanSupplier(true)).getAsBoolean());
		assertThrown(RuntimeException.class,
			() -> ExceptionBooleanSupplier.of(Std.booleanSupplier(false)).getAsBoolean());
	}

}
