package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longConsumer;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.LongConsumer;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.Captor;

public class ExceptionLongConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		LongConsumer c = longConsumer().asLongConsumer();
		c.accept(2);
		assertThrown(RuntimeException.class, () -> c.accept(1));
		assertThrown(RuntimeException.class, () -> c.accept(0));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionLongConsumer<RuntimeException> c = ExceptionLongConsumer.of(Std.longConsumer());
		c.accept(1);
		assertThrown(RuntimeException.class, () -> c.accept(0));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		Captor.OfInt capturer = Captor.ofInt();
		ExceptionLongConsumer<IOException> f = longConsumer().andThen(capturer::accept);
		f.accept(2);
		capturer.verify(2);
		assertThrown(IOException.class, () -> f.accept(1));
		assertThrown(RuntimeException.class, () -> f.accept(0));
	}

}
