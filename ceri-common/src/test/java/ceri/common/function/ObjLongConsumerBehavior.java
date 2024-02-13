package ceri.common.function;

import org.junit.Test;
import ceri.common.test.Captor;

public class ObjLongConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Captor.Bi<String, Long> capturer = Captor.ofBi();
		ObjLongConsumer<String> consumer0 = (st, l) -> capturer.accept(st, null);
		ObjLongConsumer<String> consumer1 = (st, l) -> capturer.accept(null, l);
		var consumer = consumer0.andThen(consumer1);
		consumer.accept("a", Long.MAX_VALUE);
		capturer.first.verify("a", null);
		capturer.second.verify(null, Long.MAX_VALUE);
	}
}
