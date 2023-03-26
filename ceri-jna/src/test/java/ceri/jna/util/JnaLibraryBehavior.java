package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import com.sun.jna.Library;
import ceri.common.util.SystemVars;

public class JnaLibraryBehavior {

	public static interface LibTestNative extends Library {}

	@Test
	public void testPlatformPath() {
		SystemVars.set("jna.platform.library.path", "platform-path-test");
		assertEquals(JnaLibrary.platformPath(), "platform-path-test");
	}

	@Test
	public void testPath() {
		SystemVars.set("jna.library.path", "path-test");
		assertEquals(JnaLibrary.path(), "path-test");
	}

	@Test
	public void shouldAddSearchPathForLibrary() {
		var lib = JnaLibrary.of("test", LibTestNative.class);
		lib.addPath("test-path");
	}

}
