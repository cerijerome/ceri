package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intBinaryConsumer;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntBinaryConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		IntBinaryConsumer c = intBinaryConsumer().asIntBinaryConsumer();
		c.accept(2, 3);
		assertThrown(RuntimeException.class, () -> c.accept(1, 2));
		assertThrown(RuntimeException.class, () -> c.accept(2, 0));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionIntBinaryConsumer<RuntimeException> f =
			ExceptionIntBinaryConsumer.of(Std.intBinaryConsumer());
		f.accept(1, 2);
		assertThrown(RuntimeException.class, () -> f.accept(0, 2));
	}

}