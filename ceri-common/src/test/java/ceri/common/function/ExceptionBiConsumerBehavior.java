package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.test.TestUtil.assertThrown;
import java.io.IOException;
import java.util.function.BiConsumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Capturer;

public class ExceptionBiConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() throws IOException {
		Capturer.Int capturer = Capturer.ofInt();
		var consumer = FunctionTestUtil.biConsumer().andThen((i, j) -> {
			capturer.accept(i);
			capturer.accept(j);
		});
		consumer.accept(2, -2);
		assertThrown(() -> consumer.accept(2, 0));
		assertThrown(() -> consumer.accept(1, 1));
		capturer.verifyInt(2, -2);
	}

	@Test
	public void shouldConvertToConsumer() {
		BiConsumer<Integer, Integer> c = biConsumer().asBiConsumer();
		c.accept(2, 3);
		assertThrown(RuntimeException.class, () -> c.accept(1, 2));
		assertThrown(RuntimeException.class, () -> c.accept(0, 2));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionBiConsumer<RuntimeException, Integer, Integer> c =
			ExceptionBiConsumer.of(Std.biConsumer());
		c.accept(1, 2);
		assertThrown(RuntimeException.class, () -> c.accept(0, 2));
	}

}
