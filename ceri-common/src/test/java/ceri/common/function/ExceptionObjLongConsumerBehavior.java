package ceri.common.function;

import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.ObjLongConsumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Captor;

public class ExceptionObjLongConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() throws IOException {
		Captor.OfLong capturer = Captor.ofLong();
		var consumer = FunctionTestUtil.objLongConsumer().andThen((i, j) -> {
			capturer.accept(i);
			capturer.accept(j);
		});
		consumer.accept(2, -2);
		assertThrown(() -> consumer.accept(2, 0));
		assertThrown(() -> consumer.accept(1, 1));
		capturer.verifyLong(2, -2);
	}

	@Test
	public void shouldConvertToConsumer() {
		ObjLongConsumer<Integer> c = FunctionTestUtil.objLongConsumer().asObjLongConsumer();
		c.accept(2, 3);
		assertRte(() -> c.accept(1, 2));
		assertRte(() -> c.accept(0, 2));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionObjLongConsumer<RuntimeException, Integer> c =
			ExceptionObjLongConsumer.of(Std.objLongConsumer());
		c.accept(1, 2);
		assertRte(() -> c.accept(0, 2));
	}

}
