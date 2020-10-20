package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertEquals(intSupplier(2).asIntSupplier().getAsInt(), 2);
		assertThrown(RuntimeException.class, () -> intSupplier(1).asIntSupplier().getAsInt());
		assertThrown(RuntimeException.class, () -> intSupplier(0).asIntSupplier().getAsInt());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertEquals(ExceptionIntSupplier.of(Std.intSupplier(1)).getAsInt(), 1);
		assertThrown(RuntimeException.class,
			() -> ExceptionIntSupplier.of(Std.intSupplier(0)).getAsInt());
	}

}
