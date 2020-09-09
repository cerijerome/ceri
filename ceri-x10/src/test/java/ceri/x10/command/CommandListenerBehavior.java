package ceri.x10.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class CommandListenerBehavior {

	@Test
	public void shouldDispatchCommandsToListener() {
		CommandListener listener = mock(CommandListener.class);
		CommandListener.dispatcher(CommandFactory.allUnitsOff('C')).accept(listener);
		verify(listener).allUnitsOff(CommandFactory.allUnitsOff('C'));
		CommandListener.dispatcher(CommandFactory.allLightsOff('D')).accept(listener);
		verify(listener).allLightsOff(CommandFactory.allLightsOff('D'));
		CommandListener.dispatcher(CommandFactory.allLightsOn('E')).accept(listener);
		verify(listener).allLightsOn(CommandFactory.allLightsOn('E'));
		CommandListener.dispatcher(CommandFactory.off("F13")).accept(listener);
		verify(listener).off(CommandFactory.off("F13"));
		CommandListener.dispatcher(CommandFactory.dim("G6", 99)).accept(listener);
		verify(listener).dim(CommandFactory.dim("G6", 99));
		CommandListener.dispatcher(CommandFactory.bright("G6", 99)).accept(listener);
		verify(listener).bright(CommandFactory.bright("G6", 99));
		CommandListener.dispatcher(CommandFactory.extended("G6", 1, 0xff)).accept(listener);
		verify(listener).extended(CommandFactory.extended("G6", 1, 0xff));
	}

}
