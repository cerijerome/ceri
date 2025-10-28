package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;

public class FieldBehavior {

	static enum Bit {
		_0,
		_1,
		_3,
		_7,
		_15,
		_31,
		_63;

		static final Xcoder.Types<Bit> xcoder = Xcoder.types(Bit.class, t -> t.mask);
		long mask;

		Bit() {
			mask = 1L << Integer.parseInt(name().substring(1));
		}
	}

	static class Type {
		private static final Field<RuntimeException, Type, String> S =
			Field.of(t -> t.s, (t, v) -> t.s = v);
		private static final Field.Long<RuntimeException, Type> I =
			Field.ofUint(t -> t.i, (t, v) -> t.i = v);
		private static final Field.Long<RuntimeException, Type> L =
			Field.ofLong(t -> t.l, (t, v) -> t.l = v);
		private static final Field.Long<RuntimeException, Type> IM = I.bits(8, 16);
		private static final Field.Types<RuntimeException, Type, Bit> IMT = IM.types(Bit.xcoder);
		private static final Field.Type<RuntimeException, Type, Bit> IT = I.type(Bit.xcoder);
		private static final Field.Long<RuntimeException, Type> LM = L.mask(16, 0xffffffff0000L);
		private static final Field.Types<RuntimeException, Type, Bit> LT = L.types(Bit.xcoder);
		String s = null;
		int i = 0;
		long l = 0L;

		Type(String s, int i, long l) {
			this.s = s;
			this.i = i;
			this.l = l;
		}
	}

	@Test
	public void shouldProvideNullInstance() {
		Assert.equal(Field.ofNull().set("test", 123).get("test"), null);
		Assert.equal(Field.Long.ofNull().set("test", 123).get("test"), 0L);
	}

	@Test
	public void shouldSetFieldValues() {
		var type = new Type("test", 0, 0L);
		Assert.equal(Type.S.get(type), "test");
		Type.S.set(type, "abc");
		Assert.equal(Type.S.get(type), "abc");
	}

	@Test
	public void shouldSetUintValues() {
		var type = new Type(null, -1, 0L);
		Assert.equal(Type.I.get(type), 0xffffffffL);
		Assert.equal(Type.I.getInt(type), 0xffffffff);
		Assert.equal(Type.I.getUint(type), 0xffffffffL);
		Assert.equal(Type.I.getUintExact(type), 0xffffffff);
		Type.I.set(type, 0xffffffff0000L);
		Assert.equal(Type.I.get(type), 0xffff0000L);
		Assert.equal(Type.I.getInt(type), 0xffff0000);
		Assert.equal(Type.I.getUint(type), 0xffff0000L);
		Assert.equal(Type.I.getUintExact(type), 0xffff0000);
	}

	@Test
	public void shouldSetLongValues() {
		var type = new Type(null, 0, -1L);
		Assert.equal(Type.L.get(type), 0xffffffffffffffffL);
		Assert.equal(Type.L.getInt(type), 0xffffffff);
		Assert.equal(Type.L.getUint(type), 0xffffffffL);
		Assert.thrown(ArithmeticException.class, () -> Type.L.getUintExact(type));
		Type.L.set(type, 0xffffffff0000L);
		Assert.equal(Type.L.get(type), 0xffffffff0000L);
		Assert.equal(Type.L.getInt(type), 0xffff0000);
		Assert.equal(Type.L.getUint(type), 0xffff0000L);
		Assert.thrown(ArithmeticException.class, () -> Type.L.getUintExact(type));
		Type.L.setUint(type, 0xffffffff0000L);
		Assert.equal(Type.L.get(type), 0xffff0000L);
		Assert.equal(Type.L.getInt(type), 0xffff0000);
		Assert.equal(Type.L.getUint(type), 0xffff0000L);
		Assert.equal(Type.L.getUintExact(type), 0xffff0000);
	}

	@Test
	public void shouldApplyOperator() {
		var type = new Type(null, -1, -1L);
		Type.I.apply(type, l -> l - 1);
		Assert.equal(type.i, 0xfffffffe);
		Type.I.apply(type, l -> l);
		Assert.equal(type.i, 0xfffffffe);
	}

