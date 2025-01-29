package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.test.CallSync;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.SignalArgs;
import ceri.log.util.LogUtil;

public class SignalBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, TestCLibNative> enc;

	@After
	public void after() {
		CloseableUtil.close(enc);
		enc = null;
		lib = null;
	}

	@Test
	public void shouldSetStandardHandler() throws IOException {
		initLib();
		Signal.SIGKILL.signalDefault();
		lib.signal.assertAuto(new SignalArgs(CSignal.SIGKILL, new Pointer(0)));
		Signal.SIGKILL.signalIgnore();
		lib.signal.assertAuto(new SignalArgs(CSignal.SIGKILL, new Pointer(1)));
	}

	@Test
	public void shouldSetHandler() throws IOException {
		var sync = CallSync.<Signal>consumer(null, true);
		try (var _ = Signal.SIGUSR1.signal(sync)) {
			LogUtil.runSilently(Signal.SIGUSR1::raise);
			sync.assertAuto(Signal.SIGUSR1);
		}
	}

	@Test
	public void shouldFailToSetHandler() {
		initLib();
		lib.signal.autoResponses(new Pointer(CSignal.SIG_ERR));
		assertThrown(IOException.class, () -> Signal.SIGUSR1.signal(_ -> {}));
	}

	private void initLib() {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
	}
}
