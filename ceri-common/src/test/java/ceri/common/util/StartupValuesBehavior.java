package ceri.common.util;

import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.text.StringBuilders;

public class StartupValuesBehavior {

	@Test
	public void shouldAllowNullNotifier() {
		var v = StartupValues.of("a").notifier(null);
		Assert.equal(v.next("param", p -> p.get()), "a"); // no output
	}

	@Test
	public void shouldLookupValue() {
		var sysProp = Testing.firstSysPropName();
		var envVar = Testing.firstEnvVarName();
		Assert.equal(StartupValues.lookup(sysProp).get(), SystemVars.sys(sysProp));
		Assert.equal(StartupValues.lookup(null, envVar).get(), SystemVars.env(envVar));
		Assert.equal(StartupValues.lookup(sysProp, p -> p.get()), SystemVars.sys(sysProp));
		Assert.equal(StartupValues.lookup(null, envVar, p -> p.get()), SystemVars.env(envVar));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldRenderParsedValue() {
		var b = new StringBuilder();
		try (var sys = SystemIo.of()) {
			sys.out(StringBuilders.printStream(b));
			var v = StartupValues.sysOut("a").renderer(obj -> "<" + obj + ">");
			v.next("param", p -> p.get());
			Assert.equal(b.toString(), "0) param = <a> ('a' from args[0])\n");
		}
	}

	@Test
	public void shouldGetValuesFromArgumentArray() {
		var v = StartupValues.of("a", null, "c");
		Assert.equal(v.next(p -> p.get()), "a");
		Assert.equal(v.next(p -> p.get("b")), "b");
		Assert.equal(v.next(p -> p.get("d")), "c");
		Assert.equal(v.skip().next(p -> p.get("e")), "e");
		Assert.equal(v.value(0, p -> p.get("d")), "a");
		Assert.equal(StartupValues.of((String[]) null).next(p -> p.get("x")), "x");
		Assert.equal(StartupValues.of().next(p -> p.get("x")), "x");
		Assert.equal(StartupValues.of().value(-1, p -> p.get("x")), "x");
	}

	@Test
	public void shouldGetParserFromArgumentArray() {
		var v = StartupValues.of("a", null, "c");
		Assert.equal(v.next().get(), "a");
		Assert.equal(v.next().get("b"), "b");
		Assert.equal(v.next().get("d"), "c");
		Assert.equal(v.skip().next().get("e"), "e");
		Assert.equal(v.value(0).get("d"), "a");
		Assert.equal(StartupValues.of((String[]) null).next().get("x"), "x");
		Assert.equal(StartupValues.of().next().get("x"), "x");
		Assert.equal(StartupValues.of().value(-1).get("x"), "x");
	}

	@Test
	public void shouldGetSystemProperties() {
		var v = StartupValues.of();
		String sysProp = Testing.firstSysPropName();
		Assert.equal(v.value(sysProp, null, p -> p.get()), SystemVars.sys(sysProp));
		Assert.equal(v.value(1, sysProp, null, p -> p.get()), SystemVars.sys(sysProp));
	}

	@Test
	public void shouldGetParserSystemProperties() {
		var v = StartupValues.of();
		String sysProp = Testing.firstSysPropName();
		Assert.equal(v.value(sysProp, (String) null).get(), SystemVars.sys(sysProp));
		Assert.equal(v.value(1, sysProp, null).get(), SystemVars.sys(sysProp));
	}

	@Test
	public void shouldConvertNameFormat() {
		var v = StartupValues.of();
		Assert.equal(v.sysProp(""), null);
		Assert.equal(v.envVar(""), null);
		Assert.equal(v.sysProp("testName"), "testName");
		Assert.equal(v.envVar("testName"), "TESTNAME");
		v = StartupValues.of().prefix(getClass());
		Assert.equal(v.sysProp(""), null);
		Assert.equal(v.envVar(""), null);
		Assert.equal(v.sysProp("testName"), "ceri.common.util.testName");
		Assert.equal(v.envVar("testName"), "CERI_COMMON_UTIL_TESTNAME");
	}
}
