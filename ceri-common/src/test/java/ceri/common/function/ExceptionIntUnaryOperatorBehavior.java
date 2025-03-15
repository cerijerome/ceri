package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intUnaryOperator;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertRte;
import java.io.IOException;
import java.util.function.IntUnaryOperator;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntUnaryOperatorBehavior {

	@Test
	public void shouldConvertToUnaryOperator() {
		IntUnaryOperator f = intUnaryOperator().asIntUnaryOperator();
		assertEquals(f.applyAsInt(2), 2);
		assertRte(() -> f.applyAsInt(1));
		assertRte(() -> f.applyAsInt(0));
	}

	@Test
	public void shouldConvertFromUnaryOperator() {
		ExceptionIntUnaryOperator<RuntimeException> f =
			ExceptionIntUnaryOperator.of(Std.intUnaryOperator());
		assertEquals(f.applyAsInt(1), 1);
		assertRte(() -> f.applyAsInt(0));
	}

	@Test
	public void shouldComposeOperators() throws IOException {
		ExceptionIntUnaryOperator<IOException> f = intUnaryOperator().compose(i -> i + 1);
		assertEquals(f.applyAsInt(1), 2);
		assertIoe(() -> f.applyAsInt(0));
		assertRte(() -> f.applyAsInt(-1));
	}

	@Test
	public void shouldSequenceOperators() throws IOException {
		ExceptionIntUnaryOperator<IOException> f = intUnaryOperator().andThen(i -> i + 1);
		assertEquals(f.applyAsInt(2), 3);
		assertIoe(() -> f.applyAsInt(1));
		assertRte(() -> f.applyAsInt(0));
	}

}
