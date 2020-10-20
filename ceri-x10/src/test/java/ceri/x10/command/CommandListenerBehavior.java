package ceri.x10.command;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.B;
import static ceri.x10.command.House.C;
import static ceri.x10.command.House.I;
import static ceri.x10.command.Unit._11;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;

public class CommandListenerBehavior {
	private static final Command allUnitsOff = Command.allUnitsOff(A);
	private static final Command allLightsOff = Command.allLightsOff(B);
	private static final Command allLightsOn = Command.allLightsOn(C);
	private static final Command on = Command.on(Address.from("D1"));
	private static final Command off = Command.off(Address.from("E3"));
	private static final Command.Dim dim = Command.dim(Address.from("F5"), 50);
	private static final Command.Dim bright = Command.bright(Address.from("G7"), 10);
	private static final Command.Ext ext = Command.ext(Address.from("H9"), 33, 55);
	private static ValueCondition<Command> sync = ValueCondition.of();
	private static CommandListener listener = listener(sync);

	@Before
	public void before() {
		sync.clear();
	}

	@Test
	public void shouldProvideNoOpDefaults() {
		CommandListener listener = new CommandListener() {};
		listener.allUnitsOff(allUnitsOff);
		listener.allLightsOff(allLightsOff);
		listener.allLightsOn(allLightsOn);
		listener.on(on);
		listener.off(off);
		listener.dim(dim);
		listener.bright(bright);
		listener.ext(ext);
	}

	@Test
	public void shouldProvideCommandConsumer() throws InterruptedException {
		listener.asConsumer().accept(off);
		assertEquals(sync.await(), off);
	}

	@Test
	public void shouldFailToDispatchUnsupportedCommand() {
		Command unsupported = UnsupportedCommand.hailReq(I, _11);
		assertThrown(() -> CommandListener.dispatcher(unsupported));
	}

	@Test
	public void shouldDispatchAllUnitsOff() throws InterruptedException {
		CommandListener.dispatcher(allUnitsOff).accept(listener);
		assertEquals(sync.await(), allUnitsOff);
	}

	@Test
	public void shouldDispatchAllLightsOff() throws InterruptedException {
		CommandListener.dispatcher(allLightsOff).accept(listener);
		assertEquals(sync.await(), allLightsOff);
	}

	@Test
	public void shouldDispatchAllLightsOn() throws InterruptedException {
		CommandListener.dispatcher(allLightsOn).accept(listener);
		assertEquals(sync.await(), allLightsOn);
	}

	@Test
	public void shouldDispatchOff() throws InterruptedException {
		CommandListener.dispatcher(off).accept(listener);
		assertEquals(sync.await(), off);
	}

	@Test
	public void shouldDispatchOn() throws InterruptedException {
		CommandListener.dispatcher(on).accept(listener);
		assertEquals(sync.await(), on);
	}

	@Test
	public void shouldDispatchDim() throws InterruptedException {
		CommandListener.dispatcher(dim).accept(listener);
		assertEquals(sync.await(), dim);
	}

	@Test
	public void shouldDispatchBright() throws InterruptedException {
		CommandListener.dispatcher(bright).accept(listener);
		assertEquals(sync.await(), bright);
	}

	@Test
	public void shouldDispatchExt() throws InterruptedException {
		CommandListener.dispatcher(ext).accept(listener);
		assertEquals(sync.await(), ext);
	}

	private static CommandListener listener(ValueCondition<Command> sync) {
		return new CommandListener() {
			@Override
			public void allUnitsOff(Command command) {
				sync.signal(command);
			}

			@Override
			public void allLightsOff(Command command) {
				sync.signal(command);
			}

			@Override
			public void allLightsOn(Command command) {
				sync.signal(command);
			}

			@Override
			public void off(Command command) {
				sync.signal(command);
			}

			@Override
			public void on(Command command) {
				sync.signal(command);
			}

			@Override
			public void dim(Command.Dim command) {
				sync.signal(command);
			}

			@Override
			public void bright(Command.Dim command) {
				sync.signal(command);
			}

			@Override
			public void ext(Command.Ext command) {
				sync.signal(command);
			}
		};
	}
}