	@Test
	public void shouldSetMaskedUintValues() {
		var type = new Type(null, -1, 0);
		Assert.equal(Type.IM.get(type), 0xffffL);
		Type.IM.set(type, 0x12345678);
		Assert.equal(type.i, 0xff5678ff);
		Assert.equal(Type.IM.get(type), 0x5678L);
	}

	@Test
	public void shouldSetMaskedLongValues() {
		var type = new Type(null, 0, -1L);
		Assert.equal(Type.LM.get(type), 0xffffffffL);
		Type.LM.set(type, 0x123456789abcdef0L);
		Assert.equal(type.l, 0xffff9abcdef0ffffL);
		Assert.equal(Type.LM.get(type), 0x9abcdef0L);
	}

	@Test
	public void shouldSetWithBoolean() {
		var type = new Type(null, 0, 0L);
		Type.LM.set(type, true);
		Assert.equal(type.l, 0xffffffff0000L);
		Assert.equal(Type.LM.getBool(type), true);
		type.l = -1L;
		Type.LM.set(type, false);
		Assert.equal(type.l, 0xffff00000000ffffL);
		Assert.equal(Type.LM.getBool(type), false);
		type.l = 0x10000;
		Assert.equal(Type.LM.getBool(type), true);
	}

	@Test
	public void shouldSetTypedFieldValues() {
		var type = new Type(null, 0, 0L);
		Type.LT.set(type, Bit._63, Bit._15, Bit._1);
		Assert.equal(type.l, 0x8000000000008002L);
		Type.LT.add(type, Bit._31, Bit._15, Bit._0);
		Assert.equal(type.l, 0x8000000080008003L);
		Type.LT.remove(type, Bit._63, Bit._3, Bit._1);
		Assert.equal(type.l, 0x80008001L);
		Type.LT.remove(type);
		Assert.equal(type.l, 0x80008001L);
	}

	@Test
	public void shouldSetTypedFieldValue() {
		var type = new Type(null, 0, 0L);
		Type.IT.set(type, Bit._15);
		Assert.equal(type.i, 0x8000);
		type.i = 0x80000000;
		Assert.equal(Type.IT.get(type), Bit._31);
		type.i = 0xf0000000;
		Assert.equal(Type.IT.get(type), null); // no exact match
	}

	@Test
	public void shouldSetMaskedTypedFieldValues() {
		var type = new Type(null, 0, 0L);
		Type.IMT.set(type, Bit._63, Bit._15, Bit._7, Bit._3);
		Assert.equal(type.i, 0x808800);
	}

	@Test
	public void shouldGetMaskedTypedFieldValues() {
		var type = new Type(null, 0xfff0f1f0, 0L);
		Assert.unordered(Type.IMT.getAll(type), Bit._15, Bit._7, Bit._0);
		Assert.equal(Type.IMT.hasAll(type, Bit._7, Bit._0), true);
		Assert.equal(Type.IMT.hasAll(type, Bit._7, Bit._1), false);
	}

	@Test
	public void shouldGetValidTypes() {
		var type = new Type(null, 0x80, 0x81L);
		Assert.equal(Type.IT.getValid(type), Bit._7);
		Assert.ordered(Type.LT.getAllValid(type), Bit._0, Bit._7);
		type.i = 0x81;
		Assert.illegalArg(() -> Type.IT.getValid(type));
	}

	@Test
	public void shouldDetermineIfFieldHasTypes() {
		var type = new Type(null, 0x80, 0x81L);
		Assert.equal(Type.IT.has(type, Bit._3), false);
		Assert.equal(Type.IT.has(type, Bit._7), true);
	}

	@Test
	public void shouldNotSupportMissingAccessors() {
		var get = Field.<RuntimeException, Type>ofUint(t -> t.i, null);
		var set = Field.<RuntimeException, Type>ofUint(null, (t, v) -> t.i = v);
		var type = new Type(null, 0x123, 0L);
		Assert.equal(get.get(type), 0x123L);
		Assert.unsupportedOp(() -> get.set(type, 0));
		set.set(type, 0);
		Assert.unsupportedOp(() -> set.get(type));
	}
}
