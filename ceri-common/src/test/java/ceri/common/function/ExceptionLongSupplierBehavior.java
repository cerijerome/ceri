package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longSupplier;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertThat(longSupplier(2).asLongSupplier().getAsLong(), is(2L));
		assertThrown(RuntimeException.class, () -> longSupplier(1).asLongSupplier().getAsLong());
		assertThrown(RuntimeException.class, () -> longSupplier(0).asLongSupplier().getAsLong());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertThat(ExceptionLongSupplier.of(Std.longSupplier(1)).getAsLong(), is(1L));
		assertThrown(RuntimeException.class,
			() -> ExceptionLongSupplier.of(Std.longSupplier(0)).getAsLong());
	}

}
