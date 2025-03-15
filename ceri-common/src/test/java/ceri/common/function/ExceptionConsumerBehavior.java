package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Captor;

public class ExceptionConsumerBehavior {

	@Test
	public void shouldCombineConsumers() throws IOException {
		Captor.OfInt capturer = Captor.ofInt();
		ExceptionConsumer<IOException, Integer> consumer =
			FunctionTestUtil.consumer().andThen(capturer::accept);
		consumer.accept(-1);
		assertThrown(() -> consumer.accept(0));
		assertThrown(() -> consumer.accept(1));
		capturer.verifyInt(-1);
	}

	@Test
	public void shouldConvertToConsumer() {
		Consumer<Integer> c = consumer().asConsumer();
		c.accept(2);
		assertRte(() -> c.accept(1));
		assertRte(() -> c.accept(0));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionConsumer<RuntimeException, Integer> c = ExceptionConsumer.of(Std.consumer());
		c.accept(1);
		assertRte(() -> c.accept(0));
	}

}
