package ceri.common.function;

import static ceri.common.test.AssertUtil.assertArray;
import org.junit.Test;
import ceri.common.test.Captor;

public class ObjBooleanConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Captor.Bi<Integer, Boolean> capturer = Captor.ofBi();
		ObjBooleanConsumer<Integer> consumer0 = (i, _) -> capturer.accept(i, null);
		ObjBooleanConsumer<Integer> consumer1 = (_, b) -> capturer.accept(null, b);
		ObjBooleanConsumer<Integer> consumer = consumer0.andThen(consumer1);
		consumer.accept(1, true);
		capturer.first.verify(1, null);
		capturer.second.verify(null, true);
	}

	@Test
	public void shouldConvertToIntConsumer() {
		String[] ss = new String[1];
		ObjBooleanConsumer<String[]> c = (s, b) -> s[0] = String.valueOf(b);
		c.accept(ss, false);
		assertArray(ss, "false");
		ObjBooleanConsumer.toInt(c).accept(ss, -1);
		assertArray(ss, "true");
		ObjBooleanConsumer.toInt(c).accept(ss, 0);
		assertArray(ss, "false");
	}

}
