package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.function.BiFunction;
import org.junit.Test;

public class ExceptionBiFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ExceptionBiFunction<IOException, String, Integer, Integer> function = (s, i) -> {
			if (s.length() == i) throw new IOException();
			return s.length();
		};
		BiFunction<String, Integer, Integer> f = function.asBiFunction();
		assertThat(f.apply("A", 0), is(1));
		assertException(() -> f.apply(null, 0));
		assertException(() -> f.apply("A", 1));
	}

	@Test
	public void shouldConvertFromFunction() {
		BiFunction<String, Integer, Integer> function = (s, i) -> s.length() - i;
		ExceptionBiFunction<RuntimeException, String, Integer, Integer> f =
			ExceptionBiFunction.of(function);
		assertThat(f.apply("A", 1), is(0));
		assertException(() -> f.apply(null, 0));
	}

}
