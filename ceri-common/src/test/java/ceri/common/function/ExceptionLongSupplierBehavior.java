package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertEquals(longSupplier(2).asLongSupplier().getAsLong(), 2L);
		assertRte(() -> longSupplier(1).asLongSupplier().getAsLong());
		assertRte(() -> longSupplier(0).asLongSupplier().getAsLong());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertEquals(ExceptionLongSupplier.of(Std.longSupplier(1)).getAsLong(), 1L);
		assertRte(() -> ExceptionLongSupplier.of(Std.longSupplier(0)).getAsLong());
	}

}
