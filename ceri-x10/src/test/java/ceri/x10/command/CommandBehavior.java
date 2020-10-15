package ceri.x10.command;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
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
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(Command.from("A[1,2]:on"), is(Command.on(A, _1, _2)));
		assertThat(Command.from("B[]:on"), is(Command.on(B)));
		assertThat(Command.from("C[ ]:on"), is(Command.on(C)));
		assertThat(Command.from("D:on"), is(Command.on(D)));
		assertThat(Command.from("E:allLightsOn"), is(Command.allLightsOn(E)));
		assertThat(Command.from("F:allLightsOff"), is(Command.allLightsOff(F)));
		assertThat(Command.from("G:allUnitsOff"), is(Command.allUnitsOff(G)));
		assertThat(Command.from("H[3,4]:ext:100:200"), is(Command.ext(H, 100, 200, _3, _4)));
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
		assertThat(Command.allLightsOn(H).isNoOp(), is(false));
		assertThat(Command.on(H).isNoOp(), is(true));
		assertThat(Command.dim(H, 0, _1).isNoOp(), is(true));
	}

	@Test
	public void shouldCheckFunctionGroup() {
		assertThat(Command.on(H, _1).isGroup(FunctionGroup.unit), is(true));
		assertThat(Command.on(H, _1).isGroup(FunctionGroup.house), is(false));
	}

}
