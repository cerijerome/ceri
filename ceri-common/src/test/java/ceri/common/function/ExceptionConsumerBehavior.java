package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.test.TestUtil.assertThrown;
import java.util.function.Consumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		Consumer<Integer> c = consumer().asConsumer();
		c.accept(2);
		assertThrown(RuntimeException.class, () -> c.accept(1));
		assertThrown(RuntimeException.class, () -> c.accept(0));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionConsumer<RuntimeException, Integer> c = ExceptionConsumer.of(Std.consumer());
		c.accept(1);
		assertThrown(RuntimeException.class, () -> c.accept(0));
	}

}
