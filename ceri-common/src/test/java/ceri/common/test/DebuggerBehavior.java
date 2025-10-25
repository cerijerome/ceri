package ceri.common.test;

import static ceri.common.test.Assert.assertMatch;
import static ceri.common.test.Assert.assertNoMatch;
import java.io.PrintStream;
import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.reflect.Caller;
import ceri.common.reflect.Reflect;
import ceri.common.text.StringBuilders;

public class DebuggerBehavior {

	@Test
	public void shouldAllowLoggingOfNullObjects() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log((Object[]) null);
			dbg.method((Object[]) null);
		}
		assertMatch(b, "(?ms).*\\) \n.*");
		assertMatch(b, "(?ms).*\\(\\)[^\n]+\n");
	}

	@Test
	public void shouldLogMultipleObjects() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log("test1", "test2");
			dbg.method("test3", "test4");
		}
		assertMatch(b, "(?ms).* test1, test2\n.*");
		assertMatch(b, "(?ms).*\\(test3, test4\\).*");
	}

	@Test
	public void shouldLogFullPackageName() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, true);
			dbg.log("test");
		}
		assertMatch(b, "(?ms).* %s\\.\\.\\(%s\\.java:\\d+\\) test\n.*", getClass().getPackageName(),
			getClass().getSimpleName());
	}

	@Test
	public void shouldStopLoggingAfterGivenLimit() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 1, false);
			dbg.log("test1");
			dbg.log("test2");
			dbg.method("test2");
		}
		assertMatch(b, "(?ms).* test1\n");
		assertNoMatch(b, "(?ms).* test2\n");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteToSysErrByDefault() {
		StringBuilder b = new StringBuilder();
		try (SystemIo sys = SystemIo.of()) {
			sys.err(StringBuilders.printStream(b));
			Debugger dbg = Debugger.of();
			dbg.log("test");
		}
		assertMatch(b, "(?ms).* test\n");
	}

	@Test
	public void shouldLogMethodFileAndMessage() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log("test");
		}
		assertMatch(b, "(?ms)\\Q%s (%s:\\E\\d+\\) test\n", method(), file());
	}

	@Test
	public void shouldLogMethodFileAndNull() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log((Object) null);
		}
		assertMatch(b, "(?ms)\\Q%s (%s:\\E\\d+\\) null\n", method(), file());
	}

	@Test
	public void shouldLogMethodAndFileOnlyIfEmpty() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log();
		}
		assertMatch(b, "(?ms)\\Q%s (%s:\\E\\d+\\) \n", method(), file());
	}

	@Test
	public void shouldCountAndPrintMethodInvocations() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringBuilders.printStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			debugMethod(dbg, "test1");
			debugMethod(dbg, "test2");
		}
		String method1 = debugMethod(null, "test1");
		String method2 = debugMethod(null, "test2");
		assertMatch(b, "(?ms)\\Q%s (%s:\\E\\d+\\) 0\n\\Q%s (%s:\\E\\d+\\) 1\n", method1, file(),
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
