package ceri.common.data;

import static ceri.common.data.MaskTranscoder.mask;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import ceri.common.data.TypeTranscoder.Remainder;

public class TypeTranscoderBehavior {
	private static final TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, E.class);

	enum E {
		a(1),
		b(2),
		c(12);

		public final int value;

		E(int value) {
			this.value = value;
		}
	}

	static class Holder {
		static final IntField.Typed<Holder> acc = IntField.typed(h -> h.val, (h, i) -> h.val = i);

		int val = 0;
	}

	@Test
	public void shouldNotBreachRemainderEqualsContract() {
		Remainder<E> t = xcoder.decodeWithRemainder(7);
		Remainder<E> eq0 = xcoder.decodeWithRemainder(7);
		Remainder<E> ne0 = xcoder.decodeWithRemainder(3);
		Remainder<E> ne1 = xcoder.decodeWithRemainder(5);
		Remainder<E> ne2 = xcoder.decodeWithRemainder(0);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldDetermineIfRemainderIsEmpty() {
		assertTrue(xcoder.decodeWithRemainder(0).isEmpty());
		assertFalse(xcoder.decodeWithRemainder(4).isEmpty());
		assertFalse(xcoder.decodeWithRemainder(1).isEmpty());
		assertFalse(xcoder.decodeWithRemainder(5).isEmpty());
	}

	@Test
	public void shouldDetermineIfRemainderIsExact() {
		assertTrue(xcoder.decodeWithRemainder(0).isExact());
		assertTrue(xcoder.decodeWithRemainder(1).isExact());
		assertTrue(xcoder.decodeWithRemainder(3).isExact());
		assertFalse(xcoder.decodeWithRemainder(4).isExact());
		assertFalse(xcoder.decodeWithRemainder(5).isExact());
	}

	@Test
	public void shouldReturnAllValues() {
		assertCollection(xcoder.all(), E.a, E.b, E.c);
	}

	@Test
	public void shouldEncodeValues() {
		assertEquals(xcoder.encode(), 0);
		assertEquals(xcoder.encode((E[]) null), 0);
		assertEquals(xcoder.encode((List<E>) null), 0);
		assertEquals(xcoder.encode(List.of()), 0);
		assertEquals(xcoder.encode((Remainder<E>) null), 0);
		assertEquals(xcoder.encode(E.b), E.b.value);
		assertEquals(xcoder.encode(E.b, E.c), E.b.value + E.c.value);
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(xcoder.decode(0));
		assertNull(xcoder.decode(3));
		assertEquals(xcoder.decode(2), E.b);
		assertCollection(xcoder.decodeAll(0));
		assertCollection(xcoder.decodeAll(4));
		assertCollection(xcoder.decodeAll(3), E.a, E.b);
		assertCollection(xcoder.decodeAll(15), E.a, E.b, E.c);
		assertCollection(xcoder.decodeAll(31), E.a, E.b, E.c);
	}

	@Test
	public void shouldDecodeValueWithValidation() {
		assertThrown(() -> xcoder.decodeValid(0));
		assertThrown(() -> xcoder.decodeValid(0, "E"));
		assertEquals(xcoder.decodeValid(2), E.b);
		assertEquals(xcoder.decodeValid(2, "E"), E.b);
	}

	@Test
	public void shouldDetermineIfValueHasType() {
		assertFalse(xcoder.has(0, E.a));
		assertTrue(xcoder.has(1, E.a));
		assertFalse(xcoder.has(13, E.b));
	}

	@Test
	public void shouldDetermineIfValueHasAnyTypes() {
		assertFalse(xcoder.hasAny(0, E.a, E.b, E.c));
		assertTrue(xcoder.hasAny(1, E.a, E.b, E.c));
		assertTrue(xcoder.hasAny(13, E.a, E.b, E.c));
		assertFalse(xcoder.hasAny(13, E.b));
	}

	@Test
	public void shouldDetermineIfValueHasAllTypes() {
		assertTrue(xcoder.hasAll(0));
		assertTrue(xcoder.hasAll(1, E.a));
		assertTrue(xcoder.hasAll(13, E.a, E.c));
		assertFalse(xcoder.hasAll(13, E.a, E.b, E.c));
		assertFalse(xcoder.hasAll(13, E.b));
	}

	@Test
	public void shouldValidateValues() {
		assertTrue(xcoder.isValid(0));
		assertFalse(xcoder.isValid(-1));
		assertTrue(xcoder.isValid(12));
		assertTrue(xcoder.isValid(3));
		assertFalse(xcoder.isValid(4));
	}

	@Test
	public void shouldEncodeSubset() {
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, E.a, E.c);
		assertEquals(xcoder.encode(), 0);
		assertEquals(xcoder.encode((E[]) null), 0);
		assertEquals(xcoder.encode((List<E>) null), 0);
		assertEquals(xcoder.encode(List.of()), 0);
		assertEquals(xcoder.encode(E.b), 0);
		assertEquals(xcoder.encode(E.a), E.a.value);
		assertEquals(xcoder.encode(E.b, E.c), E.c.value);
	}

	@Test
	public void shouldTranscodeMaskValues() {
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, mask(0x0e, 0), E.class);
		assertEquals(xcoder.encode(E.a), 0);
		assertEquals(xcoder.encode(E.c), E.c.value);
		assertEquals(xcoder.encode(E.a, E.c), E.c.value);
		assertEquals(xcoder.decode(0xd), E.c);
		assertCollection(xcoder.decodeAll(0xff), E.b, E.c);
	}

	@Test
	public void shouldTranscodeOverlappingMaskValues() {
		TypeTranscoder<E> xcoder = TypeTranscoder.<E>of(t -> t.value, mask(0x0e, 0), E.b, E.c);
		assertEquals(xcoder.encode(E.a), 0);
		assertEquals(xcoder.encode(E.c), E.c.value);
		assertEquals(xcoder.encode(E.a, E.c), E.c.value);
		assertEquals(xcoder.decode(0xd), E.c);
		assertCollection(xcoder.decodeAll(0xff), E.b, E.c);
	}

	@Test
	public void shouldTranscodeFields() {
		int[] store = { 0 };
		IntField accessor = IntField.of(() -> store[0], i -> store[0] = i);
		FieldTranscoder<E> field = xcoder.field(accessor);
		field.set(E.b);
		assertEquals(store[0], E.b.value);
		assertEquals(field.get(), E.b);
		field.set(E.a, E.c);
		assertEquals(store[0], E.a.value + E.c.value);
		assertCollection(field.getAll(), E.a, E.c);
	}

	@Test
	public void shouldTranscodeTypedFields() {
		FieldTranscoder.Typed<Holder, E> field = xcoder.field(Holder.acc);
		Holder h = new Holder();
		field.set(h, E.a, E.b);
		assertEquals(h.val, 3);
	}

	@SafeVarargs
	public static <T> void assertRemainder(Remainder<T> actual, int rem, T... ts) {
		assertRemainder(actual, rem, Arrays.asList(ts));
	}

	public static <T> void assertRemainder(Remainder<T> actual, int rem, Collection<T> ts) {
		assertEquals(actual.remainder, rem);
		assertCollection(actual.types, ts);
	}

}
