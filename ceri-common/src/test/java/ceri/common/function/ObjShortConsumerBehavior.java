package ceri.common.function;

import static ceri.common.test.TestUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.Capturer;

public class ObjShortConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Capturer.Bi<String, Short> capturer = Capturer.ofBi();
		ObjShortConsumer<String> consumer0 = (st, sh) -> capturer.accept(st, null);
		ObjShortConsumer<String> consumer1 = (st, sh) -> capturer.accept(null, sh);
		var consumer = consumer0.andThen(consumer1);
		consumer.accept("a", Short.MAX_VALUE);
		capturer.first.verify("a", null);
		capturer.second.verify(null, Short.MAX_VALUE);
	}

	@Test
	public void shouldConvertToIntConsumer() {
		Capturer.Bi<String, Short> capturer = Capturer.ofBi();
		var consumer = ObjShortConsumer.toInt(capturer::accept);
		consumer.accept("a", Short.MAX_VALUE);
		consumer.accept("b", Short.MIN_VALUE);
		consumer.accept("c", 0xffff);
		capturer.first.verify("a", "b", "c");
		capturer.second.verify(Short.MAX_VALUE, Short.MIN_VALUE, (short) -1);
	}

	@Test
	public void shouldConvertToIntExactConsumer() {
		Capturer.Bi<String, Short> capturer = Capturer.ofBi();
		var consumer = ObjShortConsumer.toIntExact(capturer::accept);
		consumer.accept("a", Short.MAX_VALUE);
		consumer.accept("b", Short.MIN_VALUE);
		assertThrown(() -> consumer.accept("c", 0xffff));
		capturer.first.verify("a", "b");
		capturer.second.verify(Short.MAX_VALUE, Short.MIN_VALUE);
	}

	@Test
	public void shouldConvertToUintExactConsumer() {
		Capturer.Bi<String, Short> capturer = Capturer.ofBi();
		var consumer = ObjShortConsumer.toUintExact(capturer::accept);
		consumer.accept("a", Short.MAX_VALUE);
		assertThrown(() -> consumer.accept("b", Short.MIN_VALUE));
		consumer.accept("c", 0xffff);
		capturer.first.verify("a", "c");
		capturer.second.verify(Short.MAX_VALUE, (short) -1);
	}

}
