package ceri.common.test;

import static ceri.common.test.TestUtil.matchesRegex;
import static org.junit.Assert.assertThat;
import java.io.PrintStream;
import org.junit.Test;
import ceri.common.reflect.Caller;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.StringUtil;

public class DebuggerBehavior {

	@Test
	public void shouldLogMethodFileAndMessage() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = new Debugger(out, Integer.MAX_VALUE, 0);
			dbg.log("test");
		}
		assertThat(b, matchesRegex("(?ms)\\Q" + method() + " (" + file() + ":\\E\\d+\\) test\n"));
	}

	@Test
	public void shouldLogMethodFileAndNull() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = new Debugger(out, Integer.MAX_VALUE, 0);
			dbg.log((Object) null);
		}
		assertThat(b, matchesRegex("(?ms)\\Q" + method() + " (" + file() + ":\\E\\d+\\) null\n"));
	}

	@Test
	public void shouldLogMethodAndFileOnlyIfEmpty() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = new Debugger(out, Integer.MAX_VALUE, 0);
			dbg.log();
		}
		assertThat(b, matchesRegex("(?ms)\\Q" + method() + " (" + file() + ":\\E\\d+\\) \n"));
	}

	@Test
	public void shouldCountAndPrintMethodInvocations() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			Debugger dbg = new Debugger(out, Integer.MAX_VALUE, 0);
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
