package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.x10.command.FunctionType.allLightsOff;
import static ceri.x10.command.FunctionType.allLightsOn;
import static ceri.x10.command.FunctionType.allUnitsOff;
import static ceri.x10.command.FunctionType.bright;
import static ceri.x10.command.FunctionType.dim;
import static ceri.x10.command.FunctionType.hailReq;
import static ceri.x10.command.FunctionType.off;
import static ceri.x10.command.FunctionType.on;
import static ceri.x10.command.House.D;
import static ceri.x10.command.House.E;
import static ceri.x10.command.House.F;
import static ceri.x10.command.House.G;
import static ceri.x10.command.House.H;
import static ceri.x10.command.House.I;
import static ceri.x10.command.House.J;
import static ceri.x10.command.House.K;
import static ceri.x10.command.House.L;
import static ceri.x10.command.House.M;
import static ceri.x10.command.House.N;
import static ceri.x10.command.House.P;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._10;
import static ceri.x10.command.Unit._2;
import static ceri.x10.command.Unit._3;
import static ceri.x10.command.Unit._4;
import static ceri.x10.command.Unit._5;
import static ceri.x10.command.Unit._6;
import static ceri.x10.command.Unit._8;
import java.util.Set;
import org.junit.Test;
import ceri.x10.command.Command;
import ceri.x10.command.UnsupportedCommand;

public class EntryBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Entry t = Entry.ext(N, 10, 20);
		Entry eq0 = Entry.ext(N, 10, 20);
		Entry ne0 = Entry.ext(M, 10, 20);
		Entry ne1 = Entry.ext(N, 11, 20);
		Entry ne2 = Entry.ext(N, 10, 21);
		Entry ne3 = Entry.dim(N, dim, 10);
		Entry ne4 = Entry.function(N, on);
		Entry ne5 = Entry.address(N, _10);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldCreateEntriesFromCommand() {
		var entries = Entry.allFrom(Command.dim(L, 13, _1, _2, _3));
		assertIterable(entries, Entry.address(L, _1), Entry.address(L, _2), Entry.address(L, _3),
			Entry.dim(L, dim, 13));
	}

	@Test
	public void shouldProvideEmptyEntryListForNoOpcommand() {
		assertIterable(Entry.allFrom(Command.on(P)));
	}

	@Test
	public void shouldFailToConvertUnsupportedCommandToEntries() {
		assertThrown(() -> Entry.allFrom(UnsupportedCommand.hailReq(D, _8)));
	}

	@Test
	public void shouldFailToCreateCommandFromIncompleteFunctionEntry() {
		assertNull(Entry.address(F, _1).command(F, Set.of(_1)));
		assertNull(Entry.function(H, on).command(F, Set.of(_1)));
		assertNull(Entry.function(H, hailReq).command(H, Set.of(_1)));
		assertNull(Entry.function(H, on).command(null, Set.of(_1)));
		assertNull(Entry.function(H, on).command(H, Set.of()));
		assertNull(Entry.dim(H, dim, 44).command(H, Set.of()));
		assertNull(Entry.ext(H, 66, 88).command(I, Set.of(_1)));
	}

	@Test
	public void shouldCreateCommandFromAddressAndUnits() {
		assertEquals(Entry.function(K, allUnitsOff).command(null, null), Command.allUnitsOff(K));
		assertEquals(Entry.function(D, allLightsOff).command(null, Set.of()),
			Command.allLightsOff(D));
		assertEquals(Entry.function(G, allLightsOn).command(F, Set.of(_1)), Command.allLightsOn(G));
		assertEquals(Entry.function(H, on).command(H, Set.of(_1, _2)), Command.on(H, _1, _2));
		assertEquals(Entry.function(I, off).command(I, Set.of(_4)), Command.off(I, _4));
		assertEquals(Entry.dim(J, dim, 11).command(J, Set.of(_5, _6)), Command.dim(J, 11, _5, _6));
		assertEquals(Entry.dim(J, bright, 33).command(J, Set.of(_5, _6)),
			Command.bright(J, 33, _5, _6));
		assertEquals(Entry.ext(E, 50, 60).command(E, Set.of(_8)), Command.ext(E, 50, 60, _8));
	}

}
