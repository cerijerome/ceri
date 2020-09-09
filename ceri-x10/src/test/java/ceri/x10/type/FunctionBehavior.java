package ceri.x10.type;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.x10.type.FunctionType.allLightsOff;
import static ceri.x10.type.FunctionType.allLightsOn;
import static ceri.x10.type.FunctionType.bright;
import static ceri.x10.type.FunctionType.on;
import org.junit.Test;

public class FunctionBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Function t = Function.of(House.F, allLightsOn);
		Function eq0 = Function.of(House.F, allLightsOn);
		Function ne0 = Function.of(House.N, allLightsOn);
		Function ne1 = Function.of(House.F, allLightsOff);
		Function ne2 = Function.of(House.F, on);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldValidateArguments() {
		assertThrown(() -> Function.of(null, on));
		assertThrown(() -> Function.of(House.B, null));
		assertThrown(() -> Function.of(House.B, bright));
	}

}
