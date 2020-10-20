package ceri.common.function;

import static ceri.common.test.AssertUtil.assertArray;
import java.util.function.IntConsumer;
import org.junit.Test;
import ceri.common.test.Captor;

public class BooleanConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		boolean[] bb = new boolean[] { false, false };
		BooleanConsumer c = b -> bb[0] = b;
		c = c.andThen(b -> bb[1] = b);
		c.accept(true);
		assertArray(bb, true, true);
	}

	@Test
	public void shouldConvertToInt() {
		Captor<Boolean> captor = Captor.of();
		IntConsumer consumer = BooleanConsumer.toInt(b -> captor.accept(b));
		consumer.accept(0);
		consumer.accept(1);
		consumer.accept(-1);
		captor.verify(false, true, true);
	}

}
