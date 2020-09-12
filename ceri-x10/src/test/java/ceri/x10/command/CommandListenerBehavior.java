package ceri.x10.command;

import static ceri.x10.command.House.C;
import static ceri.x10.command.House.D;
import static ceri.x10.command.House.E;
import static ceri.x10.util.X10TestUtil.addr;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class CommandListenerBehavior {

	@Test
	public void shouldDispatchCommandsToListener() {
		CommandListener listener = mock(CommandListener.class);
		CommandListener.dispatcher(Command.allUnitsOff(C)).accept(listener);
		verify(listener).allUnitsOff(Command.allUnitsOff(C));
		CommandListener.dispatcher(Command.allLightsOff(D)).accept(listener);
		verify(listener).allLightsOff(Command.allLightsOff(D));
		CommandListener.dispatcher(Command.allLightsOn(E)).accept(listener);
		verify(listener).allLightsOn(Command.allLightsOn(E));
		CommandListener.dispatcher(Command.off(Address.from("F13"))).accept(listener);
		verify(listener).off(Command.off(addr("F13")));
		CommandListener.dispatcher(Command.dim(addr("G6"), 99)).accept(listener);
		verify(listener).dim(Command.dim(addr("G6"), 99));
		CommandListener.dispatcher(Command.bright(addr("G6"), 99)).accept(listener);
		verify(listener).bright(Command.bright(addr("G6"), 99));
		CommandListener.dispatcher(Command.ext(addr("G6"), 1, 0xff)).accept(listener);
		verify(listener).extended(Command.ext(addr("G6"), 1, 0xff));
	}

}
