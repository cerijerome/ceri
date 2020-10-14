package ceri.common.test;

import static ceri.common.test.TestUtil.matchesRegex;
import static org.hamcrest.CoreMatchers.not;
import static ceri.common.test.TestUtil.assertThat;
import java.io.PrintStream;
import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.reflect.Caller;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;

public class DebuggerBehavior {

	@Test
	public void shouldAllowLoggingOfNullObjects() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log((Object[]) null);
			dbg.method((Object[]) null);
		}
		assertThat(b, matchesRegex("(?ms).*\\) \n.*"));
		assertThat(b, matchesRegex("(?ms).*\\(\\)[^\n]+\n"));
	}

	@Test
	public void shouldLogMultipleObjects() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log("test1", "test2");
			dbg.method("test3", "test4");
		}
		assertThat(b, matchesRegex("(?ms).* test1, test2\n.*"));
		assertThat(b, matchesRegex("(?ms).*\\(test3, test4\\).*"));
	}

	@Test
	public void shouldLogFullPackageName() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, true);
			dbg.log("test");
		}
		assertThat(b, matchesRegex("(?ms).* %s\\.\\.\\(%s\\.java:\\d+\\) test\n.*",
			getClass().getPackageName(), getClass().getSimpleName()));
	}

	@Test
	public void shouldStopLoggingAfterGivenLimit() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 1, false);
			dbg.log("test1");
			dbg.log("test2");
			dbg.method("test2");
		}
		assertThat(b, matchesRegex("(?ms).* test1\n"));
		assertThat(b, not(matchesRegex("(?ms).* test2\n")));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteToSysErrByDefault() {
		StringBuilder b = new StringBuilder();
		try (SystemIo sys = SystemIo.of()) {
			sys.err(StringUtil.asPrintStream(b));
			Debugger dbg = Debugger.of();
			dbg.log("test");
		}
		assertThat(b, matchesRegex("(?ms).* test\n"));
	}

	@Test
	public void shouldLogMethodFileAndMessage() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log("test");
		}
		assertThat(b, matchesRegex("(?ms)\\Q" + method() + " (" + file() + ":\\E\\d+\\) test\n"));
	}

	@Test
	public void shouldLogMethodFileAndNull() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log((Object) null);
		}
		assertThat(b, matchesRegex("(?ms)\\Q" + method() + " (" + file() + ":\\E\\d+\\) null\n"));
	}

	@Test
	public void shouldLogMethodAndFileOnlyIfEmpty() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			dbg.log();
		}
		assertThat(b, matchesRegex("(?ms)\\Q" + method() + " (" + file() + ":\\E\\d+\\) \n"));
	}

	@Test
	public void shouldCountAndPrintMethodInvocations() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = Debugger.of(out, Integer.MAX_VALUE, 0, false);
			debugMethod(dbg, "test1");
			debugMethod(dbg, "test2");
		}
		String method1 = debugMethod(null, "test1");
		String method2 = debugMethod(null, "test2");
		assertThat(b, matchesRegex("(?ms)\\Q" + method1 + " (" + file() + ":\\E\\d+\\) 0\n" +
			"\\Q" + method2 + " (" + file() + ":\\E\\d+\\) 1\n"));
	}

	private String debugMethod(Debugger dbg, String msg) {
		if (dbg != null) dbg.method(msg);
		return method(msg);
	}

	private String file() {
		return ReflectUtil.currentCaller().file;
	}

	private String method() {
		Caller caller = ReflectUtil.previousCaller(1);
		return caller.cls + '.' + caller.method + "()";
	}

	private String method(String value) {
		Caller caller = ReflectUtil.previousCaller(1);
		return caller.cls + '.' + caller.method + "(" + value + ")";
	}

}
