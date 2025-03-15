package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.intBinaryOperator;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.IntBinaryOperator;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionIntBinaryOperatorBehavior {

	@Test
	public void shouldConvertToFunction() {
		IntBinaryOperator f = intBinaryOperator().asIntBinaryOperator();
		assertEquals(f.applyAsInt(2, 3), 5);
		assertRte(() -> f.applyAsInt(1, 2));
		assertRte(() -> f.applyAsInt(2, 0));
	}

	@Test
	public void shouldConvertFromFunction() {
		ExceptionIntBinaryOperator<RuntimeException> f =
			ExceptionIntBinaryOperator.of(Std.intBinaryOperator());
		f.applyAsInt(1, 2);
		assertRte(() -> f.applyAsInt(0, 2));
	}

}
