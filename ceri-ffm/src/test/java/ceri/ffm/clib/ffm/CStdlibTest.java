package ceri.ffm.clib.ffm;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;

public class CStdlibTest {
	private static final String KEY = CStdlibTest.class.getName();
	private final FfmTesting.Lib<TestCLibNative> lib = TestCLibNative.lib();

	@After
	public void after() {
		Closeables.close(lib);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CStdlib.class);
	}

	@Test
	public void testGetEnv() throws CException {
		Assert.equal(CStdlib.getenv(KEY + "x"), null);
	}

	@Test
	public void testSetEnvWithOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", true);
		Assert.equal(CStdlib.getenv(KEY), "456");
	}

	@Test
	public void testSetEnvWithoutOverwrite() throws CException {
		CStdlib.setenv(KEY, "123", true);
		CStdlib.setenv(KEY, "456", false);
		Assert.equal(CStdlib.getenv(KEY), "123");
	}

	@Test
	public void testSetEnvErrors() {
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdlib.setenv(null, "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdlib.setenv("", "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdlib.setenv("x=y", "test", false));
	}

	@Test
	public void testSetEnvEmulated() throws CException {
		lib.init();
		Assert.equal(CStdlib.getenv(KEY), null);
		CStdlib.setenv(KEY, "123", false);
		Assert.equal(CStdlib.getenv(KEY), "123");
		CStdlib.setenv(KEY, "456", false);
		Assert.equal(CStdlib.getenv(KEY), "123");
		CStdlib.setenv(KEY, "456", true);
		Assert.equal(CStdlib.getenv(KEY), "456");
	}

	@Test
	public void testSetEnvEmulatedErrors() {
		lib.init();
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdlib.setenv(null, "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdlib.setenv("", "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdlib.setenv("x=y", "test", false));
	}
}
