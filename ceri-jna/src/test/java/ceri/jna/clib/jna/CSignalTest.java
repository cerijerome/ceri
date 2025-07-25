package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.jna.test.JnaTestUtil.assertCException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteUtil;
import ceri.common.util.CloseableUtil;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.jna.CSignal.sighandler_t;
import ceri.jna.clib.jna.CSignal.sigset_t;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.SignalArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaLibrary;

public class CSignalTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();

	@After
	public void after() {
		CloseableUtil.close(ref);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CSignal.class);
	}

	@Test
	public void testSignal() throws CException {
		var lib = ref.init();
		sighandler_t cb = _ -> {};
		assertTrue(CSignal.signal(15, cb));
		assertTrue(CSignal.signal(15, 1));
		assertThrown(() -> CSignal.signal(15, -1));
		assertThrown(() -> CSignal.signal(15, 2));
		lib.signal.autoResponses(new Pointer(-1));
		assertFalse(CSignal.signal(14, cb));
		assertFalse(CSignal.signal(14, 0));
		lib.signal.assertValues(new SignalArgs(15, cb), new SignalArgs(15, new Pointer(1)),
			new SignalArgs(14, cb), new SignalArgs(14, new Pointer(0)));
		CSignal.raise(15);
		lib.raise.assertAuto(15);
	}

	@Test
	public void testSigSet() throws CException {
		var lib = ref.init();
		// clear all
		var sigset = CSignal.sigemptyset(new sigset_t());
		lib.sigset.assertAuto(0);
		// set SIGINT
		CSignal.sigaddset(sigset, CSignal.SIGINT);
		lib.sigset.assertAuto(ByteUtil.maskOfBitsInt(CSignal.SIGINT));
		assertTrue(CSignal.sigismember(sigset, CSignal.SIGINT));
		// set SIGABRT
		CSignal.sigaddset(sigset, CSignal.SIGABRT);
		lib.sigset.assertAuto(ByteUtil.maskOfBitsInt(CSignal.SIGINT, CSignal.SIGABRT));
		// unset SIGINT
		CSignal.sigdelset(sigset, CSignal.SIGINT);
		lib.sigset.assertAuto(ByteUtil.maskOfBitsInt(CSignal.SIGABRT));
		assertFalse(CSignal.sigismember(sigset, CSignal.SIGINT));
	}

	@Test
	public void testSigSetWithError() throws CException {
		var lib = ref.init();
		var sigset = CSignal.sigemptyset(new sigset_t());
		lib.sigset.autoResponses(-1, 0);
		assertCException(() -> CSignal.sigismember(sigset, CSignal.SIGINT));
		lib.sigset.error.setFrom(ErrNo.EACCES::lastError);
		assertCException(() -> CSignal.sigismember(sigset, CSignal.SIGINT));
	}

	@Test
	public void testSigsetStringRepresentation() {
		assertMatch(new sigset_t(), "(?s)sigset_t\\(@.*\\+\\d+\\).*");
	}

	@Test
	public void testFields() {
		JnaTestUtil.testForEachOs(CSignal.class);
	}
}
