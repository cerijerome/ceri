package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.longUnaryOperator;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.LongUnaryOperator;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionLongUnaryOperatorBehavior {

	@Test
	public void shouldConvertToUnaryOperator() {
		LongUnaryOperator f = longUnaryOperator().asLongUnaryOperator();
		assertEquals(f.applyAsLong(2), 2L);
		assertThrown(RuntimeException.class, () -> f.applyAsLong(1));
		assertThrown(RuntimeException.class, () -> f.applyAsLong(0));
	}

	@Test
	public void shouldConvertFromUnaryOperator() {
		ExceptionLongUnaryOperator<RuntimeException> f =
			ExceptionLongUnaryOperator.of(Std.longUnaryOperator());
		assertEquals(f.applyAsLong(1), 1L);
		assertThrown(() -> f.applyAsLong(0));
	}

	@Test
	public void shouldComposeOperators() throws IOException {
		ExceptionLongUnaryOperator<IOException> f = longUnaryOperator().compose(i -> i + 1);
		assertEquals(f.applyAsLong(1), 2L);
		assertThrown(IOException.class, () -> f.applyAsLong(0));
		assertThrown(RuntimeException.class, () -> f.applyAsLong(-1));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		ExceptionLongUnaryOperator<IOException> f = longUnaryOperator().andThen(i -> i + 1);
		assertEquals(f.applyAsLong(2), 3L);
		assertThrown(IOException.class, () -> f.applyAsLong(1));
		assertThrown(RuntimeException.class, () -> f.applyAsLong(0));
	}

}
