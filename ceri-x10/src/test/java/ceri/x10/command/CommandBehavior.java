package ceri.x10.command;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class CommandBehavior {
	private static final Command allUnitsOff = Command.allUnitsOff(House.A);
	private static final Command allLightsOff = Command.allLightsOff(House.B);
	private static final Command allLightsOn = Command.allLightsOn(House.C);
	private static final Command on = Command.on(Address.from("D1"));
	private static final Command off = Command.off(Address.from("E3"));
	private static final Command.Dim dim = Command.dim(Address.from("F5"), 50);
	private static final Command.Dim bright = Command.bright(Address.from("G7"), 10);
	private static final Command.Ext ext = Command.ext(Address.from("H9"), 33, 55);
	private static ValueCondition<Command> sync = ValueCondition.of();
	private static Command.Listener listener = listener(sync);

	@Before
	public void before() {
		sync.clear();
	}

	@Test
	public void shouldProvideNoOpDefaults() {
		var listener = new Command.Listener() {};
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
		Assert.equal(sync.await(), off);
	}

	@Test
	public void shouldFailToDispatchUnsupportedCommand() {
		Command unsupported = UnsupportedCommand.hailReq(House.I, Unit._11);
		Assert.thrown(() -> Command.Listener.dispatcher(unsupported));
	}

	@Test
	public void shouldDispatchAllUnitsOff() throws InterruptedException {
		Command.Listener.dispatcher(allUnitsOff).accept(listener);
		Assert.equal(sync.await(), allUnitsOff);
	}

	@Test
	public void shouldDispatchAllLightsOff() throws InterruptedException {
		Command.Listener.dispatcher(allLightsOff).accept(listener);
		Assert.equal(sync.await(), allLightsOff);
	}

	@Test
	public void shouldDispatchAllLightsOn() throws InterruptedException {
		Command.Listener.dispatcher(allLightsOn).accept(listener);
		Assert.equal(sync.await(), allLightsOn);
	}

	@Test
	public void shouldDispatchOff() throws InterruptedException {
		Command.Listener.dispatcher(off).accept(listener);
		Assert.equal(sync.await(), off);
	}

	@Test
	public void shouldDispatchOn() throws InterruptedException {
		Command.Listener.dispatcher(on).accept(listener);
		Assert.equal(sync.await(), on);
	}

	@Test
	public void shouldDispatchDim() throws InterruptedException {
		Command.Listener.dispatcher(dim).accept(listener);
		Assert.equal(sync.await(), dim);
	}

	@Test
	public void shouldDispatchBright() throws InterruptedException {
		Command.Listener.dispatcher(bright).accept(listener);
		Assert.equal(sync.await(), bright);
	}

	@Test
	public void shouldDispatchExt() throws InterruptedException {
		Command.Listener.dispatcher(ext).accept(listener);
		Assert.equal(sync.await(), ext);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		Command t = Command.ext(House.O, 10, 20, Unit._11, Unit._12);
		Command eq0 = Command.ext(House.O, 10, 20, Unit._11, Unit._12);
		Command ne0 = Command.ext(House.P, 10, 20, Unit._11, Unit._12);
		Command ne1 = Command.ext(House.O, 11, 20, Unit._11, Unit._12);
		Command ne2 = Command.ext(House.O, 10, 21, Unit._11, Unit._12);
		Command ne3 = Command.ext(House.O, 10, 20, Unit._12);
		Command ne4 = Command.ext(House.O, 10, 20, Unit._11, Unit._13);
		Command ne5 = Command.ext(House.O, 10, 20, (Collection<Unit>) null);
		Command ne6 = Command.bright(House.O, 10, Unit._11, Unit._12);
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldCreateFromString() {
		Assert.equal(Command.from("A[1,2]:on"), Command.on(House.A, Unit._1, Unit._2));
		Assert.equal(Command.from("B[]:on"), Command.on(House.B));
		Assert.equal(Command.from("C[ ]:off"), Command.off(House.C));
		Assert.equal(Command.from("D:on"), Command.on(House.D));
		Assert.equal(Command.from("E[8]:bright:50"), Command.bright(House.E, 50, Unit._8));
		Assert.equal(Command.from("F:allLightsOn"), Command.allLightsOn(House.F));
		Assert.equal(Command.from("G:allLightsOff"), Command.allLightsOff(House.G));
		Assert.equal(Command.from("H:allUnitsOff"), Command.allUnitsOff(House.H));
		Assert.equal(Command.from("I[3,4]:ext:100:200"),
			Command.ext(House.I, 100, 200, Unit._3, Unit._4));
	}

	@Test
	public void shouldFailToCreateFromBadString() {
		Assert.thrown(() -> Command.from(null));
		Assert.thrown(() -> Command.from(""));
		Assert.thrown(() -> Command.from("A[1,2]:"));
		Assert.thrown(() -> Command.from(":on"));
		Assert.thrown(() -> Command.from("A:xx"));
	}

	@Test
	public void shouldFailToCreateFromUnsupportedString() {
		Assert.thrown(() -> Command.from("A[1,2]:hailReq"));
	}

	@Test
	public void shouldDetermineIfNoOpCommand() {
		Assert.no(Command.allLightsOn(House.H).isNoOp());
		Assert.yes(Command.on(House.H).isNoOp());
		Assert.yes(Command.dim(House.H, 0, Unit._1).isNoOp());
	}

	@Test
	public void shouldCheckFunctionGroup() {
		Assert.yes(Command.on(House.H, Unit._1).isGroup(FunctionGroup.unit));
		Assert.no(Command.on(House.H, Unit._1).isGroup(FunctionGroup.house));
	}

	private static Command.Listener listener(ValueCondition<Command> sync) {
		return new Command.Listener() {
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
