package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.junit.Test;
import ceri.jna.clib.jna.CSignal;

public class SigSetBehavior {

	@Test
	public void shouldRemoveSignal() throws IOException {
		var ss = SigSet.of(Signal.SIGABRT, Signal.SIGALRM, Signal.SIGBUS);
		ss.remove(Signal.SIGBUS, Signal.SIGALRM);
		assertTrue(ss.has(Signal.SIGABRT));
		assertFalse(ss.has(Signal.SIGALRM));
	}

	@Test
	public void shouldProvideStruct() throws IOException {
		assertNull(SigSet.struct(null));
		var ss = SigSet.struct(SigSet.of(Signal.SIGABRT));
		assertTrue(CSignal.sigismember(ss, Signal.SIGABRT.signal));
	}

}
