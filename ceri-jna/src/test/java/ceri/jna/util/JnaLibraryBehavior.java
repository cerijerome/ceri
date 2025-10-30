package ceri.jna.util;

import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.jna.Library;
import ceri.common.test.Assert;
import ceri.common.util.Os;
import ceri.common.util.SystemVars;
import ceri.jna.test.JnaTesting;

public class JnaLibraryBehavior {

	public static interface LibTestNative extends Library {}

	@BeforeClass
	public static void beforeClass() {
		JnaLibrary.path(); // make sure class has initialized
	}
	
	@Test
	public void testPlatformPath() {
		SystemVars.set("jna.platform.library.path", "platform-path-test");
		Assert.equal(JnaLibrary.platformPath(), "platform-path-test");
	}

	@Test
	public void testPath() {
		SystemVars.set("jna.library.path", "path-test");
		Assert.equal(JnaLibrary.path(), "path-test");
	}

	@Test
	public void shouldAddSearchPathForLibrary() {
		var lib = JnaLibrary.of("test", LibTestNative.class);
		lib.addPath("test-path");
	}

	@Test
	public void testEmptyPath() {
		try (var _ = SystemVars.removableProperty("jna.library.path", null)) {
			SystemVars.set("jna.library.path", null);
			JnaLibrary.addPaths();
			Assert.equal(JnaLibrary.path(), "");
		}
	}

	@Test
	public void testMacLibPath() {
		try (var _ = SystemVars.removableProperty("jna.library.path", null)) {
			JnaTesting.testForEachOs(LibPath.class, JnaLibrary.class);
		}
	}

	public static class LibPath {
		static {
			System.clearProperty("jna.library.path");
			if (Os.info().mac) Assert.find(JnaLibrary.path(), "/homebrew/");
			if (Os.info().linux) Assert.notFound(JnaLibrary.path(), "/homebrew/");
		}
	}
}
