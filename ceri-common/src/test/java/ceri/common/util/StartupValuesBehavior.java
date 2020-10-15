package ceri.common.util;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertPath;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.firstEnvironmentVariableName;
import static ceri.common.test.TestUtil.firstSystemPropertyName;
import static org.hamcrest.CoreMatchers.is;
import java.nio.file.Path;
import org.junit.Test;
import ceri.common.function.ExceptionFunction;
import ceri.common.io.SystemIo;
import ceri.common.text.StringUtil;
import ceri.common.util.StartupValues.Value;

public class StartupValuesBehavior {

	@Test
	public void shouldAllowNullNotifier() {
		StartupValues v = StartupValues.of("a").notifier(null);
		v.next("param").get();
	}

	@Test
	public void shouldLookupValue() {
		String sysProp = firstSystemPropertyName();
		String envVar = firstEnvironmentVariableName();
		assertThat(StartupValues.lookup(sysProp).get(), is(SystemVars.sys(sysProp)));
		assertThat(StartupValues.lookup(null, envVar).get(), is(SystemVars.env(envVar)));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyStdOutWhenValueIsReads() {
		StringBuilder b = new StringBuilder();
		try (SystemIo sys = SystemIo.of()) {
			sys.out(StringUtil.asPrintStream(b));
			StartupValues v = StartupValues.sysOut("a");
			v.next("param").get();
			v.next().get(); // ignored
			assertThat(b.toString(), is("param = a (from args[0])\n"));
		}
	}

	@Test
	public void shouldGetValuesFromArgumentArray() {
		StartupValues v = StartupValues.of("a", null, "c");
		assertThat(v.next().get(), is("a"));
		assertThat(v.next().get("b"), is("b"));
		assertThat(v.next().get("d"), is("c"));
		assertThat(v.skip().next().get("e"), is("e"));
		assertThat(v.value(0).get("d"), is("a"));
		assertThat(StartupValues.of((String[]) null).next().get("x"), is("x"));
		assertThat(StartupValues.of().next().get("x"), is("x"));
		assertThat(StartupValues.of().value(-1).get("x"), is("x"));
	}

	@Test
	public void shouldGetSystemProperties() {
		StartupValues v = StartupValues.of();
		String sysProp = firstSystemPropertyName();
		assertThat(v.value(sysProp, null).get(), is(SystemVars.sys(sysProp)));
	}

	@Test
	public void shouldConvertNameFormat() {
		StartupValues v = StartupValues.of().prefix(getClass());
		Value value = v.value(null);
		assertNull(value.sysProp);
		assertNull(value.envVar);
		value = v.value("");
		assertNull(value.sysProp);
		assertNull(value.envVar);
		value = v.value("testName");
		assertThat(value.sysProp, is("ceri.common.util.testName"));
		assertThat(value.envVar, is("CERI_COMMON_UTIL_TESTNAME"));
	}

	@Test
	public void shouldConvertValueToType() {
		StartupValues v = StartupValues.of(null, "true", "-1", "0xffffffffff", "1.1");
		assertThat(v.value(1).asBool(), is(true));
		assertThat(v.value(1).asBool(false), is(true));
		assertThat(v.value(0).asBool(false), is(false));
		assertThat(v.value(2).asInt(), is(-1));
		assertThat(v.value(2).asInt(1), is(-1));
		assertThat(v.value(0).asInt(1), is(1));
		assertThat(v.value(3).asLong(), is(0xffffffffffL));
		assertThat(v.value(3).asLong(-1L), is(0xffffffffffL));
		assertThat(v.value(0).asLong(-1L), is(-1L));
		assertThat(v.value(4).asDouble(), is(1.1));
		assertThat(v.value(4).asDouble(-1.1), is(1.1));
		assertThat(v.value(0).asDouble(-1.1), is(-1.1));
		assertPath(v.value(1).asPath(), "true");
		assertPath(v.value(1).asPath(Path.of("abc")), "true");
		assertPath(v.value(0).asPath(Path.of("abc")), "abc");
	}

	@Test
	public void shouldApplyConverterFunctionToValue() {
		StartupValues v = StartupValues.of("test");
		assertThat(v.value(0).apply(String::length), is(4));
		ExceptionFunction<RuntimeException, String, Integer> nullFn = null;
		assertNull(v.value(0).apply(nullFn));
		assertNull(v.value(1).apply(String::length));
		assertThat(v.value(1).apply(String::length, 0), is(0));
		assertNull(v.value(1).applyFrom(String::length, null));
	}

	@Test
	public void shouldGetEnvironmentVariables() {
		StartupValues v = StartupValues.of();
		String envVar = firstEnvironmentVariableName();
		assertThat(v.value(0, null, envVar).get(), is(SystemVars.env(envVar)));
		assertThat(v.value("", "").get("x"), is("x"));
	}

}
