package ceri.common.reflect;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class CallerBehavior {

	@Test
	public void shouldSupportNull() {
		TestUtil.assertThrown(Caller.NULL::cls);
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
		exerciseEquals(caller, caller1);
		assertAllNotEqual(caller, caller2, caller3, caller4, caller5);
	}

}
