package ceri.x10.cm17a.device;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.TestListener;
import ceri.x10.command.Command;
import ceri.x10.command.TestCommandListener;

public class Cm17aEmulatorBehavior {

	@Test
	public void shouldDispatchCommands() throws IOException, InterruptedException {
		TestCommandListener listener = TestCommandListener.of();
		try (var emu = Cm17aEmulator.of(0)) {
			try (var enc = emu.listen(listener)) {
				emu.command(Command.from("I[1,3,5]:dim:30%"));
				listener.sync.await(Command.from("I[1,3,5]:dim:30%"));
			}
		}
	}

	@Test
	public void shouldNotifyOfStateChange() throws InterruptedException {
		try (var emu = Cm17aEmulator.of(0)) {
			try (TestListener<?> listener = TestListener.of(emu.listeners())) {
				emu.listeners.accept(StateChange.broken);
				assertThat(listener.await(), is(StateChange.broken));
			}
		}
	}

}
