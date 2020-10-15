package ceri.common.util;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class SystemVarsTest {

	@After
	public void after() {
		SystemVars.clear();
	}

	@Test
	public void testGetAllEnvValues() {
		String name = TestUtil.firstEnvironmentVariableName();
		SystemVars.set("!@#$%", "test");
		var map = SystemVars.env();
		assertThat(map.get(name), is(System.getenv(name)));
		assertThat(map.get("!@#$%"), is("test"));
	}

	@Test
	public void testGetAllSysValues() {
		String name = TestUtil.firstSystemPropertyName();
		SystemVars.set("!@#$%", "test");
		var map = SystemVars.sys();
		assertThat(map.get(name), is(System.getProperty(name)));
		assertThat(map.get("!@#$%"), is("test"));
	}

	@Test
	public void testRemove() {
		assertNull(SystemVars.remove(null));
		assertNull(SystemVars.remove("!@#$%"));
		assertNull(SystemVars.set("!@#$%", "test"));
		assertThat(SystemVars.remove("!@#$%"), is("test"));
		assertNull(SystemVars.remove("!@#$%"));
		assertNull(SystemVars.env("!@#$%"));
		assertNull(SystemVars.sys("!@#$%"));
	}

	@Test
	public void testSetNull() {
		assertNull(SystemVars.set("!@#$%", "test"));
		assertThat(SystemVars.env("!@#$%"), is("test"));
		assertThat(SystemVars.sys("!@#$%"), is("test"));
		assertThat(SystemVars.set("!@#$%", null), is("test"));
		assertNull(SystemVars.env("!@#$%"));
		assertNull(SystemVars.sys("!@#$%"));
	}

	@Test
	public void testSetNullToHideVariables() {
		String name = TestUtil.firstSystemPropertyName();
		assertNull(SystemVars.set(name, null));
		assertNull(SystemVars.sys(name));
		assertThat(SystemVars.sys(name, "test"), is("test"));
		var map = SystemVars.sys();
		assertNull(map.get(name));
	}

	@Test
	public void testSet() {
		assertNull(SystemVars.env("!@#$%"));
		assertNull(SystemVars.sys("!@#$%"));
		assertNull(SystemVars.set(null, null));
		assertNull(SystemVars.set(null, "test"));
		assertNull(SystemVars.set("!@#$%", null));
		assertNull(SystemVars.set("!@#$%", "test"));
		assertThat(SystemVars.set("!@#$%", "test2"), is("test"));
		assertThat(SystemVars.env("!@#$%"), is("test2"));
		assertThat(SystemVars.sys("!@#$%"), is("test2"));
	}

	@Test
	public void testEnvWithDefault() {
		assertNull(SystemVars.env("!@#$%"));
		assertThat(SystemVars.env("!@#$%", "test"), is("test"));
	}

	@Test
	public void testSysWithDefault() {
		assertNull(SystemVars.sys("!@#$%"));
		assertThat(SystemVars.sys("!@#$%", "test"), is("test"));
	}

	@Test
	public void testEnvWithoutOverride() {
		String name = TestUtil.firstEnvironmentVariableName();
		assertThat(SystemVars.env(name), is(System.getenv(name)));
	}

	@Test
	public void testSysWithoutOverride() {
		String name = TestUtil.firstSystemPropertyName();
		assertThat(SystemVars.sys(name), is(System.getProperty(name)));
	}

}
