package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.unaryOperator;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRte;
import java.util.function.UnaryOperator;
import org.junit.Test;
import ceri.common.function.FunctionTestUtil.Std;

public class ExceptionUnaryOperatorBehavior {

	@Test
	public void shouldConvertToUnaryOperator() {
		UnaryOperator<Integer> f = unaryOperator().asUnaryOperator();
		assertEquals(f.apply(2), 2);
		assertRte(() -> f.apply(1));
		assertRte(() -> f.apply(0));
	}

	@Test
	public void shouldConvertFromUnaryOperator() {
		ExceptionUnaryOperator<RuntimeException, Integer> f =
			ExceptionUnaryOperator.of(Std.unaryOperator());
		f.apply(1);
		assertRte(() -> f.apply(0));
	}

}
