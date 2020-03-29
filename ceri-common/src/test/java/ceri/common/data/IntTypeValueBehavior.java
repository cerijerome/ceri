package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
		assertThat(IntTypeValue.of(1, null, "one").subValue(), is(0));
		assertThat(IntTypeValue.of(1, null, "one", 1).subValue(), is(1));
	}

	@Test
	public void shouldProvideCastValues() {
		assertThat(IntTypeValue.of(0xffff, E.one, null).byteValue(), is((byte) -1));
		assertThat(IntTypeValue.of(0xffff, E.one, null).shortValue(), is((short) -1));
	}

	@Test
	public void shouldReturnName() {
		assertThat(IntTypeValue.of(1, E.one, null).name(), is("one"));
		assertThat(IntTypeValue.of(1, E.one, "ONE").name(), is("one"));
		assertThat(IntTypeValue.of(1, null, "ONE").name(), is("ONE"));
		assertNull(IntTypeValue.of(1, null, null).name());
	}

	@Test
	public void shouldProvideCompactFormat() {
		assertThat(IntTypeValue.ofUint(255, E.ten, null).compact(), is("ten"));
		assertThat(IntTypeValue.ofUint(255, E.ten, null, 1).compact(), is("ten(0x1)"));
		assertThat(IntTypeValue.ofUbyte(255, null, "test").compact(), is("test(0xff)"));
	}

	@Test
	public void shouldFormatValue() {
		assertThat(IntTypeValue.ofUbyte(255, E.ten, null).toString(), is("ten(0xff)"));
		assertThat(IntTypeValue.ofUbyte(256, E.ten, null, 255).toString(), is("ten(0x00:0xff)"));
		assertThat(IntTypeValue.ofUshort(256, E.ten, null).toString(), is("ten(0x0100)"));
		assertThat(IntTypeValue.ofUshort(256, E.ten, null, 1).toString(), is("ten(0x0100:0x0001)"));
		assertThat(IntTypeValue.ofUint(256, E.ten, null).toString(), is("ten(0x100)"));
	}

}
