package ceri.common.reflect;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CallerBehavior {

	@Test
	public void shouldConformToEqualsContract() {
		assertFalse(Caller.NULL.equals(null));
		assertThat(Caller.NULL, is(Caller.NULL));
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		Caller caller2 = Caller.fromStackTraceElement(ste);
		assertFalse(caller.equals(null));
		assertTrue(caller.equals(caller));
		assertTrue(caller.equals(caller2));
		assertThat(caller.hashCode(), is(caller2.hashCode()));
		assertThat(caller.toString(), is(caller2.toString()));
	}

}
