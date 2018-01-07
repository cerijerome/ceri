package ceri.common.reflect;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CallerBehavior {

	@Test
	public void shouldSupportNull() {
		assertException(() -> Caller.NULL.cls());
	}

	@Test
	public void shouldConformToEqualsContract() {
		assertFalse(Caller.NULL.equals(null));
		assertThat(Caller.NULL, is(Caller.NULL));
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		Caller caller1 = Caller.fromStackTraceElement(ste);
		Caller caller2 = new Caller(caller.fullCls, caller.line + 1, caller.method, caller.file);
		Caller caller3 = new Caller(caller.fullCls, caller.line, "", caller.file);
		Caller caller4 = new Caller(caller.fullCls, caller.line, caller.method, "");
		Caller caller5 = new Caller("", caller.line, caller.method, caller.file);
		assertFalse(caller.equals(null));
		assertTrue(caller.equals(caller));
		assertTrue(caller.equals(caller1));
		assertFalse(caller.equals(caller2));
		assertFalse(caller.equals(caller3));
		assertFalse(caller.equals(caller4));
		assertFalse(caller.equals(caller5));
		assertThat(caller.hashCode(), is(caller1.hashCode()));
		assertThat(caller.toString(), is(caller1.toString()));
	}

}
