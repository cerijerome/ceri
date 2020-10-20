package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.objIntConsumer;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.ObjIntConsumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Captor;

public class ExceptionObjIntConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() throws IOException {
		Captor.Int capturer = Captor.ofInt();
		var consumer = FunctionTestUtil.objIntConsumer().andThen((i, j) -> {
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
		ObjIntConsumer<Integer> c = objIntConsumer().asObjIntConsumer();
		c.accept(2, 3);
		assertThrown(RuntimeException.class, () -> c.accept(1, 2));
		assertThrown(RuntimeException.class, () -> c.accept(0, 2));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionObjIntConsumer<RuntimeException, Integer> c =
			ExceptionObjIntConsumer.of(Std.objIntConsumer());
		c.accept(1, 2);
		assertThrown(RuntimeException.class, () -> c.accept(0, 2));
	}

}
