package ceri.x10.command;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.B;
import static ceri.x10.command.House.C;
import static ceri.x10.command.House.D;
import static ceri.x10.command.House.E;
import static ceri.x10.command.House.F;
import static ceri.x10.command.House.G;
import static ceri.x10.command.House.H;
import static ceri.x10.command.House.O;
import static ceri.x10.command.House.P;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._11;
import static ceri.x10.command.Unit._12;
import static ceri.x10.command.Unit._13;
import static ceri.x10.command.Unit._2;
import static ceri.x10.command.Unit._3;
import static ceri.x10.command.Unit._4;
import java.util.Collection;
import org.junit.Test;

public class CommandBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Command t = Command.ext(O, 10, 20, _11, _12);
		Command eq0 = Command.ext(O, 10, 20, _11, _12);
		Command ne0 = Command.ext(P, 10, 20, _11, _12);
		Command ne1 = Command.ext(O, 11, 20, _11, _12);
		Command ne2 = Command.ext(O, 10, 21, _11, _12);
		Command ne3 = Command.ext(O, 10, 20, _12);
		Command ne4 = Command.ext(O, 10, 20, _11, _13);
		Command ne5 = Command.ext(O, 10, 20, (Collection<Unit>) null);
		Command ne6 = Command.bright(O, 10, _11, _12);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldCreateFromString() {
		assertEquals(Command.from("A[1,2]:on"), Command.on(A, _1, _2));
		assertEquals(Command.from("B[]:on"), Command.on(B));
		assertEquals(Command.from("C[ ]:on"), Command.on(C));
		assertEquals(Command.from("D:on"), Command.on(D));
		assertEquals(Command.from("E:allLightsOn"), Command.allLightsOn(E));
		assertEquals(Command.from("F:allLightsOff"), Command.allLightsOff(F));
		assertEquals(Command.from("G:allUnitsOff"), Command.allUnitsOff(G));
		assertEquals(Command.from("H[3,4]:ext:100:200"), Command.ext(H, 100, 200, _3, _4));
	}

	@Test
	public void shouldFailToCreateFromBadString() {
		assertThrown(() -> Command.from(null));
		assertThrown(() -> Command.from(""));
		assertThrown(() -> Command.from("A[1,2]:"));
		assertThrown(() -> Command.from(":on"));
		assertThrown(() -> Command.from("A:xx"));
	}

	@Test
	public void shouldFailToCreateFromUnsupportedString() {
		assertThrown(() -> Command.from("A[1,2]:hailReq"));
	}

	@Test
	public void shouldDetermineIfNoOpCommand() {
		assertFalse(Command.allLightsOn(H).isNoOp());
		assertTrue(Command.on(H).isNoOp());
		assertTrue(Command.dim(H, 0, _1).isNoOp());
	}

	@Test
	public void shouldCheckFunctionGroup() {
		assertTrue(Command.on(H, _1).isGroup(FunctionGroup.unit));
		assertFalse(Command.on(H, _1).isGroup(FunctionGroup.house));
	}

}
