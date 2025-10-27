package ceri.jna.clib;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.clib.jna.CSignal;

public class SigSetBehavior {

	@Test
	public void shouldRemoveSignal() throws IOException {
		var ss = SigSet.of(Signal.SIGABRT, Signal.SIGALRM, Signal.SIGBUS);
		ss.remove(Signal.SIGBUS, Signal.SIGALRM);
		Assert.yes(ss.has(Signal.SIGABRT));
		Assert.no(ss.has(Signal.SIGALRM));
	}

	@Test
	public void shouldProvideStruct() throws IOException {
		Assert.isNull(SigSet.struct(null));
		var ss = SigSet.struct(SigSet.of(Signal.SIGABRT));
		Assert.yes(CSignal.sigismember(ss, Signal.SIGABRT.signal));
	}

}
