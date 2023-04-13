package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.List;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.jna.clib.jna.CSignal.sighandler_t;
import ceri.jna.clib.test.TestCLibNative;

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
			lib.signal.assertValues(List.of(15, cb), List.of(15, new Pointer(1)), List.of(14, cb),
				List.of(14, new Pointer(0)));
			CSignal.raise(15);
			lib.raise.assertAuto(15);
		});
	}

}
