package ceri.common.function;

import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.Captor;

public class ByteConsumerBehavior {

	@Test
	public void shouldCombineWithAndThen() {
		Captor<Byte> captor = Captor.of();
		ByteConsumer consumer0 = _ -> captor.accept((byte) 0);
		ByteConsumer consumer1 = b -> captor.accept(b);
		var consumer = consumer0.andThen(consumer1);
		consumer.accept(Byte.MAX_VALUE);
		captor.verify((byte) 0, Byte.MAX_VALUE);
	}

	@Test
	public void shouldConvertToIntConsumer() {
		Captor<Byte> captor = Captor.of();
		var consumer = ByteConsumer.toInt(captor::accept);
		consumer.accept(Byte.MAX_VALUE);
		consumer.accept(Byte.MIN_VALUE);
		consumer.accept(0xff);
		captor.verify(Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) -1);
	}

	@Test
	public void shouldConvertToIntExactConsumer() {
		Captor<Byte> captor = Captor.of();
		var consumer = ByteConsumer.toIntExact(captor::accept);
		consumer.accept(Byte.MAX_VALUE);
		consumer.accept(Byte.MIN_VALUE);
		assertThrown(() -> consumer.accept(0xff));
		captor.verify(Byte.MAX_VALUE, Byte.MIN_VALUE);
	}

	@Test
	public void shouldConvertToUintExactConsumer() {
		Captor<Byte> captor = Captor.of();
		var consumer = ByteConsumer.toUintExact(captor::accept);
		consumer.accept(Byte.MAX_VALUE);
		assertThrown(() -> consumer.accept(Byte.MIN_VALUE));
		consumer.accept(0xff);
		captor.verify(Byte.MAX_VALUE, (byte) -1);
	}

}
