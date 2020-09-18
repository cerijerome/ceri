package ceri.x10.command;

import static ceri.x10.command.House.C;
import static ceri.x10.command.House.D;
import static ceri.x10.command.House.E;
import static ceri.x10.util.X10TestUtil.addr;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class CommandListenerBehavior {
	private static CommandListener listener;
	
	@BeforeClass
	public static void beforeClass() {
		listener = mock(CommandListener.class);
	}
	
	@Before
	public void before() {
		Mockito.reset(listener);
	}
	
	@Test
	public void shouldDispatchAllUnitsOff() {
		CommandListener.dispatcher(Command.allUnitsOff(C)).accept(listener);
		verify(listener).allUnitsOff(Command.allUnitsOff(C));
	}

	@Test
	public void shouldDispatchAllLightsOff() {
		CommandListener.dispatcher(Command.allLightsOff(D)).accept(listener);
		verify(listener).allLightsOff(Command.allLightsOff(D));
	}

	@Test
	public void shouldDispatchAllLightsOn() {
		CommandListener.dispatcher(Command.allLightsOn(E)).accept(listener);
		verify(listener).allLightsOn(Command.allLightsOn(E));
	}

	@Test
	public void shouldDispatchOff() {
		CommandListener.dispatcher(Command.off(Address.from("F13"))).accept(listener);
		verify(listener).off(Command.off(addr("F13")));
	}

	@Test
	public void shouldDispatchOn() {
		CommandListener.dispatcher(Command.on(Address.from("H14"))).accept(listener);
		verify(listener).on(Command.on(addr("H14")));
	}

	@Test
	public void shouldDispatchDim() {
		CommandListener.dispatcher(Command.dim(addr("E3"), 99)).accept(listener);
		verify(listener).dim(Command.dim(addr("E3"), 99));
	}

	@Test
	public void shouldDispatchBright() {
		CommandListener.dispatcher(Command.bright(addr("N12"), 8)).accept(listener);
		verify(listener).bright(Command.bright(addr("N12"), 8));
	}

	@Test
	public void shouldDispatchExt() {
		CommandListener.dispatcher(Command.ext(addr("O10"), 0xee, 0xaa)).accept(listener);
		verify(listener).ext(Command.ext(addr("O10"), 0xee, 0xaa));
	}

}
