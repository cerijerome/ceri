package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.doubleSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionDoubleSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertEquals(doubleSupplier(2.0).asDoubleSupplier().getAsDouble(), 2.0);
		assertRte(() -> doubleSupplier(1).asDoubleSupplier().getAsDouble());
		assertRte(() -> doubleSupplier(0).asDoubleSupplier().getAsDouble());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertEquals(ExceptionDoubleSupplier.of(Std.doubleSupplier(1)).getAsDouble(), 1.0);
		assertRte(() -> ExceptionDoubleSupplier.of(Std.doubleSupplier(0)).getAsDouble());
	}

}
