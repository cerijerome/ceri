package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.validation.DisplayLong.dec;
import static ceri.common.validation.DisplayLong.hex;
import static ceri.common.validation.DisplayLong.hex2;
import static ceri.common.validation.DisplayLong.hex4;
import org.junit.Test;

public class TypeValueBehavior {

	private static enum E {
		one,
		two,
		ten;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		TypeValue<?> v = TypeValue.of(1, E.one, null, 1, hex4);
		TypeValue<?> eq0 = TypeValue.of(1, E.one, null, 1, hex4);
		TypeValue<?> eq1 = TypeValue.of(1, E.one, "two", 1, hex4);
		TypeValue<?> eq2 = TypeValue.of(1, E.one, "two", 1);
		TypeValue<?> ne0 = TypeValue.of(0, E.one, null, 1, hex4);
		TypeValue<?> ne1 = TypeValue.of(1, E.two, null, 1, hex4);
		TypeValue<?> ne2 = TypeValue.of(1, null, "one", 1, hex4);
		TypeValue<?> ne3 = TypeValue.of(1, null, null, 1, hex4);
		TypeValue<?> ne4 = TypeValue.of(1, E.one, null, 0, hex4);
		TypeValue<?> ne5 = TypeValue.of(1, E.one, null, hex4);
		TypeValue<?> ne6 = TypeValue.of(1, E.one, null);
		exerciseEquals(v, eq0, eq1);
		assertEquals(v, eq2);
		assertAllNotEqual(v, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
		v = TypeValue.of(1, null, "one", 1, hex4);
		eq0 = TypeValue.of(1, null, "one", 1, hex4);
		ne0 = TypeValue.of(1, null, "One", 1, hex4);
		exerciseEquals(v, eq0);
		assertAllNotEqual(v, ne0, ne1);
	}

	@Test
	public void shouldProvideSubValue() {
		assertEquals(TypeValue.of(1, null, "one", hex).intSub(), 0);
		assertEquals(TypeValue.of(1, null, "one", 1, hex).intSub(), 1);
	}

	@Test
	public void shouldProvideCastValues() {
		assertEquals(TypeValue.of(-1, E.one, null, hex).value(), -1L);
		assertEquals(TypeValue.of(-1, E.one, null, hex).intValue(), -1);
	}

	@Test
	public void shouldReturnName() {
		assertEquals(TypeValue.of(1, E.one, null, hex).name(), "one");
		assertEquals(TypeValue.of(1, E.one, "ONE", hex).name(), "one");
		assertEquals(TypeValue.of(1, null, "ONE", hex).name(), "ONE");
		assertNull(TypeValue.of(1, null, null, hex).name());
	}

	@Test
	public void shouldProvideCompactFormat() {
		assertEquals(TypeValue.of(255, E.ten, null, hex).compact(), "ten");
		assertEquals(TypeValue.of(255, E.ten, null, 1, hex).compact(), "ten(0x1)");
		assertEquals(TypeValue.of(255, null, "test", hex).compact(), "test(0xff)");
	}

	@Test
	public void shouldFormatValue() {
		assertEquals(TypeValue.of(255, E.ten, null, hex2).toString(), "ten(0xff)");
		assertEquals(TypeValue.of(256, E.ten, null, 255, hex2).toString(), "ten(0x00:0xff)");
		assertEquals(TypeValue.of(256, E.ten, null, hex4).toString(), "ten(0x0100)");
		assertEquals(TypeValue.of(256, E.ten, null, 1, hex4).toString(), "ten(0x0100:0x0001)");
		assertEquals(TypeValue.of(256, E.ten, null, hex).toString(), "ten(0x100)");
	}

	@Test
	public void shouldValidateValue() {
		TypeValue.validate(TypeValue.of(1, E.one, "one", dec));
		assertThrown(() -> TypeValue.validate(TypeValue.of(1, null, "one", dec)));
		TypeValue.validateExcept(TypeValue.of(1, E.one, "one", dec), E.two);
		assertThrown(() -> TypeValue.validateExcept(TypeValue.of(1, E.one, "one", dec), E.one));
	}

}
