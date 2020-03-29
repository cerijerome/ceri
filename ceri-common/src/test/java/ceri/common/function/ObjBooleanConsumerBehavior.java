package ceri.common.function;

import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;
import ceri.common.test.Capturer;

public class ObjBooleanConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Capturer.Bi<Integer, Boolean> capturer = Capturer.ofBi();
		ObjBooleanConsumer<Integer> consumer0 = (i, b) -> capturer.accept(i, null);
		ObjBooleanConsumer<Integer> consumer1 = (i, b) -> capturer.accept(null, b);
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
