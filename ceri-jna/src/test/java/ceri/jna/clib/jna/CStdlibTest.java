package ceri.jna.clib.jna;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.clib.test.TestCLibNative;

public class CStdlibTest {
	private static final String KEY = CStdlibTest.class.getName();

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CStdlib.class);
	}

	@Test
	public void testNativeGetEnv() throws CException {
		Assert.equal(CStdlib.getenv(KEY + "x"), null);
	}
	
	@Test
	public void testNativeSetEnvWithOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", true);
		Assert.equal(CStdlib.getenv(KEY), "456");
	}

	@Test
	public void testNativeSetEnvWithoutOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", false);
		Assert.equal(CStdlib.getenv(KEY), "123");
	}

	@Test
	public void testSetEnv() throws CException {
		try (var _ = TestCLibNative.register()) {
			Assert.equal(CStdlib.getenv(KEY), null);
			CStdlib.setenv(KEY, "123", false);
			Assert.equal(CStdlib.getenv(KEY), "123");
			CStdlib.setenv(KEY, "456", false);
			Assert.equal(CStdlib.getenv(KEY), "123");
			CStdlib.setenv(KEY, "456", true);
			Assert.equal(CStdlib.getenv(KEY), "456");
		}
	}

}
