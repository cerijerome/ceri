package ceri.common.reflect;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertNotEquals;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class CallerBehavior {

	@Test
	public void shouldSupportNull() {
		Assert.thrown(Caller.NULL::cls);
	}

	@Test
	public void shouldConformToEqualsContract() {
		assertNotEquals(null, Caller.NULL);
		assertEquals(Caller.NULL, Caller.NULL);
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		Caller caller1 = Caller.fromStackTraceElement(ste);
		Caller caller2 = new Caller(caller.fullCls, caller.line + 1, caller.method, caller.file);
		Caller caller3 = new Caller(caller.fullCls, caller.line, "", caller.file);
		Caller caller4 = new Caller(caller.fullCls, caller.line, caller.method, "");
		Caller caller5 = new Caller("", caller.line, caller.method, caller.file);
		TestUtil.exerciseEquals(caller, caller1);
		assertAllNotEqual(caller, caller2, caller3, caller4, caller5);
	}

	@Test
	public void shouldDeterminePackage() {
		assertEquals(Caller.NULL.pkg(), "");
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		assertEquals(caller.pkg(), getClass().getPackage().getName());
	}

}
