package ceri.ffm.core;

import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestConcurrent;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.test.FfmTesting;
import ceri.log.test.LogModifier;

public class LastErrorTest {
	private final FfmTesting.Lib<TestCLibNative> testLib = TestCLibNative.lib();
	private TestCLibNative lib;

	@After
	public void after() {
		Closeables.close(testLib);
		lib = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(LastError.class);
	}

	@Test
	public void testGet() {
		LastError.set(1);
		try (var _ = TestConcurrent.threadRun(() -> Assert.equal(LastError.get(), 0))) {}
		Assert.equal(LastError.get(), 1);
	}

	@Test
	public void testMessage() {
		Assert.find(LastError.message(0), "OK");
		Assert.find(LastError.message(1), ".+");
		Assert.find(LastError.message(Integer.MAX_VALUE), "");
	}

	@Test
	public void testMessageFailure() {
		LogModifier.run(() -> {
			init().general.error.setFrom(ErrorGen.RIX);
			Assert.equal(LastError.message(1), "");
		}, Level.OFF, LastError.class);
	}

	private TestCLibNative init() {
		lib = testLib.init();
		return lib;
	}
}
