package ceri.x10.cm11a.device;

import static ceri.common.test.Assert.assertEquals;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.StateChange;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.TestCommandListener;
import ceri.x10.command.Unit;

public class Cm11aEmulatorBehavior {
	private Cm11aEmulator cm11a;

	@Before
	public void beforeClass() {
		cm11a = Cm11aEmulator.of(1);
	}

	@After
	public void afterClass() {
		cm11a.close();
	}

	@Test
	public void shouldListenForConnectionChanges() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = cm11a.listeners().enclose(sync::signal)) {
			cm11a.listeners.accept(StateChange.broken);
			assertEquals(sync.await(), StateChange.broken);
		}
	}

	@Test
	public void shouldDispatchCommands() throws InterruptedException, IOException {
		TestCommandListener listener = TestCommandListener.of();
		try (var _ = cm11a.listen(listener)) {
			cm11a.command(Command.bright(House.B, 42, Unit._6, Unit._7));
			listener.sync.await(Command.bright(House.B, 42, Unit._6, Unit._7));
		}
	}
}
