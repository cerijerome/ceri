package ceri.common.function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ToBooleanFunctionBehavior {

	@Test
	public void shouldConvertToIntFunction() {
		ToBooleanFunction<String> fn = Boolean::parseBoolean;
		assertThat(ToBooleanFunction.toInt(fn).applyAsInt("true"), is(1));
		assertThat(ToBooleanFunction.toInt(fn).applyAsInt("false"), is(0));
	}

}
