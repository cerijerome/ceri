package ceri.x10.cm11a.protocol;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOrdered;
import java.util.Set;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;
import ceri.x10.command.UnsupportedCommand;

public class EntryBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Entry t = Entry.ext(House.N, 10, 20);
		Entry eq0 = Entry.ext(House.N, 10, 20);
		Entry ne0 = Entry.ext(House.M, 10, 20);
		Entry ne1 = Entry.ext(House.N, 11, 20);
		Entry ne2 = Entry.ext(House.N, 10, 21);
		Entry ne3 = Entry.dim(House.N, FunctionType.dim, 10);
		Entry ne4 = Entry.function(House.N, FunctionType.on);
		Entry ne5 = Entry.address(House.N, Unit._10);
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldCreateEntriesFromCommand() {
		var entries = Entry.allFrom(Command.dim(House.L, 13, Unit._1, Unit._2, Unit._3));
		assertOrdered(entries, Entry.address(House.L, Unit._1), Entry.address(House.L, Unit._2),
			Entry.address(House.L, Unit._3), Entry.dim(House.L, FunctionType.dim, 13));
	}

	@Test
	public void shouldProvideEmptyEntryListForNoOpcommand() {
		assertOrdered(Entry.allFrom(Command.on(House.P)));
	}

	@Test
	public void shouldFailToConvertUnsupportedCommandToEntries() {
		Assert.thrown(() -> Entry.allFrom(UnsupportedCommand.hailReq(House.D, Unit._8)));
	}

	@Test
	public void shouldFailToCreateCommandFromIncompleteFunctionEntry() {
		Assert.isNull(Entry.address(House.F, Unit._1).command(House.F, Set.of(Unit._1)));
		Assert.isNull(Entry.function(House.H, FunctionType.on).command(House.F, Set.of(Unit._1)));
		Assert.isNull(Entry.function(House.H, FunctionType.hailReq).command(House.H, Set.of(Unit._1)));
		Assert.isNull(Entry.function(House.H, FunctionType.on).command(null, Set.of(Unit._1)));
		Assert.isNull(Entry.function(House.H, FunctionType.on).command(House.H, Set.of()));
		Assert.isNull(Entry.dim(House.H, FunctionType.dim, 44).command(House.H, Set.of()));
		Assert.isNull(Entry.ext(House.H, 66, 88).command(House.I, Set.of(Unit._1)));
	}

	@Test
	public void shouldCreateCommandFromAddressAndUnits() {
		assertEquals(Entry.function(House.K, FunctionType.allUnitsOff).command(null, null),
			Command.allUnitsOff(House.K));
		assertEquals(Entry.function(House.D, FunctionType.allLightsOff).command(null, Set.of()),
			Command.allLightsOff(House.D));
		assertEquals(
			Entry.function(House.G, FunctionType.allLightsOn).command(House.F, Set.of(Unit._1)),
			Command.allLightsOn(House.G));
		assertEquals(
			Entry.function(House.H, FunctionType.on).command(House.H, Set.of(Unit._1, Unit._2)),
			Command.on(House.H, Unit._1, Unit._2));
		assertEquals(Entry.function(House.I, FunctionType.off).command(House.I, Set.of(Unit._4)),
			Command.off(House.I, Unit._4));
		assertEquals(
			Entry.dim(House.J, FunctionType.dim, 11).command(House.J, Set.of(Unit._5, Unit._6)),
			Command.dim(House.J, 11, Unit._5, Unit._6));
		assertEquals(
			Entry.dim(House.J, FunctionType.bright, 33).command(House.J, Set.of(Unit._5, Unit._6)),
			Command.bright(House.J, 33, Unit._5, Unit._6));
		assertEquals(Entry.ext(House.E, 50, 60).command(House.E, Set.of(Unit._8)),
			Command.ext(House.E, 50, 60, Unit._8));
	}
}
