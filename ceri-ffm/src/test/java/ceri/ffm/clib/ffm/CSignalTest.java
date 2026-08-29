package ceri.ffm.clib.ffm;

import java.lang.foreign.SegmentAllocator;
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
import ceri.ffm.type.Pointer;

public class CSignalTest {
	private final FfmTesting.Lib<TestCLibNative> lib = TestCLibNative.lib();
	private final Captor<CSignal> captor = Captor.of();
	private final sighandler_t handler = s -> captor.accept(CSignal.xcoder.decode(s));

	@After
	public void after() {
		Closeables.close(lib);
	}

	@SuppressWarnings("resource")
	@Test
	public void testSignal() throws CException {
		captor.reset();
		CSignal.SIGUSR1.setIgnore();
		var previous = CSignal.SIGUSR1.set(handler);
		Assert.equal(previous.macro(), CSignal.Macro.SIG_IGN);
		Assert.string(previous, "SIG_IGN");
		CSignal.SIGUSR1.invoke(previous); // ignored
		previous = CSignal.SIGUSR1.setDefault();
		Assert.equal(previous.macro(), null);
		Assert.match(previous, "sighandler_t#[0-9a-f]+");
		CSignal.SIGUSR1.invoke(previous);
		CSignal.SIGUSR2.invoke(previous.callback());
		captor.verify(CSignal.SIGUSR1, CSignal.SIGUSR2);
	}

	@Test
	public void testNullSignal() throws CException {
		CSignal.SIGUSR1.invoke((CSignal.sighandler_t) null);
		CSignal.SIGUSR1.invoke((CSignal.Result) null);
		CSignal.SIGUSR1.invoke(CSignal.Result.NULL);
		CSignal.SIGUSR1.set(null);
	}

	@Test
	public void testSignalWithErrors() throws CException {
		lib.init().signal.autoResponses(Result.of(CSignal.Macro.SIG_IGN.pointer()),
			Result.errno(CErrNo.EINVAL));
		Assert.equal(CSignal.SIGUSR1.setIgnore().macro(), CSignal.Macro.SIG_IGN);
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.SIGUSR1.setIgnore());
	}

	@Test
	public void testRaise() throws CException {
		captor.reset();
		CSignal.SIGUSR1.setIgnore();
		CSignal.SIGUSR1.raise(); // ignored
		CSignal.SIGUSR1.set(handler);
		CSignal.SIGUSR1.raise();
		captor.verify(CSignal.SIGUSR1);
		CSignal.SIGUSR1.setDefault();
	}

	@Test
	public void testRaiseWithErrors() throws CException {
		lib.init().raise.autoResponses(null, CErrNo.EINVAL);
		CSignal.SIGUSR1.raise();
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.SIGUSR2.raise());
		lib.lib().raise.assertValues(CSignal.SIGUSR1.value, CSignal.SIGUSR2.value);
	}

	@Test
	public void testSigSet() throws CException {
		var set = CSignal.sigset();
		CSignal.SIGUSR1.add(set);
		CSignal.SIGUSR2.add(set);
		Assert.equal(CSignal.SIGUSR1.isMember(set), true);
		Assert.equal(CSignal.SIGUSR2.isMember(set), true);
		Assert.equal(CSignal.SIGABRT.isMember(set), false);
		CSignal.SIGUSR2.delete(set);
		Assert.equal(CSignal.SIGUSR1.isMember(set), true);
		Assert.equal(CSignal.SIGUSR2.isMember(set), false);
		Assert.equal(CSignal.SIGABRT.isMember(set), false);
	}

	@Test
	public void testNullSigSet() throws CException {
		Assert.equal(CSignal.sigset((SegmentAllocator) null), null);
		Assert.equal(CSignal.sigset((Iterable<CSignal>) null), null);
		Assert.equal(CSignal.SIGABRT.isMember(CSignal.sigset(CSignal.SIGABRT, null)), true);
		Assert.equal(CSignal.sigemptyset((Pointer<sigset_t>) null), null);
		Assert.equal(CSignal.SIGUSR2.add(null), null);
		Assert.equal(CSignal.SIGUSR2.delete(null), null);
		Assert.equal(CSignal.SIGUSR2.isMember(null), false);
	}

	@Test
	public void testSigSetErrors() throws CException {
		var set = sigset_t.$.pointer();
		CSignal.sigemptyset(set);
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigaddset(set, -1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigdelset(set, -1));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigismember(set, -1));
	}

	@Test
	public void testSigSetEmulated() throws CException {
		lib.init();
		var set = CSignal.sigset();
		CSignal.SIGUSR1.add(set);
		CSignal.SIGUSR2.add(set);
		Assert.equal(CSignal.SIGUSR1.isMember(set), true);
		Assert.equal(CSignal.SIGUSR2.isMember(set), true);
		Assert.equal(CSignal.SIGABRT.isMember(set), false);
		CSignal.SIGUSR2.delete(set);
		Assert.equal(CSignal.SIGUSR1.isMember(set), true);
		Assert.equal(CSignal.SIGUSR2.isMember(set), false);
		Assert.equal(CSignal.SIGABRT.isMember(set), false);
	}

	@Test
	public void testSigSetEmulatedErrors() {
		lib.init().sigset.autoResponses(CErrNo.EINVAL);
		var set = sigset_t.$.pointer();
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.sigemptyset(set));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.SIGUSR1.add(set));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.SIGUSR1.delete(set));
		FfmAssert.cexception(CErrNo.EINVAL, () -> CSignal.SIGUSR1.isMember(set));
	}

	@Test
	public void testOsCoverage() {
		FfmTesting.testForEachOs(CSignal.class);
	}
}
