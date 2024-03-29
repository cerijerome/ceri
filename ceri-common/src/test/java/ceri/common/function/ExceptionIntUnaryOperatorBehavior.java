package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intUnaryOperator;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.util.function.IntUnaryOperator;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntUnaryOperatorBehavior {

	@Test
	public void shouldConvertToUnaryOperator() {
		IntUnaryOperator f = intUnaryOperator().asIntUnaryOperator();
		assertEquals(f.applyAsInt(2), 2);
		assertThrown(RuntimeException.class, () -> f.applyAsInt(1));
		assertThrown(RuntimeException.class, () -> f.applyAsInt(0));
	}

	@Test
	public void shouldConvertFromUnaryOperator() {
		ExceptionIntUnaryOperator<RuntimeException> f =
			ExceptionIntUnaryOperator.of(Std.intUnaryOperator());
		assertEquals(f.applyAsInt(1), 1);
		assertThrown(() -> f.applyAsInt(0));
	}

	@Test
	public void shouldComposeOperators() throws IOException {
		ExceptionIntUnaryOperator<IOException> f = intUnaryOperator().compose(i -> i + 1);
		assertEquals(f.applyAsInt(1), 2);
		assertThrown(IOException.class, () -> f.applyAsInt(0));
		assertThrown(RuntimeException.class, () -> f.applyAsInt(-1));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		ExceptionIntUnaryOperator<IOException> f = intUnaryOperator().andThen(i -> i + 1);
		assertEquals(f.applyAsInt(2), 3);
		assertThrown(IOException.class, () -> f.applyAsInt(1));
		assertThrown(RuntimeException.class, () -> f.applyAsInt(0));
	}

}
