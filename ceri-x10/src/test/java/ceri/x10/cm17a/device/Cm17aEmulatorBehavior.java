package ceri.x10.cm17a.device;

import java.io.IOException;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.x10.command.Command;
import ceri.x10.command.TestCommandListener;

public class Cm17aEmulatorBehavior {

	@Test
	public void shouldDispatchCommands() throws IOException, InterruptedException {
		TestCommandListener listener = TestCommandListener.of();
		try (var emu = Cm17aEmulator.of(0)) {
			try (var _ = emu.listen(listener)) {
				emu.command(Command.from("I[1,3,5]:dim:30%"));
				listener.sync.await(Command.from("I[1,3,5]:dim:30%"));
			}
		}
	}

	@Test
	public void shouldNotifyOfStateChange() {
		try (var emu = Cm17aEmulator.of(0)) {
			CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
			try (var _ = emu.listeners().enclose(sync::accept)) {
				emu.listeners.accept(StateChange.broken);
				sync.assertCall(StateChange.broken);
			}
		}
	}

}
