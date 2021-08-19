package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.doubleSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionDoubleSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertEquals(doubleSupplier(2.0).asDoubleSupplier().getAsDouble(), 2.0);
		assertThrown(RuntimeException.class,
			() -> doubleSupplier(1).asDoubleSupplier().getAsDouble());
		assertThrown(RuntimeException.class,
			() -> doubleSupplier(0).asDoubleSupplier().getAsDouble());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertEquals(ExceptionDoubleSupplier.of(Std.doubleSupplier(1)).getAsDouble(), 1.0);
		assertThrown(RuntimeException.class,
			() -> ExceptionDoubleSupplier.of(Std.doubleSupplier(0)).getAsDouble());
	}

}
