package ceri.ffm.clib.ffm;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.ffm.clib.ffm.CSignal.sighandler_t;
import ceri.ffm.clib.ffm.CSignal.sigset_t;
import ceri.ffm.clib.test.TestCLibNative;
import ceri.ffm.clib.test.TestCLibNative.Result;
import ceri.ffm.test.FfmAssert;
import ceri.ffm.test.FfmTesting;

public class CSignalTest {
	private final FfmTesting.Lib<TestCLibNative> lib = TestCLibNative.lib();
	private final Captor.OfInt captor = Captor.ofInt();
	private final sighandler_t handler = captor::accept;

	@After
	public void after() {
		Closeables.close(lib);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CSignal.class);
	}

	@Test
	public void testSignal() throws CException {
		captor.reset();
		CSignal.signalIgnore(CSignal.SIGUSR1);
		var previous = CSignal.signal(CSignal.SIGUSR1, handler);
		Assert.equal(previous.macro(), CSignal.Macro.SIG_IGN);
		Assert.string(previous, "SIG_IGN");
		previous.invoke(CSignal.SIGUSR1); // ignored
		previous = CSignal.signalDefault(CSignal.SIGUSR1);
		Assert.equal(previous.macro(), null);
		Assert.match(previous, "sighandler_t#[0-9a-f]+");
		previous.invoke(CSignal.SIGUSR1);
		captor.verifyInt(CSignal.SIGUSR1);
	}

	@Test
	public void testSignalWithErrors() throws CException {
		lib.init().signal.autoResponses(Result.of(CSignal.Macro.SIG_IGN.pointer),
			Result.errno(CErrNo.EINVAL));
		Assert.equal(CSignal.signalDefault(CSignal.SIGUSR1).macro(), CSignal.Macro.SIG_IGN);
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.signalIgnore(CSignal.SIGUSR1));
	}

	@Test
	public void testRaise() throws CException {
		captor.reset();
		CSignal.signalIgnore(CSignal.SIGUSR1);
		CSignal.raise(CSignal.SIGUSR1); // ignored
		CSignal.signal(CSignal.SIGUSR1, handler);
		CSignal.raise(CSignal.SIGUSR1);
		captor.verifyInt(CSignal.SIGUSR1);
		CSignal.signalDefault(CSignal.SIGUSR1);
	}

	@Test
	public void testRaiseWithErrors() throws CException {
		lib.init().raise.autoResponses(null, CErrNo.EINVAL);
		CSignal.raise(CSignal.SIGUSR1);
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.raise(CSignal.SIGUSR2));
		lib.lib().raise.assertValues(CSignal.SIGUSR1, CSignal.SIGUSR2);
	}

	@Test
	public void testSigSet() throws CException {
		var set = sigset_t.$.pointer();
		CSignal.sigemptyset(set);
		CSignal.sigaddset(set, CSignal.SIGUSR1);
		CSignal.sigaddset(set, CSignal.SIGUSR2);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR1), true);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR2), true);
		CSignal.sigdelset(set, CSignal.SIGUSR2);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR1), true);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR2), false);
		CSignal.sigdelset(set, CSignal.SIGUSR2);
	}

	@Test
	public void testSigSetErrors() throws CException {
		Assert.nullPointer(() -> CSignal.sigemptyset(null));
		Assert.nullPointer(() -> CSignal.sigaddset(null, CSignal.SIGUSR1));
		Assert.nullPointer(() -> CSignal.sigdelset(null, CSignal.SIGUSR1));
		Assert.nullPointer(() -> CSignal.sigismember(null, CSignal.SIGUSR1));
		var set = sigset_t.$.pointer();
		CSignal.sigemptyset(set);
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigaddset(set, -1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigdelset(set, -1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigismember(set, -1));
	}

	@Test
	public void testSigSetEmulated() throws CException {
		lib.init();
		var set = sigset_t.$.pointer();
		CSignal.sigemptyset(set);
		CSignal.sigaddset(set, CSignal.SIGUSR1);
		CSignal.sigaddset(set, CSignal.SIGUSR2);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR1), true);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR2), true);
		CSignal.sigdelset(set, CSignal.SIGUSR2);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR1), true);
		Assert.equal(CSignal.sigismember(set, CSignal.SIGUSR2), false);
		CSignal.sigdelset(set, CSignal.SIGUSR2);
	}

	@Test
	public void testSigSetEmulatedErrors() {
		lib.init().sigset.autoResponses(CErrNo.EINVAL);
		var set = sigset_t.$.pointer();
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigemptyset(set));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigaddset(set, CSignal.SIGUSR1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigdelset(set, CSignal.SIGUSR1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigismember(set, CSignal.SIGUSR1));
	}

	@Test
	public void testOsCoverage() {
		FfmTesting.testForEachOs(CSignal.class);
	}
}
