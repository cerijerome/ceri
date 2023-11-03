package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.jna.clib.jna.CSignal.sighandler_t;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.SignalArgs;
import ceri.jna.test.JnaTestUtil;

public class CSignalTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CSignal.class);
	}

	@Test
	public void testSignal() throws CException {
		sighandler_t cb = i -> {};
		TestCLibNative.exec(lib -> {
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
		});
	}

	@Test
	public void testFields() {
		JnaTestUtil.testForEachOs(CSignal.class);
	}
}
