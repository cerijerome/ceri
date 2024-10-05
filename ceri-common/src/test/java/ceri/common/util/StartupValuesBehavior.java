package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.firstEnvironmentVariableName;
import static ceri.common.test.TestUtil.firstSystemPropertyName;
import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.text.StringUtil;

public class StartupValuesBehavior {

	@Test
	public void shouldAllowNullNotifier() {
		StartupValues v = StartupValues.of("a").notifier(null);
		assertEquals(v.next("param", p -> p.get()), "a"); // no output
	}

	@Test
	public void shouldLookupValue() {
		String sysProp = firstSystemPropertyName();
		String envVar = firstEnvironmentVariableName();
		assertEquals(StartupValues.lookup(sysProp, p -> p.get()), SystemVars.sys(sysProp));
		assertEquals(StartupValues.lookup(null, envVar, p -> p.get()), SystemVars.env(envVar));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldRenderParsedValue() {
		StringBuilder b = new StringBuilder();
		try (SystemIo sys = SystemIo.of()) {
			sys.out(StringUtil.asPrintStream(b));
			StartupValues v = StartupValues.sysOut("a").renderer(obj -> "<" + obj + ">");
			v.next("param", p -> p.get());
			assertEquals(b.toString(), "0) param = <a> ('a' from args[0])\n");
		}
	}

	@Test
	public void shouldGetValuesFromArgumentArray() {
		StartupValues v = StartupValues.of("a", null, "c");
		assertEquals(v.next(p -> p.get()), "a");
		assertEquals(v.next(p -> p.get("b")), "b");
		assertEquals(v.next(p -> p.get("d")), "c");
		assertEquals(v.skip().next(p -> p.get("e")), "e");
		assertEquals(v.value(0, p -> p.get("d")), "a");
		assertEquals(StartupValues.of((String[]) null).next(p -> p.get("x")), "x");
		assertEquals(StartupValues.of().next(p -> p.get("x")), "x");
		assertEquals(StartupValues.of().value(-1, p -> p.get("x")), "x");
	}

	@Test
	public void shouldGetSystemProperties() {
		StartupValues v = StartupValues.of();
		String sysProp = firstSystemPropertyName();
		assertEquals(v.value(sysProp, null, p -> p.get()), SystemVars.sys(sysProp));
		assertEquals(v.value(1, sysProp, null, p -> p.get()), SystemVars.sys(sysProp));
	}

	@Test
	public void shouldConvertNameFormat() {
		StartupValues v = StartupValues.of();
		assertEquals(v.sysProp(""), null);
		assertEquals(v.envVar(""), null);
		assertEquals(v.sysProp("testName"), "testName");
		assertEquals(v.envVar("testName"), "TESTNAME");
		v = StartupValues.of().prefix(getClass());
		assertEquals(v.sysProp(""), null);
		assertEquals(v.envVar(""), null);
		assertEquals(v.sysProp("testName"), "ceri.common.util.testName");
		assertEquals(v.envVar("testName"), "CERI_COMMON_UTIL_TESTNAME");
	}
}
