package ceri.common.util;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Set;
import org.junit.Test;

public class StartupValueBehavior {

	@Test
	public void testArg() {
		assertNull(StartupValue.arg(0, (String[]) null));
		assertNull(StartupValue.arg(-1, "", "A", "B"));
		assertNull(StartupValue.arg(3, "", "A", "B"));
		assertThat(StartupValue.arg(0, ""), is(""));
		assertThat(StartupValue.arg(1, "A", "B", "C"), is("B"));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		StartupValue v0 = StartupValue.of(2, "sys.prop", "env.var");
		StartupValue v1 = StartupValue.of(2, "sys.prop", "env.var");
		StartupValue v2 = StartupValue.of(1, "sys.prop", "env.var");
		StartupValue v3 = StartupValue.of(2, "sys.props", "env.var");
		StartupValue v4 = StartupValue.of(2, "sys.prop", "env.vars");
		StartupValue v5 = StartupValue.ofClass(null, null, null, null);
		exerciseEquals(v0, v1);
		assertAllNotEqual(v0, v2, v3, v4, v5);
	}

	@Test
	public void shouldCreateEnvVariableNameFromSystemPropertyName() {
		assertNull(StartupValue.of(2, null).envVariableName);
		assertThat(StartupValue.of(2, "sys.prop").envVariableName, is("SYS_PROP"));
	}

	@Test
	public void shouldCreateSystemPropertyNameFromClassPackageAndSuffix() {
		assertNull(StartupValue.ofClass(2, null, null).sysPropertyName);
		assertThat(StartupValue.ofClass(2, null, "test").sysPropertyName, is("test"));
		assertThat(StartupValue.ofClass(2, String.class, null).sysPropertyName, is("java.lang"));
		assertThat(StartupValue.ofClass(2, String.class, "").sysPropertyName, is("java.lang"));
		assertThat(StartupValue.ofClass(2, String.class, "test").sysPropertyName,
			is("java.lang.test"));
	}

	@Test
	public void shouldApplyFunctionToValue() {
		StartupValue v = StartupValue.of(0, "a.b", "A_B");
		String[] args = { "A", "B", "C" };
		assertNull(v.apply(new String[] {}, null));
		assertNull(v.apply(args, null));
		assertThat(v.apply(args, s -> s.toLowerCase()), is("a"));
		assertThat(v.apply(args, null, () -> "x"), is("x"));
	}

	@Test
	public void shouldReadFromMainArguments() {
		String[] args = { "A", "B", "C" };
		assertNull(StartupValue.of(null, null).value(args));
		assertNull(StartupValue.of(3, null).value(args));
		assertThat(StartupValue.of(2, null).value(args), is("C"));
	}

	@Test
	public void shouldReadFromSystemProperty() {
		String key = firstSysPropertyName();
		String value = System.getProperty(key);
		assertNull(StartupValue.of(0, "").value());
		assertThat(StartupValue.of(1, key).value("A"), is(value));
	}

	@Test
	public void shouldReadFromEnvVariable() {
		String key = firstEnvVariableName();
		String value = System.getenv(key);
		assertNull(StartupValue.of(0, null, "").value());
		assertThat(StartupValue.of(1, "", key).value("A"), is(value));
	}

	private String firstSysPropertyName() {
		Set<Object> keys = System.getProperties().keySet();
		if (keys.isEmpty()) return "none";
		return String.valueOf(keys.iterator().next());
	}

	private String firstEnvVariableName() {
		Set<String> keys = System.getenv().keySet();
		if (keys.isEmpty()) return "none";
		return keys.iterator().next();
	}

}
