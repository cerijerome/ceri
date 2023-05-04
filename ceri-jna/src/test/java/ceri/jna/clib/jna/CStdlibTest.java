package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class CStdlibTest {
	private static final String KEY = CStdlibTest.class.getName();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CStdlib.class);
	}

	@Test
	public void testSetEnvWithOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", true);
		assertEquals(CStdlib.getenv(KEY), "456");
	}

	@Test
	public void testSetEnvWithoutOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", false);
		assertEquals(CStdlib.getenv(KEY), "123");
	}

}
