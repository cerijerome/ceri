package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.booleanSupplier;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionBooleanSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertThat(booleanSupplier(true).asBooleanSupplier().getAsBoolean(), is(true));
		assertThrown(RuntimeException.class,
			() -> booleanSupplier(null).asBooleanSupplier().getAsBoolean());
		assertThrown(RuntimeException.class,
			() -> booleanSupplier(false).asBooleanSupplier().getAsBoolean());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertThat(ExceptionBooleanSupplier.of(Std.booleanSupplier(true)).getAsBoolean(), is(true));
		assertThrown(RuntimeException.class,
			() -> ExceptionBooleanSupplier.of(Std.booleanSupplier(false)).getAsBoolean());
	}

}
