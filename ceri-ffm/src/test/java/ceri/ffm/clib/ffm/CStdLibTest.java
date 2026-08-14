package ceri.ffm.clib.ffm;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;

public class CStdLibTest {
	private static final String KEY = CStdLibTest.class.getName();
	private final FfmTesting.Lib<TestCLibNative> lib = TestCLibNative.lib();

	@After
	public void after() {
		Closeables.close(lib);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CStdLib.class);
	}

	@Test
	public void testGetEnv() throws CException {
		Assert.equal(CStdLib.getenv(KEY + "x"), null);
	}

	@Test
	public void testSetEnvWithOverwrite() throws CException {
		CStdLib.setenv(KEY, "123", true);
		CStdLib.setenv(KEY, "456", true);
		Assert.equal(CStdLib.getenv(KEY), "456");
	}

	@Test
	public void testSetEnvWithoutOverwrite() throws CException {
		CStdLib.setenv(KEY, "123", true);
		CStdLib.setenv(KEY, "456", false);
		Assert.equal(CStdLib.getenv(KEY), "123");
	}

	@Test
	public void testSetEnvErrors() {
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdLib.setenv(null, "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdLib.setenv("", "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdLib.setenv("x=y", "test", false));
	}

	@Test
	public void testSetEnvEmulated() throws CException {
		lib.init();
		Assert.equal(CStdLib.getenv(KEY), null);
		CStdLib.setenv(KEY, "123", false);
		Assert.equal(CStdLib.getenv(KEY), "123");
		CStdLib.setenv(KEY, "456", false);
		Assert.equal(CStdLib.getenv(KEY), "123");
		CStdLib.setenv(KEY, "456", true);
		Assert.equal(CStdLib.getenv(KEY), "456");
	}

	@Test
	public void testSetEnvEmulatedErrors() {
		lib.init();
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdLib.setenv(null, "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdLib.setenv("", "test", false));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CStdLib.setenv("x=y", "test", false));
	}
}
