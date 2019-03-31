package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Set;
import org.junit.Test;

public class StartupValuesBehavior {

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
		String sysProp = firstSysPropertyName();
		assertThat(v.value(sysProp, null).get(), is(System.getProperty(sysProp)));
	}

	@Test
	public void shouldGetEnvironmentVariables() {
		StartupValues v = StartupValues.of();
		String envVar = firstEnvVariableName();
		assertThat(v.value(0, null, envVar).get(), is(System.getenv().get(envVar)));
		assertThat(v.value("", "").get("x"), is("x"));
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
