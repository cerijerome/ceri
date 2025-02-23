package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;
import ceri.jna.clib.test.TestCLibNative;

public class CStdlibTest {
	private static final String KEY = CStdlibTest.class.getName();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CStdlib.class);
	}

	@Test
	public void testNativeGetEnv() throws CException {
		assertEquals(CStdlib.getenv(KEY + "x"), null);
	}
	
	@Test
	public void testNativeSetEnvWithOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", true);
		assertEquals(CStdlib.getenv(KEY), "456");
	}

	@Test
	public void testNativeSetEnvWithoutOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", false);
		assertEquals(CStdlib.getenv(KEY), "123");
	}

	@Test
	public void testSetEnv() throws CException {
		try (var _ = TestCLibNative.register()) {
			assertEquals(CStdlib.getenv(KEY), null);
			CStdlib.setenv(KEY, "123", false);
			assertEquals(CStdlib.getenv(KEY), "123");
			CStdlib.setenv(KEY, "456", false);
			assertEquals(CStdlib.getenv(KEY), "123");
			CStdlib.setenv(KEY, "456", true);
			assertEquals(CStdlib.getenv(KEY), "456");
		}
	}

}
