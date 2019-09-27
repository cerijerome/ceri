package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertThat(supplier(2).asSupplier().get(), is(2));
		assertThrown(RuntimeException.class, () -> supplier(1).asSupplier().get());
		assertThrown(RuntimeException.class, () -> supplier(0).asSupplier().get());
	}

	@Test
	public void shouldConvertFromSupplier() {
		ExceptionSupplier.of(Std.supplier(1)).get();
		assertThat(ExceptionSupplier.of(Std.supplier(1)).get(), is(1));
		assertThrown(RuntimeException.class, () -> ExceptionSupplier.of(Std.supplier(0)).get());
	}

}
