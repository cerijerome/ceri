package ceri.x10.type;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class ExtFunctionBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ExtFunction t = ExtFunction.of(House.I, 0xff, 0x80);
		ExtFunction eq0 = ExtFunction.of(House.I, 0xff, 0x80);
		ExtFunction ne0 = ExtFunction.of(House.J, 0xff, 0x80);
		ExtFunction ne1 = ExtFunction.of(House.I, 0xfe, 0x80);
		ExtFunction ne2 = ExtFunction.of(House.I, 0xff, 0x7f);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldValidateArguments() {
		assertThrown(() -> ExtFunction.of(null, 0, 0));
		assertThrown(() -> ExtFunction.of(House.A, -1, 0));
		assertThrown(() -> ExtFunction.of(House.A, 0x100, 0));
		assertThrown(() -> ExtFunction.of(House.A, 0, -1));
		assertThrown(() -> ExtFunction.of(House.A, 0, 0x100));
	}

}
