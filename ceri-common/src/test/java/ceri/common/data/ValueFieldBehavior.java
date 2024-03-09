package ceri.common.data;

import static ceri.common.data.MaskTranscoder.mask;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ubyteExact;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.math.MathUtil.ushortExact;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnsupported;
import org.junit.Test;

public class ValueFieldBehavior {

	static class Holder {
		static final ValueField.Typed<Holder> longField =
			ValueField.Typed.of(h -> h.longVal, (h, l) -> h.longVal = l);
		static final ValueField.Typed<Holder> intField =
			ValueField.Typed.ofInt(h -> h.intVal, (h, i) -> h.intVal = i);
		static final ValueField.Typed<Holder> ushortField =
			ValueField.Typed.of(h -> ushort(h.shortVal), (h, s) -> h.shortVal = ushortExact(s));
		static final ValueField.Typed<Holder> ubyteField =
			ValueField.Typed.of(h -> ubyte(h.byteVal), (h, b) -> h.byteVal = ubyteExact(b));
		long longVal = 0;
		int intVal = 0;
		short shortVal = 0;
		byte byteVal = 0;
	}

	@Test
	public void shouldFailForNullAccessors() {
		assertUnsupported(() -> ValueField.of(null, null).get());
		assertUnsupported(() -> ValueField.of(null, null).set(0));
		assertUnsupported(() -> ValueField.ofInt(null, null).get());
		assertUnsupported(() -> ValueField.ofInt(null, null).set(0));
	}

	@Test
	public void shouldFailForNullTypedAccessors() {
		assertUnsupported(() -> ValueField.Typed.of(null, null).get(""));
		assertUnsupported(() -> ValueField.Typed.of(null, null).set("", 0));
		assertUnsupported(() -> ValueField.Typed.ofInt(null, null).get(""));
		assertUnsupported(() -> ValueField.Typed.ofInt(null, null).set("", 0));
	}

	@Test
	public void shouldSetMaskedValues() {
		Holder h = new Holder();
		ValueField intField = Holder.intField.from(h);
		intField.set(0x1234);
		intField.mask(0xff00).set(0xabcd);
		assertEquals(h.intVal, 0xab34);
	}

	@Test
	public void shouldSetUnsignedValue() {
		Holder h = new Holder();
		ValueField field = Holder.longField.from(h);
		field.setUint(-1);
		assertEquals(field.get(), 0xffffffffL);
	}

	@Test
	public void shouldGetIntegerValues() {
		var field = ValueField.of(() -> 0xffeeddccbbaa9988L, null);
		assertEquals(field.getInt(), 0xbbaa9988);
		assertEquals(field.getUint(), 0xbbaa9988L);
	}

	@Test
	public void shouldGetMaskedValues() {
		Holder h = new Holder();
		ValueField intField = Holder.intField.from(h);
		intField.set(0x123456);
		assertEquals(intField.mask(0xff00).getInt(), 0x3400);
	}

	@Test
	public void shouldAddValues() {
		Holder h = new Holder();
		ValueField intField = Holder.intField.from(h);
		intField.set(0x1234);
		intField.add(0x4321);
		assertEquals(h.intVal, 0x5335);
	}

	@Test
	public void shouldRemoveValues() {
		Holder h = new Holder();
		ValueField intField = Holder.intField.from(h);
		intField.set(0x1234);
		intField.remove(0x4321);
		assertEquals(h.intVal, 0x1014);
	}

	@Test
	public void shouldAddTypedValues() {
		Holder h = new Holder();
		Holder.intField.set(h, 0x1200);
		assertEquals(h.intVal, 0x1200);
		Holder.intField.add(h, 0x0034);
		assertEquals(h.intVal, 0x1234);
	}

	@Test
	public void shouldRemoveTypedValues() {
		Holder h = new Holder();
		Holder.intField.set(h, 0x1234);
		assertEquals(h.intVal, 0x1234);
		Holder.intField.remove(h, 0x0034);
		assertEquals(h.intVal, 0x1200);
	}

	@Test
	public void shouldSetMaskedTypeValues() {
		Holder h = new Holder();
		Holder.intField.set(h, 0x1234);
		Holder.intField.mask(mask(0xff00, 0)).set(h, 0xabcd);
		assertEquals(h.intVal, 0xab34);
	}

	@Test
	public void shouldGetMaskedTypeValues() {
		Holder h = new Holder();
		Holder.intField.set(h, 0x123456);
		assertEquals(Holder.intField.mask(mask(0xff00, 0)).getInt(h), 0x3400);
	}

	@Test
	public void shouldAccessIntegerFields() {
		Holder h = new Holder();
		Holder.intField.set(h, 0xff);
		assertEquals(h.intVal, 0xff);
		assertEquals(Holder.intField.getInt(h), 0xff);
		ValueField.Typed<Holder> iAcc = ValueField.Typed.ofInt(t -> t.intVal, null);
		assertEquals(iAcc.getInt(h), 0xff);
	}

	@Test
	public void shouldProvideInstanceAccessorFromTypedAccessor() {
		Holder h = new Holder();
		ValueField l = Holder.longField.from(h);
		ValueField i = Holder.intField.from(h);
		ValueField s = Holder.ushortField.from(h);
		ValueField b = Holder.ubyteField.from(h);
		l.set(0xffffffffffffffL);
		i.set(0xffffff);
		s.set(0);
		s.set(0xffff);
		assertThrown(() -> s.set(0x10000));
		assertThrown(() -> s.set(-1));
		b.set(0);
		b.set(0xff);
		assertThrown(() -> b.set(0x100));
		assertThrown(() -> b.set(-1));
		assertEquals(h.longVal, 0xffffffffffffffL);
		assertEquals(h.intVal, 0xffffff);
		assertEquals(h.shortVal, (short) -1);
		assertEquals(h.byteVal, (byte) -1);
		assertEquals(i.getInt(), 0xffffff);
		assertEquals(s.getInt(), 0xffff);
		assertEquals(b.getInt(), 0xff);
	}

}
