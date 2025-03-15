package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertRte(() -> supplier(0).asSupplier().get());
		assertRte(() -> supplier(1).asSupplier().get());
		assertEquals(supplier(2).asSupplier().get(), 2);
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertRte(() -> ExceptionSupplier.of(Std.supplier(0)).get());
		assertEquals(ExceptionSupplier.of(Std.supplier(1)).get(), 1);
	}

	@Test
	public void shouldConvertToCallable() throws Exception {
		assertRte(() -> supplier(0).asCallable().call());
		assertIoe(() -> supplier(1).asCallable().call());
		assertEquals(supplier(2).asCallable().call(), 2);
	}

	@Test
	public void shouldConvertFromCallable() throws Exception {
		assertRte(() -> ExceptionSupplier.of(Std.callable(0)).get());
		assertIoe(() -> ExceptionSupplier.of(Std.callable(1)).get());
		assertEquals(ExceptionSupplier.of(Std.callable(2)).get(), 2);
	}

}
