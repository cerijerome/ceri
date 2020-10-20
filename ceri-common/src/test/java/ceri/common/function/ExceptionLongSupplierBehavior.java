package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertEquals(longSupplier(2).asLongSupplier().getAsLong(), 2L);
		assertThrown(RuntimeException.class, () -> longSupplier(1).asLongSupplier().getAsLong());
		assertThrown(RuntimeException.class, () -> longSupplier(0).asLongSupplier().getAsLong());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertEquals(ExceptionLongSupplier.of(Std.longSupplier(1)).getAsLong(), 1L);
		assertThrown(RuntimeException.class,
			() -> ExceptionLongSupplier.of(Std.longSupplier(0)).getAsLong());
	}

}
