package ceri.common.function;

import static ceri.common.test.TestUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.Capturer;

public class ByteConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Capturer<Byte> capturer = Capturer.of();
		ByteConsumer consumer0 = b -> capturer.accept((byte) 0);
		ByteConsumer consumer1 = b -> capturer.accept(b);
		var consumer = consumer0.andThen(consumer1);
		consumer.accept(Byte.MAX_VALUE);
		capturer.verify((byte) 0, Byte.MAX_VALUE);
	}

	@Test
	public void shouldConvertToIntConsumer() {
		Capturer<Byte> capturer = Capturer.of();
		var consumer = ByteConsumer.toInt(capturer::accept);
		consumer.accept(Byte.MAX_VALUE);
		consumer.accept(Byte.MIN_VALUE);
		consumer.accept(0xff);
		capturer.verify(Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) -1);
	}

	@Test
	public void shouldConvertToIntExactConsumer() {
		Capturer<Byte> capturer = Capturer.of();
		var consumer = ByteConsumer.toIntExact(capturer::accept);
		consumer.accept(Byte.MAX_VALUE);
		consumer.accept(Byte.MIN_VALUE);
		assertThrown(() -> consumer.accept(0xff));
		capturer.verify(Byte.MAX_VALUE, Byte.MIN_VALUE);
	}

	@Test
	public void shouldConvertToUintExactConsumer() {
		Capturer<Byte> capturer = Capturer.of();
		var consumer = ByteConsumer.toUintExact(capturer::accept);
		consumer.accept(Byte.MAX_VALUE);
		assertThrown(() -> consumer.accept(Byte.MIN_VALUE));
		consumer.accept(0xff);
		capturer.verify(Byte.MAX_VALUE, (byte) -1);
	}

}
