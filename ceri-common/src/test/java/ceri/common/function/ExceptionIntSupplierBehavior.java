package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intSupplier;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertThat(intSupplier(2).asIntSupplier().getAsInt(), is(2));
		assertThrown(RuntimeException.class, () -> intSupplier(1).asIntSupplier().getAsInt());
		assertThrown(RuntimeException.class, () -> intSupplier(0).asIntSupplier().getAsInt());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertThat(ExceptionIntSupplier.of(Std.intSupplier(1)).getAsInt(), is(1));
		assertThrown(RuntimeException.class,
			() -> ExceptionIntSupplier.of(Std.intSupplier(0)).getAsInt());
	}

}
