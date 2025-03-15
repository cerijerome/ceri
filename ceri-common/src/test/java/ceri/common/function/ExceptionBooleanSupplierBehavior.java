package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.booleanSupplier;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBooleanSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertTrue(booleanSupplier(true).asBooleanSupplier().getAsBoolean());
		assertRte(() -> booleanSupplier(null).asBooleanSupplier().getAsBoolean());
		assertRte(() -> booleanSupplier(false).asBooleanSupplier().getAsBoolean());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertTrue(ExceptionBooleanSupplier.of(Std.booleanSupplier(true)).getAsBoolean());
		assertRte(() -> ExceptionBooleanSupplier.of(Std.booleanSupplier(false)).getAsBoolean());
	}

}
