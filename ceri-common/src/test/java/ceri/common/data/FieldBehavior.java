package ceri.common.data;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.util.HashSet;
import org.junit.Test;
import ceri.common.test.TestUtil.Rte;

public class FieldBehavior {

	static enum Bit {
		_0,
		_1,
		_3,
		_7,
		_15,
		_31,
		_63;

		static final TypeTranscoder<Bit> xcoder = TypeTranscoder.of(t -> t.mask, Bit.class);
		long mask;

		Bit() {
			mask = 1L << Integer.parseInt(name().substring(1));
		}
	}

	static class Type {
		private static final Field<Rte, Type, String> S = Field.of(t -> t.s, (t, v) -> t.s = v);
		private static final Field.Long<Rte, Type> I = Field.ofUint(t -> t.i, (t, v) -> t.i = v);
		private static final Field.Long<Rte, Type> L = Field.ofLong(t -> t.l, (t, v) -> t.l = v);
		private static final Field.Long<Rte, Type> IM = I.bits(8, 16);
		private static final Field.Types<Rte, Type, Bit> IMT = IM.types(Bit.xcoder);
		private static final Field.Type<Rte, Type, Bit> IT = I.type(Bit.xcoder);
		private static final Field.Long<Rte, Type> LM = L.mask(16, 0xffffffff0000L);
		private static final Field.Types<Rte, Type, Bit> LT = L.types(Bit.xcoder);
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
		assertEquals(Field.ofNull().set("test", 123).get("test"), null);
		assertEquals(Field.Long.ofNull().set("test", 123).get("test"), 0L);
	}

	@Test
	public void shouldSetFieldValues() {
		var type = new Type("test", 0, 0L);
		assertEquals(Type.S.get(type), "test");
		Type.S.set(type, "abc");
		assertEquals(Type.S.get(type), "abc");
	}

	@Test
	public void shouldSetUintValues() {
		var type = new Type(null, -1, 0L);
		assertEquals(Type.I.get(type), 0xffffffffL);
		assertEquals(Type.I.getInt(type), 0xffffffff);
		assertEquals(Type.I.getUint(type), 0xffffffffL);
		assertEquals(Type.I.getUintExact(type), 0xffffffff);
		Type.I.set(type, 0xffffffff0000L);
		assertEquals(Type.I.get(type), 0xffff0000L);
		assertEquals(Type.I.getInt(type), 0xffff0000);
		assertEquals(Type.I.getUint(type), 0xffff0000L);
		assertEquals(Type.I.getUintExact(type), 0xffff0000);
	}

	@Test
	public void shouldSetLongValues() {
		var type = new Type(null, 0, -1L);
		assertEquals(Type.L.get(type), 0xffffffffffffffffL);
		assertEquals(Type.L.getInt(type), 0xffffffff);
		assertEquals(Type.L.getUint(type), 0xffffffffL);
		assertThrown(ArithmeticException.class, () -> Type.L.getUintExact(type));
		Type.L.set(type, 0xffffffff0000L);
		assertEquals(Type.L.get(type), 0xffffffff0000L);
		assertEquals(Type.L.getInt(type), 0xffff0000);
		assertEquals(Type.L.getUint(type), 0xffff0000L);
		assertThrown(ArithmeticException.class, () -> Type.L.getUintExact(type));
		Type.L.setUint(type, 0xffffffff0000L);
		assertEquals(Type.L.get(type), 0xffff0000L);
		assertEquals(Type.L.getInt(type), 0xffff0000);
		assertEquals(Type.L.getUint(type), 0xffff0000L);
		assertEquals(Type.L.getUintExact(type), 0xffff0000);
	}

	@Test
	public void shouldSetMaskedUintValues() {
		var type = new Type(null, -1, 0);
		assertEquals(Type.IM.get(type), 0xffffL);
		Type.IM.set(type, 0x12345678);
		assertEquals(type.i, 0xff5678ff);
		assertEquals(Type.IM.get(type), 0x5678L);
	}

	@Test
	public void shouldSetMaskedLongValues() {
		var type = new Type(null, 0, -1L);
		assertEquals(Type.LM.get(type), 0xffffffffL);
		Type.LM.set(type, 0x123456789abcdef0L);
		assertEquals(type.l, 0xffff9abcdef0ffffL);
		assertEquals(Type.LM.get(type), 0x9abcdef0L);
	}

	@Test
	public void shouldSetWithBoolean() {
		var type = new Type(null, 0, 0L);
		Type.LM.set(type, true);
		assertEquals(type.l, 0xffffffff0000L);
		assertEquals(Type.LM.getBool(type), true);
		type.l = -1L;
		Type.LM.set(type, false);
		assertEquals(type.l, 0xffff00000000ffffL);
		assertEquals(Type.LM.getBool(type), false);
		type.l = 0x10000;
		assertEquals(Type.LM.getBool(type), true);
	}

	@Test
	public void shouldSetTypedFieldValues() {
		var type = new Type(null, 0, 0L);
		Type.LT.set(type, Bit._63, Bit._15, Bit._1);
		assertEquals(type.l, 0x8000000000008002L);
		Type.LT.add(type, Bit._31, Bit._15, Bit._0);
		assertEquals(type.l, 0x8000000080008003L);
		Type.LT.remove(type, Bit._63, Bit._3, Bit._1);
		assertEquals(type.l, 0x80008001L);
		Type.LT.remove(type);
		assertEquals(type.l, 0x80008001L);
	}

	@Test
	public void shouldSetTypedFieldValue() {
		var type = new Type(null, 0, 0L);
		Type.IT.set(type, Bit._15);
		assertEquals(type.i, 0x8000);
		type.i = 0x80000000;
		assertEquals(Type.IT.get(type), Bit._31);
		type.i = 0xf0000000;
		assertEquals(Type.IT.get(type), null); // no exact match
	}

	@Test
	public void shouldSetMaskedTypedFieldValues() {
		var type = new Type(null, 0, 0L);
		Type.IMT.set(type, Bit._63, Bit._15, Bit._7, Bit._3);
		assertEquals(type.i, 0x808800);
	}

	@Test
	public void shouldGetMaskedTypedFieldValues() {
		var type = new Type(null, 0xfff0f1f0, 0L);
		assertCollection(Type.IMT.get(type), Bit._15, Bit._7, Bit._0);
		assertEquals(Type.IMT.has(type, Bit._7, Bit._0), true);
		assertEquals(Type.IMT.has(type, Bit._7, Bit._1), false);
	}

	@Test
	public void shouldCollectTypedFieldValues() {
		var type = new Type(null, 0, 0xfL);
		var set = new HashSet<Bit>();
		assertEquals(Type.LT.get(type, set), 4L);
		assertEquals(Type.LT.getInt(type, set), 4);
		assertCollection(set, Bit._3, Bit._1, Bit._0);
	}

	@Test
	public void shouldNotSupportMissingAccessors() {
		var get = Field.<Rte, Type>ofUint(t -> t.i, null);
		var set = Field.<Rte, Type>ofUint(null, (t, v) -> t.i = v);
		var type = new Type(null, 0x123, 0L);
		assertEquals(get.get(type), 0x123L);
		assertUnsupported(() -> get.set(type, 0));
		set.set(type, 0);
		assertUnsupported(() -> set.get(type));
	}

}
