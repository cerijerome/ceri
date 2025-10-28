package ceri.common.util;

import java.io.File;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class SystemVarsTest {

	@After
	public void after() {
		SystemVars.clear();
	}

	@Test
	public void testTempDir() {
		Assert.path(SystemVars.tempDir(), SystemVars.sys("java.io.tmpdir"));
	}

	@Test
	public void testUserHome() {
		Assert.path(SystemVars.userHome(), SystemVars.sys("user.home"));
		Assert.path(SystemVars.userHome("test"), SystemVars.sys("user.home") + "/test");
	}

	@Test
	public void testUserDir() {
		Assert.path(SystemVars.userDir(), SystemVars.sys("user.dir"));
		Assert.path(SystemVars.userDir("test"), SystemVars.sys("user.dir") + "/test");
	}

	@Test
	public void testSysPath() {
		Assert.isNull(SystemVars.sysPath("?"));
	}

	@Test
	public void testEnvPath() {
		Assert.isNull(SystemVars.envPath("?"));
		var name = Testing.firstEnvVarName();
		Assert.path(SystemVars.envPath(name), SystemVars.env(name));
	}

	@Test
	public void testPathVar() {
		Assert.equal(SystemVars.pathVar(), "");
		Assert.equal(SystemVars.pathVar("a/b"), "a/b");
		Assert.equal(SystemVars.pathVar("a/b", "c/d"), "a/b" + File.pathSeparator + "c/d");
	}

	@Test
	public void testVarPaths() {
		Assert.unordered(SystemVars.varPaths(null));
		Assert.unordered(SystemVars.varPaths(""));
		Assert.unordered(SystemVars.varPaths(File.pathSeparator));
		Assert.unordered(SystemVars.varPaths(" " + File.pathSeparator + " a"), "a");
		Assert.unordered(SystemVars.varPaths("a/b" + File.pathSeparator + "a/b"), "a/b");
		Assert.unordered(SystemVars.varPaths(" a/b" + File.pathSeparator + "a/b /c"), "a/b",
			"a/b /c");
	}

	@Test
	public void testGetAllEnvValues() {
		String name = Testing.firstEnvVarName();
		SystemVars.set("!@#$%", "test");
		var map = SystemVars.env();
		Assert.equal(map.get(name), System.getenv(name));
		Assert.equal(map.get("!@#$%"), "test");
	}

	@Test
	public void testGetAllSysValues() {
		String name = Testing.firstSysPropName();
		SystemVars.set("!@#$%", "test");
		var map = SystemVars.sys();
		Assert.equal(map.get(name), System.getProperty(name));
		Assert.equal(map.get("!@#$%"), "test");
	}

	@Test
	public void testSetNullProperty() {
		Assert.isNull(SystemVars.setProperty(null, "test"));
		Assert.isNull(SystemVars.setProperty(" ", "test"));
	}

	@Test
	public void testRemovableProperty() {
		String key = getClass().getName();
		try (var _ = SystemVars.removableProperty(key, "test")) {
			Assert.equal(System.getProperty(key), "test");
		}
		Assert.equal(System.getProperty(key), null);
	}

	@Test
	public void testRemove() {
		Assert.isNull(SystemVars.remove(null));
		Assert.isNull(SystemVars.remove("!@#$%"));
		Assert.isNull(SystemVars.set("!@#$%", "test"));
		Assert.equal(SystemVars.remove("!@#$%"), "test");
		Assert.isNull(SystemVars.remove("!@#$%"));
		Assert.isNull(SystemVars.env("!@#$%"));
		Assert.isNull(SystemVars.sys("!@#$%"));
	}

	@Test
	public void testSetNull() {
		Assert.isNull(SystemVars.set("!@#$%", "test"));
		Assert.equal(SystemVars.env("!@#$%"), "test");
		Assert.equal(SystemVars.sys("!@#$%"), "test");
		Assert.equal(SystemVars.set("!@#$%", null), "test");
		Assert.isNull(SystemVars.env("!@#$%"));
		Assert.isNull(SystemVars.sys("!@#$%"));
	}

	@Test
	public void testSetNullToHideVariables() {
		String name = Testing.firstSysPropName();
		Assert.isNull(SystemVars.set(name, null));
		Assert.isNull(SystemVars.sys(name));
		Assert.equal(SystemVars.sys(name, "test"), "test");
		var map = SystemVars.sys();
		Assert.isNull(map.get(name));
	}

	@Test
	public void testSet() {
		Assert.isNull(SystemVars.env("!@#$%"));
		Assert.isNull(SystemVars.sys("!@#$%"));
		Assert.isNull(SystemVars.set(null, null));
		Assert.isNull(SystemVars.set(null, "test"));
		Assert.isNull(SystemVars.set("!@#$%", null));
		Assert.isNull(SystemVars.set("!@#$%", "test"));
		Assert.equal(SystemVars.set("!@#$%", "test2"), "test");
		Assert.equal(SystemVars.env("!@#$%"), "test2");
		Assert.equal(SystemVars.sys("!@#$%"), "test2");
	}

	@Test
	public void testEnvWithDefault() {
		Assert.isNull(SystemVars.env("!@#$%"));
		Assert.equal(SystemVars.env("!@#$%", "test"), "test");
	}

	@Test
	public void testSysWithDefault() {
		Assert.isNull(SystemVars.sys("!@#$%"));
		Assert.equal(SystemVars.sys("!@#$%", "test"), "test");
	}

	@Test
	public void testEnvWithoutOverride() {
		String name = Testing.firstEnvVarName();
		Assert.equal(SystemVars.env(name), System.getenv(name));
	}

	@Test
	public void testSysWithoutOverride() {
		String name = Testing.firstSysPropName();
		Assert.equal(SystemVars.sys(name), System.getProperty(name));
	}

	@Test
	public void testRemovable() {
		Assert.equal(SystemVars.env("!@#$%"), null);
		Assert.equal(SystemVars.sys("!@#$%"), null);
		try (var _ = SystemVars.removable("!@#$%", "test")) {
			Assert.equal(SystemVars.env("!@#$%"), "test");
			Assert.equal(SystemVars.sys("!@#$%"), "test");
		}
		Assert.equal(SystemVars.env("!@#$%"), null);
		Assert.equal(SystemVars.sys("!@#$%"), null);
	}
}
