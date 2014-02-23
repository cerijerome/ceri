package ceri.x10.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class CommandDispatcherBehavior {

	@Test
	public void shouldDispatchCommandsToListener() {
		CommandListener listener = mock(CommandListener.class);
		CommandDispatcher.dispatch(CommandFactory.allUnitsOff('C'), listener);
		verify(listener).allUnitsOff(CommandFactory.allUnitsOff('C'));
		CommandDispatcher.dispatch(CommandFactory.allLightsOff('D'), listener);
		verify(listener).allLightsOff(CommandFactory.allLightsOff('D'));
		CommandDispatcher.dispatch(CommandFactory.allLightsOn('E'), listener);
		verify(listener).allLightsOn(CommandFactory.allLightsOn('E'));
		CommandDispatcher.dispatch(CommandFactory.off("F13"), listener);
		verify(listener).off(CommandFactory.off("F13"));
		CommandDispatcher.dispatch(CommandFactory.dim("G6", 99), listener);
		verify(listener).dim(CommandFactory.dim("G6", 99));
		CommandDispatcher.dispatch(CommandFactory.bright("G6", 99), listener);
		verify(listener).bright(CommandFactory.bright("G6", 99));
		CommandDispatcher.dispatch(CommandFactory.extended("G6", 1, -1), listener);
		verify(listener).extended(CommandFactory.extended("G6", 1, -1));
	}


}
