package ceri.common.data;

import static ceri.common.data.TypeTranscoderBehavior.assertRemainder;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.TypeTranscoder.Remainder;

public class FieldTranscoderBehavior {
	private final int[] store = { 0 };
	private final ValueField<RuntimeException> accessor =
		ValueField.ofInt(() -> store[0], i -> store[0] = i);
	private static final TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, E.class);
	private final FieldTranscoder<RuntimeException, E> field = xcoder.field(accessor);

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
		static ValueField.Typed<RuntimeException, Holder> accessor =
			ValueField.Typed.ofInt(h -> h.val, (h, i) -> h.val = i);
		static FieldTranscoder.Typed<RuntimeException, Holder, E> field =
			FieldTranscoder.Typed.of(Holder.accessor, xcoder);

		int val;
	}

	@Before
	public void init() {
		store[0] = 0;
	}

	@Test
	public void shouldConvertFromTypedToInstance() {
		Holder h = new Holder();
		h.val = E.c.value;
		var field = Holder.field.from(h);
		assertEquals(field.get(), E.c);
		field.set(E.a, E.b);
		assertEquals(h.val, 3);
	}

	@Test
	public void shouldAddValues() {
		field.add(E.a, E.b);
		assertCollection(field.getAll(), E.a, E.b);
		field.add();
		assertCollection(field.getAll(), E.a, E.b);
		field.add(Collections.emptySet());
		assertCollection(field.getAll(), E.a, E.b);
		field.add(E.a, E.c);
		assertCollection(field.getAll(), E.a, E.b, E.c);
	}

	@Test
	public void shouldRemoveValues() {
		field.set(E.a, E.b);
		assertCollection(field.getAll(), E.a, E.b);
		field.remove();
		assertCollection(field.getAll(), E.a, E.b);
		field.remove(Collections.emptySet());
		assertCollection(field.getAll(), E.a, E.b);
		field.remove(E.a, E.c);
		assertCollection(field.getAll(), E.b);
	}

	@Test
	public void shouldAccessWithRemainder() {
		assertRemainder(field.getWithRemainder(), 0);
		field.set(xcoder.decodeWithRemainder(15));
		assertRemainder(field.getWithRemainder(), 0, E.a, E.b, E.c);
		field.set(xcoder.decodeWithRemainder(11));
		assertRemainder(field.getWithRemainder(), 8, E.a, E.b);
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(field.get());
		assertCollection(field.getAll());
		store[0] = E.c.value;
		assertEquals(field.get(), E.c);
		store[0] = E.a.value + E.c.value;
		assertCollection(field.getAll(), E.a, E.c);
	}

	@Test
	public void shouldValidateFields() {
		assertTrue(field.isValid());
		field.set(E.c);
		assertTrue(field.isValid());
		store[0] = 6;
		assertFalse(field.isValid());
		field.set(E.a, E.c);
		assertTrue(field.isValid());
		store[0] = 6;
		assertFalse(field.isValid());
	}

	@Test
	public void shouldDetermineIfFieldHasValues() {
		assertFalse(field.has(E.a));
		assertFalse(field.hasAny(E.a, E.b, E.c));
		assertFalse(field.hasAll(E.a, E.b));
		field.add(E.a, E.b);
		assertTrue(field.has(E.a));
		assertTrue(field.hasAny(E.a, E.b, E.c));
		assertTrue(field.hasAll(E.a, E.b));
		assertFalse(field.hasAll(E.a, E.b, E.c));
	}

	@Test
	public void shouldSetValuesOnType() {
		Holder h = new Holder();
		h.val = 15;
		Holder.field.set(h, E.a, E.b);
		assertEquals(h.val, 3);
		Holder.field.set(h, E.c);
		assertEquals(h.val, 12);
	}

	@Test
	public void shouldSetRemainderValueOnType() {
		Holder h = new Holder();
		h.val = 0;
		Holder.field.set(h, Remainder.of(8, E.a, E.b));
		assertEquals(h.val, 11);
	}

	@Test
	public void shouldGetValueFromType() {
		Holder h = new Holder();
		h.val = 2;
		assertEquals(Holder.field.get(h), E.b);
		h.val = 4;
		assertNull(Holder.field.get(h));
	}

	@Test
	public void shouldGetValuesFromType() {
		Holder h = new Holder();
		h.val = 3;
		assertCollection(Holder.field.getAll(h), E.a, E.b);
	}

	@Test
	public void shouldGetRemainderFromType() {
		Holder h = new Holder();
		h.val = 7;
		assertRemainder(Holder.field.getWithRemainder(h), 4, E.a, E.b);
	}

	@Test
	public void shouldAddValuesToType() {
		Holder h = new Holder();
		h.val = 0;
		Holder.field.add(h, E.a, E.b);
		assertEquals(h.val, 3);
		Holder.field.add(h);
		Holder.field.add(h, Set.of());
		assertEquals(h.val, 3);
	}

	@Test
	public void shouldRemoveValuesFromType() {
		Holder h = new Holder();
		h.val = 15;
		Holder.field.remove(h, E.c, E.a);
		assertEquals(h.val, 2);
		Holder.field.remove(h);
		Holder.field.remove(h, Set.of());
		assertEquals(h.val, 2);
	}

	@Test
	public void shouldValidateType() {
		Holder h = new Holder();
		h.val = 0;
		assertTrue(Holder.field.isValid(h));
		h.val = 7;
		assertFalse(Holder.field.isValid(h));
		h.val = 3;
		assertTrue(Holder.field.isValid(h));
	}

	@Test
	public void shouldDetermineIfTypeHasValues() {
		Holder h = new Holder();
		h.val = 3;
		assertTrue(Holder.field.has(h, E.a));
		assertTrue(Holder.field.hasAny(h, E.a, E.c));
		assertFalse(Holder.field.hasAny(h, E.c));
		assertTrue(Holder.field.hasAll(h, E.a));
		assertTrue(Holder.field.hasAll(h, E.a, E.b));
		assertFalse(Holder.field.hasAll(h, E.a, E.b, E.c));
	}

}
