package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.function.FunctionTestUtil.predicate;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;
import ceri.common.test.TestUtil;

public class ExceptionBiConsumerBehavior {

	@Test
	public void shouldConvertToConsumer() {
		BiConsumer<Integer, Integer> c = biConsumer().asBiConsumer();
		c.accept(2, 3);
		assertThrown(RuntimeException.class, () -> c.accept(1, 2));
		assertThrown(RuntimeException.class, () -> c.accept(0, 2));
	}

	@Test
	public void shouldConvertFromConsumer() {
		ExceptionBiConsumer<RuntimeException, Integer, Integer> c =
			ExceptionBiConsumer.of(Std.biConsumer());
		c.accept(1, 2);
		assertThrown(RuntimeException.class, () -> c.accept(0, 2));
	}

}
