package ceri.common.function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ToShortFunctionBehavior {

	@Test
	public void testToUint() {
		ToShortFunction<String> fn = s -> (short) s.charAt(0);
		assertThat(fn.applyAsShort("\uffff"), is((short) -1));
		assertThat(ToShortFunction.toUint(fn).applyAsInt("\uffff"), is(0xffff));
	}

}
