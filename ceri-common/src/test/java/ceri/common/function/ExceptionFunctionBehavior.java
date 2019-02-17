package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.function.Function;
import org.junit.Test;

public class ExceptionFunctionBehavior {

	@Test
	public void shouldConvertToFunction() {
		ExceptionFunction<IOException, String, Integer> function = s -> {
			if (s.isEmpty()) throw new IOException();
			return s.length();
		};
		Function<String, Integer> f = function.asFunction();
		assertThat(f.apply("A"), is(1));
		assertException(() -> f.apply(null));
		assertException(() -> f.apply(""));
	}

	@Test
	public void shouldConvertFromFunction() {
		Function<String, Integer> function = String::length;
		ExceptionFunction<RuntimeException, String, Integer> f = ExceptionFunction.of(function);
		assertThat(f.apply("A"), is(1));
		assertException(() -> f.apply(null));
	}

}
