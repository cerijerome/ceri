package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;
import ceri.common.text.Format;

public class TypeValueBehavior {

	private static enum E {
		one,
		two,
		ten;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var v = TypeValue.of(1, E.one, null, 1, Format.HEX_SHORT);
		var eq0 = TypeValue.of(1, E.one, null, 1, Format.HEX_SHORT);
		var eq1 = TypeValue.of(1, E.one, "two", 1, Format.HEX_SHORT);
		var eq2 = TypeValue.of(1, E.one, "two", 1);
		var ne0 = TypeValue.of(0, E.one, null, 1, Format.HEX_SHORT);
		var ne1 = TypeValue.of(1, E.two, null, 1, Format.HEX_SHORT);
		var ne2 = TypeValue.of(1, null, "one", 1, Format.HEX_SHORT);
		var ne3 = TypeValue.of(1, null, null, 1, Format.HEX_SHORT);
		var ne4 = TypeValue.of(1, E.one, null, 0, Format.HEX_SHORT);
		var ne5 = TypeValue.of(1, E.one, null, Format.HEX_SHORT);
		var ne6 = TypeValue.of(1, E.one, null);
		TestUtil.exerciseEquals(v, eq0, eq1);
		Assert.equal(v, eq2);
		Assert.notEqualAll(v, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
		v = TypeValue.of(1, null, "one", 1, Format.HEX_SHORT);
		eq0 = TypeValue.of(1, null, "one", 1, Format.HEX_SHORT);
		ne0 = TypeValue.of(1, null, "One", 1, Format.HEX_SHORT);
		TestUtil.exerciseEquals(v, eq0);
		Assert.notEqualAll(v, ne0, ne1);
	}

	@Test
	public void shouldProvideSubValue() {
		Assert.equal(TypeValue.of(1, null, "one", Format.HEX).intSub(), 0);
		Assert.equal(TypeValue.of(1, null, "one", 1, Format.HEX).intSub(), 1);
	}

	@Test
	public void shouldProvideCastValues() {
		Assert.equal(TypeValue.of(-1, E.one, null, Format.HEX).value(), -1L);
		Assert.equal(TypeValue.of(-1, E.one, null, Format.HEX).intValue(), -1);
	}

	@Test
	public void shouldReturnName() {
		Assert.equal(TypeValue.of(1, E.one, null, Format.HEX).name(), "one");
		Assert.equal(TypeValue.of(1, E.one, "ONE", Format.HEX).name(), "one");
		Assert.equal(TypeValue.of(1, null, "ONE", Format.HEX).name(), "ONE");
		Assert.isNull(TypeValue.of(1, null, null, Format.HEX).name());
	}

	@Test
	public void shouldProvideCompactFormat() {
		Assert.equal(TypeValue.of(255, E.ten, null, Format.HEX).compact(), "ten");
		Assert.equal(TypeValue.of(255, E.ten, null, 1, Format.HEX).compact(), "ten(0x1)");
		Assert.equal(TypeValue.of(255, null, "test", Format.HEX).compact(), "test(0xff)");
	}

	@Test
	public void shouldFormatValue() {
		Assert.equal(TypeValue.of(255, E.ten, null, Format.HEX_BYTE).toString(), "ten(0xff)");
		Assert.equal(TypeValue.of(256, E.ten, null, 255, Format.HEX_BYTE).toString(),
			"ten(0x00:0xff)");
		Assert.equal(TypeValue.of(256, E.ten, null, Format.HEX_SHORT).toString(), "ten(0x0100)");
		Assert.equal(TypeValue.of(256, E.ten, null, 1, Format.HEX_SHORT).toString(),
			"ten(0x0100:0x0001)");
		Assert.equal(TypeValue.of(256, E.ten, null, Format.HEX).toString(), "ten(0x100)");
	}

	@Test
	public void shouldValidateValue() {
		TypeValue.validate(TypeValue.of(1, E.one, "one", Format.DEC));
		Assert.thrown(() -> TypeValue.validate(TypeValue.of(1, null, "one", Format.DEC)));
		TypeValue.validExcept(TypeValue.of(1, E.one, "one", Format.DEC), E.two);
		Assert
			.thrown(() -> TypeValue.validExcept(TypeValue.of(1, E.one, "one", Format.DEC), E.one));
	}
}
