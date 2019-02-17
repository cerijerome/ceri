package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.function.Supplier;
import org.junit.Test;

public class ExceptionSupplierBehavior {

	@Test
	public void shouldConvertToSupplier() {
		int[] i = { 1 };
		ExceptionSupplier<IOException, Integer> supplier = () -> {
			if (i[0] < 0) throw new IOException();
			if (i[0] == 0) throw new RuntimeException();
			return i[0];
		};
		Supplier<Integer> s = supplier.asSupplier();
		assertThat(s.get(), is(1));
		i[0] = 0;
		assertException(s::get);
		i[0] = -1;
		assertException(s::get);
	}

	@Test
	public void shouldConvertFromSupplier() {
		int[] i = { 1 };
		Supplier<Integer> supplier = () -> {
			if (i[0] == 0) throw new RuntimeException();
			return i[0];
		};
		ExceptionSupplier<RuntimeException, Integer> s = ExceptionSupplier.of(supplier);
		assertThat(s.get(), is(1));
		i[0] = 0;
		assertException(s::get);
	}

}
