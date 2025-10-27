package ceri.common.test;

import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.reflect.Caller;
import ceri.common.reflect.Reflect;
import ceri.common.text.StringBuilders;

public class DebuggerBehavior {

	@Test
	public void shouldAllowLoggingOfNullObjects() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log((Object[]) null);
			dbg.method((Object[]) null);
		}
		Assert.match(b, "(?ms).*\\) \n.*");
		Assert.match(b, "(?ms).*\\(\\)[^\n]+\n");
	}

	@Test
	public void shouldLogMultipleObjects() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log("test1", "test2");
			dbg.method("test3", "test4");
		}
		Assert.match(b, "(?ms).* test1, test2\n.*");
		Assert.match(b, "(?ms).*\\(test3, test4\\).*");
	}

	@Test
	public void shouldLogFullPackageName() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 0, true);
			dbg.log("test");
		}
		Assert.match(b, "(?ms).* %s\\.\\.\\(%s\\.java:\\d+\\) test\n.*",
			getClass().getPackageName(), getClass().getSimpleName());
	}

	@Test
	public void shouldStopLoggingAfterGivenLimit() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 1, false);
			dbg.log("test1");
			dbg.log("test2");
			dbg.method("test2");
		}
		Assert.match(b, "(?ms).* test1\n");
		Assert.noMatch(b, "(?ms).* test2\n");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteToSysErrByDefault() {
		var b = new StringBuilder();
		try (var sys = SystemIo.of()) {
			sys.err(StringBuilders.printStream(b));
			Debugger dbg = Debugger.of();
			dbg.log("test");
		}
		Assert.match(b, "(?ms).* test\n");
	}

	@Test
	public void shouldLogMethodFileAndMessage() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log("test");
		}
		Assert.match(b, "(?ms)\\Q%s (%s:\\E\\d+\\) test\n", method(), file());
	}

	@Test
	public void shouldLogMethodFileAndNull() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log((Object) null);
		}
		Assert.match(b, "(?ms)\\Q%s (%s:\\E\\d+\\) null\n", method(), file());
	}

	@Test
	public void shouldLogMethodAndFileOnlyIfEmpty() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log();
		}
		Assert.match(b, "(?ms)\\Q%s (%s:\\E\\d+\\) \n", method(), file());
	}

	@Test
	public void shouldCountAndPrintMethodInvocations() {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b)) {
			var dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			debugMethod(dbg, "test1");
			debugMethod(dbg, "test2");
		}
		var method1 = debugMethod(null, "test1");
		var method2 = debugMethod(null, "test2");
		Assert.match(b, "(?ms)\\Q%s (%s:\\E\\d+\\) 0\n\\Q%s (%s:\\E\\d+\\) 1\n", method1, file(),
			method2, file());
	}

	private String debugMethod(Debugger dbg, String msg) {
		if (dbg != null) dbg.method(msg);
		return method(msg);
	}

	private String file() {
		return Reflect.currentCaller().file;
	}

	private String method() {
		Caller caller = Reflect.previousCaller(1);
		return caller.cls + '.' + caller.method + "()";
	}

	private String method(String value) {
		Caller caller = Reflect.previousCaller(1);
		return caller.cls + '.' + caller.method + "(" + value + ")";
	}
}
