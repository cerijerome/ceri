package ceri.common.function;

import static ceri.common.test.TestUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.Capturer;

public class ObjByteConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Capturer.Bi<String, Byte> capturer = Capturer.ofBi();
		ObjByteConsumer<String> consumer0 = (s, b) -> capturer.accept(s, null);
		ObjByteConsumer<String> consumer1 = (s, b) -> capturer.accept(null, b);
		var consumer = consumer0.andThen(consumer1);
		consumer.accept("a", Byte.MAX_VALUE);
		capturer.first.verify("a", null);
		capturer.second.verify(null, Byte.MAX_VALUE);
	}

	@Test
	public void shouldConvertToIntConsumer() {
		Capturer.Bi<String, Byte> capturer = Capturer.ofBi();
		var consumer = ObjByteConsumer.toInt(capturer::accept);
		consumer.accept("a", Byte.MAX_VALUE);
		consumer.accept("b", Byte.MIN_VALUE);
		consumer.accept("c", 0xff);
		capturer.first.verify("a", "b", "c");
		capturer.second.verify(Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) -1);
	}

	@Test
	public void shouldConvertToIntExactConsumer() {
		Capturer.Bi<String, Byte> capturer = Capturer.ofBi();
		var consumer = ObjByteConsumer.toIntExact(capturer::accept);
		consumer.accept("a", Byte.MAX_VALUE);
		consumer.accept("b", Byte.MIN_VALUE);
		assertThrown(() -> consumer.accept("c", 0xff));
		capturer.first.verify("a", "b");
		capturer.second.verify(Byte.MAX_VALUE, Byte.MIN_VALUE);
	}

	@Test
	public void shouldConvertToUintExactConsumer() {
		Capturer.Bi<String, Byte> capturer = Capturer.ofBi();
		var consumer = ObjByteConsumer.toUintExact(capturer::accept);
		consumer.accept("a", Byte.MAX_VALUE);
		assertThrown(() -> consumer.accept("b", Byte.MIN_VALUE));
		consumer.accept("c", 0xff);
		capturer.first.verify("a", "c");
		capturer.second.verify(Byte.MAX_VALUE, (byte) -1);
	}

}
