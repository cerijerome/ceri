package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intConsumer;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.IntConsumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Captor;

public class ExceptionIntConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		IntConsumer c = intConsumer().asIntConsumer();
		c.accept(2);
		assertThrown(RuntimeException.class, () -> c.accept(1));
		assertThrown(RuntimeException.class, () -> c.accept(0));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionIntConsumer<RuntimeException> c = ExceptionIntConsumer.of(Std.intConsumer());
		c.accept(1);
		assertThrown(RuntimeException.class, () -> c.accept(0));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		Captor.OfInt capturer = Captor.ofInt();
		ExceptionIntConsumer<IOException> f = intConsumer().andThen(capturer::accept);
		f.accept(2);
		capturer.verify(2);
		assertThrown(IOException.class, () -> f.accept(1));
		assertThrown(RuntimeException.class, () -> f.accept(0));
	}

}
