package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertThrown(RuntimeException.class, () -> supplier(0).asSupplier().get());
		assertThrown(RuntimeException.class, () -> supplier(1).asSupplier().get());
		assertEquals(supplier(2).asSupplier().get(), 2);
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertThrown(RuntimeException.class, () -> ExceptionSupplier.of(Std.supplier(0)).get());
		assertEquals(ExceptionSupplier.of(Std.supplier(1)).get(), 1);
	}

	@Test
	public void shouldConvertToCallable() throws Exception {
		assertThrown(RuntimeException.class, () -> supplier(0).asCallable().call());
		assertThrown(IOException.class, () -> supplier(1).asCallable().call());
		assertEquals(supplier(2).asCallable().call(), 2);
	}

	@Test
	public void shouldConvertFromCallable() throws Exception {
		assertThrown(RuntimeException.class, () -> ExceptionSupplier.of(Std.callable(0)).get());
		assertThrown(IOException.class, () -> ExceptionSupplier.of(Std.callable(1)).get());
		assertEquals(ExceptionSupplier.of(Std.callable(2)).get(), 2);
	}

}
