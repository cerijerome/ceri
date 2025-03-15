package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.byteConsumer;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Captor;

public class ExceptionByteConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		ByteConsumer c = byteConsumer().asByteConsumer();
		c.accept((byte) 2);
		assertRte(() -> c.accept((byte) 1));
		assertRte(() -> c.accept((byte) 0));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionByteConsumer<RuntimeException> c = ExceptionByteConsumer.of(Std.byteConsumer());
		c.accept((byte) 1);
		assertRte(() -> c.accept((byte) 0));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		Captor.OfInt capturer = Captor.ofInt();
		ExceptionByteConsumer<IOException> f = byteConsumer().andThen(capturer::accept);
		f.accept((byte) 2);
		capturer.verify(2);
		assertIoe(() -> f.accept((byte) 1));
		assertRte(() -> f.accept((byte) 0));
	}

}
