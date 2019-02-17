package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import org.junit.Test;

public class ExceptionConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		ExceptionConsumer<IOException, String> consumer = s -> {
			Objects.requireNonNull(s);
			if (s.isEmpty()) throw new IOException();
		};
		Consumer<String> c = consumer.asConsumer();
		c.accept("A");
		assertException(() -> c.accept(null));
		assertException(() -> c.accept(""));
	}

	@Test
	public void shouldConvertFromConsumer() {
		Consumer<String> consumer = Objects::requireNonNull;
		ExceptionConsumer<RuntimeException, String> c = ExceptionConsumer.of(consumer);
		c.accept("A");
		assertException(() -> c.accept(null));
	}

}
