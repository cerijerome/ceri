package ceri.common.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPath;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.firstEnvironmentVariableName;
import static ceri.common.test.TestUtil.firstSystemPropertyName;
import java.nio.file.Path;
import java.util.stream.Stream;
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
		assertEquals(StartupValues.lookup(sysProp).get(), SystemVars.sys(sysProp));
		assertEquals(StartupValues.lookup(null, envVar).get(), SystemVars.env(envVar));
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
			assertEquals(b.toString(), "param = a (from args[0])\n");
		}
	}

	@Test
	public void shouldGetValuesFromArgumentArray() {
		StartupValues v = StartupValues.of("a", null, "c");
		assertEquals(v.next().get(), "a");
		assertEquals(v.next().get("b"), "b");
		assertEquals(v.next().get("d"), "c");
		assertEquals(v.skip().next().get("e"), "e");
		assertEquals(v.value(0).get("d"), "a");
		assertEquals(StartupValues.of((String[]) null).next().get("x"), "x");
		assertEquals(StartupValues.of().next().get("x"), "x");
		assertEquals(StartupValues.of().value(-1).get("x"), "x");
	}

	@Test
	public void shouldGetSystemProperties() {
		StartupValues v = StartupValues.of();
		String sysProp = firstSystemPropertyName();
		assertEquals(v.value(sysProp, null).get(), SystemVars.sys(sysProp));
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
		assertEquals(value.sysProp, "ceri.common.util.testName");
		assertEquals(value.envVar, "CERI_COMMON_UTIL_TESTNAME");
	}

	@Test
	public void shouldConvertValueToType() {
		StartupValues v = StartupValues.of(null, "true", "-1", "0xffffffffff", "1.1", "left");
		assertTrue(v.value(1).asBool());
		assertTrue(v.value(1).asBool(false));
		assertFalse(v.value(0).asBool(false));
		assertEquals(v.value(2).asInt(), -1);
		assertEquals(v.value(2).asInt(1), -1);
		assertEquals(v.value(0).asInt(1), 1);
		assertEquals(v.value(3).asLong(), 0xffffffffffL);
		assertEquals(v.value(3).asLong(-1L), 0xffffffffffL);
		assertEquals(v.value(0).asLong(-1L), -1L);
		assertEquals(v.value(4).asDouble(), 1.1);
		assertEquals(v.value(4).asDouble(-1.1), 1.1);
		assertEquals(v.value(0).asDouble(-1.1), -1.1);
		assertEquals(v.value(0).asEnum(Align.H.right), Align.H.right);
		assertEquals(v.value(5).asEnum(Align.H.right), Align.H.left);
		assertPath(v.value(1).asPath(), "true");
		assertPath(v.value(1).asPath(Path.of("abc")), "true");
		assertPath(v.value(0).asPath(Path.of("abc")), "abc");
	}

	@Test
	public void shouldConvertIntArray() {
		var v = StartupValues.of(null, "", "-1", "0xffffffff,0,0x80000000");
		assertArray(v.value(0).asIntArray());
		assertArray(v.value(1).asIntArray());
		assertArray(v.value(2).asIntArray(), -1);
		assertArray(v.value(3).asIntArray(), 0xffffffff, 0, 0x80000000);
	}

	@Test
	public void shouldConvertLongArray() {
		var v = StartupValues.of(null, "", "-1", "0xffffffffffffffff,0,0x8000000000000000");
		assertArray(v.value(0).asLongArray());
		assertArray(v.value(1).asLongArray());
		assertArray(v.value(2).asLongArray(), -1L);
		assertArray(v.value(3).asLongArray(), 0xffffffffffffffffL, 0L, 0x8000000000000000L);
	}

	@Test
	public void shouldApplyFunctionFromStream() {
		var v = StartupValues.of(null, "a,ab,abc");
		assertEquals(v.value(1).applyFromStream(null, () -> 0), 0);
		assertEquals(v.value(1).applyFromStream(Stream::count, () -> 0), 3L);
	}

	@Test
	public void shouldApplyConverterFunctionToValue() {
		StartupValues v = StartupValues.of("test");
		assertEquals(v.value(0).apply(String::length), 4);
		ExceptionFunction<RuntimeException, String, Integer> nullFn = null;
		assertNull(v.value(0).apply(nullFn));
		assertNull(v.value(1).apply(String::length));
		assertEquals(v.value(1).apply(String::length, 0), 0);
		assertNull(v.value(1).applyFrom(String::length, null));
	}

	@Test
	public void shouldGetEnvironmentVariables() {
		StartupValues v = StartupValues.of();
		String envVar = firstEnvironmentVariableName();
		assertEquals(v.value(0, null, envVar).get(), SystemVars.env(envVar));
		assertEquals(v.value("", "").get("x"), "x");
	}

}
