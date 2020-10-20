package ceri.common.data;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class IntTypeValueBehavior {

	private static enum E {
		one,
		two,
		ten;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		IntTypeValue<?> v = IntTypeValue.of(1, E.one, null, 1);
		IntTypeValue<?> eq0 = IntTypeValue.of(1, E.one, null, 1);
		IntTypeValue<?> eq1 = IntTypeValue.of(1, E.one, "two", 1);
		IntTypeValue<?> ne0 = IntTypeValue.of(0, E.one, null, 1);
		IntTypeValue<?> ne1 = IntTypeValue.of(1, E.two, null, 1);
		IntTypeValue<?> ne2 = IntTypeValue.of(1, null, "one", 1);
		IntTypeValue<?> ne3 = IntTypeValue.of(1, null, null, 1);
		IntTypeValue<?> ne4 = IntTypeValue.of(1, E.one, null, 0);
		IntTypeValue<?> ne5 = IntTypeValue.of(1, E.one, null);
		exerciseEquals(v, eq0, eq1);
		assertAllNotEqual(v, ne0, ne1, ne2, ne3, ne4, ne5);
		v = IntTypeValue.of(1, null, "one", 1);
		eq0 = IntTypeValue.of(1, null, "one", 1);
		ne0 = IntTypeValue.of(1, E.one, "one", 1);
		ne1 = IntTypeValue.of(1, null, "ONE", 1);
		exerciseEquals(v, eq0);
		assertAllNotEqual(v, ne0, ne1);
	}

	@Test
	public void shouldProvideSubValue() {
		assertEquals(IntTypeValue.of(1, null, "one").subValue(), 0);
		assertEquals(IntTypeValue.of(1, null, "one", 1).subValue(), 1);
	}

	@Test
	public void shouldProvideCastValues() {
		assertEquals(IntTypeValue.of(0xffff, E.one, null).value(), 0xffff);
		assertEquals(IntTypeValue.of(0xffff, E.one, null).byteValue(), (byte) -1);
		assertEquals(IntTypeValue.of(0xffff, E.one, null).shortValue(), (short) -1);
	}

	@Test
	public void shouldReturnName() {
		assertEquals(IntTypeValue.of(1, E.one, null).name(), "one");
		assertEquals(IntTypeValue.of(1, E.one, "ONE").name(), "one");
		assertEquals(IntTypeValue.of(1, null, "ONE").name(), "ONE");
		assertNull(IntTypeValue.of(1, null, null).name());
	}

	@Test
	public void shouldProvideCompactFormat() {
		assertEquals(IntTypeValue.ofUint(255, E.ten, null).compact(), "ten");
		assertEquals(IntTypeValue.ofUint(255, E.ten, null, 1).compact(), "ten(0x1)");
		assertEquals(IntTypeValue.ofUbyte(255, null, "test").compact(), "test(0xff)");
	}

	@Test
	public void shouldFormatValue() {
		assertEquals(IntTypeValue.ofUbyte(255, E.ten, null).toString(), "ten(0xff)");
		assertEquals(IntTypeValue.ofUbyte(256, E.ten, null, 255).toString(), "ten(0x00:0xff)");
		assertEquals(IntTypeValue.ofUshort(256, E.ten, null).toString(), "ten(0x0100)");
		assertEquals(IntTypeValue.ofUshort(256, E.ten, null, 1).toString(), "ten(0x0100:0x0001)");
		assertEquals(IntTypeValue.ofUint(256, E.ten, null).toString(), "ten(0x100)");
	}

	@Test
	public void shouldValidateValue() {
		IntTypeValue.validate(IntTypeValue.of(1, E.one, "one"));
		assertThrown(() -> IntTypeValue.validate(IntTypeValue.of(1, null, "one")));
		IntTypeValue.validateExcept(IntTypeValue.of(1, E.one, "one"), E.two);
		assertThrown(() -> IntTypeValue.validateExcept(IntTypeValue.of(1, E.one, "one"), E.one));
	}

}
