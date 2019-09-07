package ceri.common.reflect;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CallerBehavior {

	@Test
	public void shouldSupportNull() {
		assertException(Caller.NULL::cls);
	}

	@Test
	public void shouldConformToEqualsContract() {
		assertNotEquals(null, Caller.NULL);
		assertThat(Caller.NULL, is(Caller.NULL));
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		Caller caller1 = Caller.fromStackTraceElement(ste);
		Caller caller2 = new Caller(caller.fullCls, caller.line + 1, caller.method, caller.file);
		Caller caller3 = new Caller(caller.fullCls, caller.line, "", caller.file);
		Caller caller4 = new Caller(caller.fullCls, caller.line, caller.method, "");
		Caller caller5 = new Caller("", caller.line, caller.method, caller.file);
		assertNotEquals(null, caller);
		assertEquals(caller, caller);
		assertEquals(caller, caller1);
		assertNotEquals(caller, caller2);
		assertNotEquals(caller, caller3);
		assertNotEquals(caller, caller4);
		assertNotEquals(caller, caller5);
		assertThat(caller.hashCode(), is(caller1.hashCode()));
		assertThat(caller.toString(), is(caller1.toString()));
	}

}
