package ceri.jna.clib;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.SignalArgs;
import ceri.jna.util.JnaLibrary;
import ceri.log.util.LogUtil;

public class SignalBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();

	@After
	public void after() {
		Closeables.close(ref);
	}

	@Test
	public void shouldSetStandardHandler() throws IOException {
		var lib = ref.init();
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
		var lib = ref.init();
		lib.signal.autoResponses(new Pointer(CSignal.SIG_ERR));
		Assert.io(() -> Signal.SIGUSR1.signal(_ -> {}));
	}
}
