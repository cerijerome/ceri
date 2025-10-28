package ceri.common.reflect;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class CallerBehavior {

	@Test
	public void shouldSupportNull() {
		Assert.thrown(Caller.NULL::cls);
	}

	@Test
	public void shouldConformToEqualsContract() {
		Assert.notEqual(null, Caller.NULL);
		Assert.equal(Caller.NULL, Caller.NULL);
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		Caller caller1 = Caller.fromStackTraceElement(ste);
		Caller caller2 = new Caller(caller.fullCls, caller.line + 1, caller.method, caller.file);
		Caller caller3 = new Caller(caller.fullCls, caller.line, "", caller.file);
		Caller caller4 = new Caller(caller.fullCls, caller.line, caller.method, "");
		Caller caller5 = new Caller("", caller.line, caller.method, caller.file);
		Testing.exerciseEquals(caller, caller1);
		Assert.notEqualAll(caller, caller2, caller3, caller4, caller5);
	}

	@Test
	public void shouldDeterminePackage() {
		Assert.equal(Caller.NULL.pkg(), "");
		StackTraceElement ste = new Exception().getStackTrace()[0];
		Caller caller = Caller.fromStackTraceElement(ste);
		Assert.equal(caller.pkg(), getClass().getPackage().getName());
	}

}
