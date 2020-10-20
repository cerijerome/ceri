package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ToBooleanFunctionBehavior {

	@Test
	public void shouldConvertToIntFunction() {
		ToBooleanFunction<String> fn = Boolean::parseBoolean;
		assertEquals(ToBooleanFunction.toInt(fn).applyAsInt("true"), 1);
		assertEquals(ToBooleanFunction.toInt(fn).applyAsInt("false"), 0);
	}

}
