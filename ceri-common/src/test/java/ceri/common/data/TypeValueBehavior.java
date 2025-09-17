package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.common.text.Formats;

public class TypeValueBehavior {

	private static enum E {
		one,
		two,
		ten;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var v = TypeValue.of(1, E.one, null, 1, Formats.HEX_SHORT);
		var eq0 = TypeValue.of(1, E.one, null, 1, Formats.HEX_SHORT);
		var eq1 = TypeValue.of(1, E.one, "two", 1, Formats.HEX_SHORT);
		var eq2 = TypeValue.of(1, E.one, "two", 1);
		var ne0 = TypeValue.of(0, E.one, null, 1, Formats.HEX_SHORT);
		var ne1 = TypeValue.of(1, E.two, null, 1, Formats.HEX_SHORT);
		var ne2 = TypeValue.of(1, null, "one", 1, Formats.HEX_SHORT);
		var ne3 = TypeValue.of(1, null, null, 1, Formats.HEX_SHORT);
		var ne4 = TypeValue.of(1, E.one, null, 0, Formats.HEX_SHORT);
		var ne5 = TypeValue.of(1, E.one, null, Formats.HEX_SHORT);
		var ne6 = TypeValue.of(1, E.one, null);
		TestUtil.exerciseEquals(v, eq0, eq1);
		assertEquals(v, eq2);
		assertAllNotEqual(v, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
		v = TypeValue.of(1, null, "one", 1, Formats.HEX_SHORT);
		eq0 = TypeValue.of(1, null, "one", 1, Formats.HEX_SHORT);
		ne0 = TypeValue.of(1, null, "One", 1, Formats.HEX_SHORT);
		TestUtil.exerciseEquals(v, eq0);
		assertAllNotEqual(v, ne0, ne1);
	}

	@Test
	public void shouldProvideSubValue() {
		assertEquals(TypeValue.of(1, null, "one", Formats.HEX).intSub(), 0);
		assertEquals(TypeValue.of(1, null, "one", 1, Formats.HEX).intSub(), 1);
	}

	@Test
	public void shouldProvideCastValues() {
		assertEquals(TypeValue.of(-1, E.one, null, Formats.HEX).value(), -1L);
		assertEquals(TypeValue.of(-1, E.one, null, Formats.HEX).intValue(), -1);
	}

	@Test
	public void shouldReturnName() {
		assertEquals(TypeValue.of(1, E.one, null, Formats.HEX).name(), "one");
		assertEquals(TypeValue.of(1, E.one, "ONE", Formats.HEX).name(), "one");
		assertEquals(TypeValue.of(1, null, "ONE", Formats.HEX).name(), "ONE");
		assertNull(TypeValue.of(1, null, null, Formats.HEX).name());
	}

	@Test
	public void shouldProvideCompactFormat() {
		assertEquals(TypeValue.of(255, E.ten, null, Formats.HEX).compact(), "ten");
		assertEquals(TypeValue.of(255, E.ten, null, 1, Formats.HEX).compact(), "ten(0x1)");
		assertEquals(TypeValue.of(255, null, "test", Formats.HEX).compact(), "test(0xff)");
	}

	@Test
	public void shouldFormatValue() {
		assertEquals(TypeValue.of(255, E.ten, null, Formats.HEX_BYTE).toString(), "ten(0xff)");
		assertEquals(TypeValue.of(256, E.ten, null, 255, Formats.HEX_BYTE).toString(),
			"ten(0x00:0xff)");
		assertEquals(TypeValue.of(256, E.ten, null, Formats.HEX_SHORT).toString(), "ten(0x0100)");
		assertEquals(TypeValue.of(256, E.ten, null, 1, Formats.HEX_SHORT).toString(),
			"ten(0x0100:0x0001)");
		assertEquals(TypeValue.of(256, E.ten, null, Formats.HEX).toString(), "ten(0x100)");
	}

	@Test
	public void shouldValidateValue() {
		TypeValue.validate(TypeValue.of(1, E.one, "one", Formats.DEC));
		assertThrown(() -> TypeValue.validate(TypeValue.of(1, null, "one", Formats.DEC)));
		TypeValue.validateExcept(TypeValue.of(1, E.one, "one", Formats.DEC), E.two);
		assertThrown(
			() -> TypeValue.validateExcept(TypeValue.of(1, E.one, "one", Formats.DEC), E.one));
	}
}
