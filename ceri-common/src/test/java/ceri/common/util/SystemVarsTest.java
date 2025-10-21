package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPath;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.io.File;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class SystemVarsTest {

	@After
	public void after() {
		SystemVars.clear();
	}

	@Test
	public void testTempDir() {
		assertPath(SystemVars.tempDir(), SystemVars.sys("java.io.tmpdir"));
	}

	@Test
	public void testUserHome() {
		assertPath(SystemVars.userHome(), SystemVars.sys("user.home"));
		assertPath(SystemVars.userHome("test"), SystemVars.sys("user.home") + "/test");
	}

	@Test
	public void testUserDir() {
		assertPath(SystemVars.userDir(), SystemVars.sys("user.dir"));
		assertPath(SystemVars.userDir("test"), SystemVars.sys("user.dir") + "/test");
	}

	@Test
	public void testSysPath() {
		assertNull(SystemVars.sysPath("?"));
	}

	@Test
	public void testEnvPath() {
		assertNull(SystemVars.envPath("?"));
		var name = TestUtil.firstEnvironmentVariableName();
		assertPath(SystemVars.envPath(name), SystemVars.env(name));
	}

	@Test
	public void testPathVar() {
		assertEquals(SystemVars.pathVar(), "");
		assertEquals(SystemVars.pathVar("a/b"), "a/b");
		assertEquals(SystemVars.pathVar("a/b", "c/d"), "a/b" + File.pathSeparator + "c/d");
	}

	@Test
	public void testVarPaths() {
		assertUnordered(SystemVars.varPaths(null));
		assertUnordered(SystemVars.varPaths(""));
		assertUnordered(SystemVars.varPaths(File.pathSeparator));
		assertUnordered(SystemVars.varPaths(" " + File.pathSeparator + " a"), "a");
		assertUnordered(SystemVars.varPaths("a/b" + File.pathSeparator + "a/b"), "a/b");
		assertUnordered(SystemVars.varPaths(" a/b" + File.pathSeparator + "a/b /c"), "a/b",
			"a/b /c");
	}

	@Test
	public void testGetAllEnvValues() {
		String name = TestUtil.firstEnvironmentVariableName();
		SystemVars.set("!@#$%", "test");
		var map = SystemVars.env();
		assertEquals(map.get(name), System.getenv(name));
		assertEquals(map.get("!@#$%"), "test");
	}

	@Test
	public void testGetAllSysValues() {
		String name = TestUtil.firstSystemPropertyName();
		SystemVars.set("!@#$%", "test");
		var map = SystemVars.sys();
		assertEquals(map.get(name), System.getProperty(name));
		assertEquals(map.get("!@#$%"), "test");
	}

	@Test
	public void testSetNullProperty() {
		assertNull(SystemVars.setProperty(null, "test"));
		assertNull(SystemVars.setProperty(" ", "test"));
	}

	@Test
	public void testRemovableProperty() {
		String key = getClass().getName();
		try (var _ = SystemVars.removableProperty(key, "test")) {
			assertEquals(System.getProperty(key), "test");
		}
		assertEquals(System.getProperty(key), null);
	}

	@Test
	public void testRemove() {
		assertNull(SystemVars.remove(null));
		assertNull(SystemVars.remove("!@#$%"));
		assertNull(SystemVars.set("!@#$%", "test"));
		assertEquals(SystemVars.remove("!@#$%"), "test");
		assertNull(SystemVars.remove("!@#$%"));
		assertNull(SystemVars.env("!@#$%"));
		assertNull(SystemVars.sys("!@#$%"));
	}

	@Test
	public void testSetNull() {
		assertNull(SystemVars.set("!@#$%", "test"));
		assertEquals(SystemVars.env("!@#$%"), "test");
		assertEquals(SystemVars.sys("!@#$%"), "test");
		assertEquals(SystemVars.set("!@#$%", null), "test");
		assertNull(SystemVars.env("!@#$%"));
		assertNull(SystemVars.sys("!@#$%"));
	}

	@Test
	public void testSetNullToHideVariables() {
		String name = TestUtil.firstSystemPropertyName();
		assertNull(SystemVars.set(name, null));
		assertNull(SystemVars.sys(name));
		assertEquals(SystemVars.sys(name, "test"), "test");
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
		assertEquals(SystemVars.set("!@#$%", "test2"), "test");
		assertEquals(SystemVars.env("!@#$%"), "test2");
		assertEquals(SystemVars.sys("!@#$%"), "test2");
	}

	@Test
	public void testEnvWithDefault() {
		assertNull(SystemVars.env("!@#$%"));
		assertEquals(SystemVars.env("!@#$%", "test"), "test");
	}

	@Test
	public void testSysWithDefault() {
		assertNull(SystemVars.sys("!@#$%"));
		assertEquals(SystemVars.sys("!@#$%", "test"), "test");
	}

	@Test
	public void testEnvWithoutOverride() {
		String name = TestUtil.firstEnvironmentVariableName();
		assertEquals(SystemVars.env(name), System.getenv(name));
	}

	@Test
	public void testSysWithoutOverride() {
		String name = TestUtil.firstSystemPropertyName();
		assertEquals(SystemVars.sys(name), System.getProperty(name));
	}

	@Test
	public void testRemovable() {
		assertEquals(SystemVars.env("!@#$%"), null);
		assertEquals(SystemVars.sys("!@#$%"), null);
		try (var _ = SystemVars.removable("!@#$%", "test")) {
			assertEquals(SystemVars.env("!@#$%"), "test");
			assertEquals(SystemVars.sys("!@#$%"), "test");
		}
		assertEquals(SystemVars.env("!@#$%"), null);
		assertEquals(SystemVars.sys("!@#$%"), null);
	}
}
