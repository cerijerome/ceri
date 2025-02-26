package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.byteSupplier;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionByteSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		assertEquals(byteSupplier((byte) 2).asByteSupplier().getAsByte(), (byte) 2);
		assertThrown(RuntimeException.class,
			() -> byteSupplier((byte) 1).asByteSupplier().getAsByte());
		assertThrown(RuntimeException.class,
			() -> byteSupplier((byte) 0).asByteSupplier().getAsByte());
	}

	@Test
	public void shouldConvertFromSupplier() {
		assertEquals(ExceptionByteSupplier.of(Std.byteSupplier((byte) 1)).getAsByte(), (byte) 1);
		assertThrown(RuntimeException.class,
			() -> ExceptionByteSupplier.of(Std.byteSupplier((byte) 0)).getAsByte());
	}
}
