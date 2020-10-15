package ceri.common.function;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class ToBooleanFunctionBehavior {

	@Test
	public void shouldConvertToIntFunction() {
		ToBooleanFunction<String> fn = Boolean::parseBoolean;
		assertThat(ToBooleanFunction.toInt(fn).applyAsInt("true"), is(1));
		assertThat(ToBooleanFunction.toInt(fn).applyAsInt("false"), is(0));
	}

}
