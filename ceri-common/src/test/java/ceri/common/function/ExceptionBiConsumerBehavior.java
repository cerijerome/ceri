package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.junit.Test;

public class ExceptionBiConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		ExceptionBiConsumer<IOException, String, Integer> consumer = (s, i) -> {
			if (s.length() == i) throw new IOException();
		};
		BiConsumer<String, Integer> c = consumer.asBiConsumer();
		c.accept("A", 0);
		assertException(() -> c.accept(null, 0));
		assertException(() -> c.accept("A", 1));
	}

	@Test
	public void shouldConvertFromConsumer() {
		BiConsumer<String, Integer> consumer = (s, i) -> Objects.requireNonNull(s);
		ExceptionBiConsumer<RuntimeException, String, Integer> c = ExceptionBiConsumer.of(consumer);
		c.accept("A", 1);
		assertException(() -> c.accept(null, 1));
	}

}
