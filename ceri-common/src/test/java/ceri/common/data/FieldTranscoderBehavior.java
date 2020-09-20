package ceri.common.data;

import static ceri.common.data.TypeTranscoderBehavior.assertRemainder;
import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.TypeTranscoder.Remainder;

public class FieldTranscoderBehavior {
	private final int[] store = { 0 };
	private final IntAccessor accessor = IntAccessor.of(() -> store[0], i -> store[0] = i);
	private static final TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, E.class);
	private final FieldTranscoder<E> field = xcoder.field(accessor);

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
		static IntAccessor.Typed<Holder> accessor =
			IntAccessor.typed(h -> h.val, (h, i) -> h.val = i);
		static FieldTranscoder.Typed<Holder, E> field =
			FieldTranscoder.Typed.of(Holder.accessor, xcoder);

		int val;
	}

	@Before
	public void init() {
		store[0] = 0;
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
		assertThat(field.get(), is(E.c));
		store[0] = E.a.value + E.c.value;
		assertCollection(field.getAll(), E.a, E.c);
	}

	@Test
	public void shouldValidateFields() {
		assertThat(field.isValid(), is(true));
		field.set(E.c);
		assertThat(field.isValid(), is(true));
		store[0] = 6;
		assertThat(field.isValid(), is(false));
		field.set(E.a, E.c);
		assertThat(field.isValid(), is(true));
		store[0] = 6;
		assertThat(field.isValid(), is(false));
	}

	@Test
	public void shouldDetermineIfFieldHasValues() {
		assertThat(field.has(E.a), is(false));
		assertThat(field.hasAny(E.a, E.b, E.c), is(false));
		assertThat(field.hasAll(E.a, E.b), is(false));
		field.add(E.a, E.b);
		assertThat(field.has(E.a), is(true));
		assertThat(field.hasAny(E.a, E.b, E.c), is(true));
		assertThat(field.hasAll(E.a, E.b), is(true));
		assertThat(field.hasAll(E.a, E.b, E.c), is(false));
	}

	@Test
	public void shouldSetValuesOnType() {
		Holder h = new Holder();
		h.val = 15;
		Holder.field.set(h, E.a, E.b);
		assertThat(h.val, is(3));
		Holder.field.set(h, E.c);
		assertThat(h.val, is(12));
	}

	@Test
	public void shouldSetRemainderValueOnType() {
		Holder h = new Holder();
		h.val = 0;
		Holder.field.set(h, Remainder.of(8, E.a, E.b));
		assertThat(h.val, is(11));
	}

	@Test
	public void shouldGetValueFromType() {
		Holder h = new Holder();
		h.val = 2;
		assertThat(Holder.field.get(h), is(E.b));
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
		assertThat(h.val, is(3));
		Holder.field.add(h);
		Holder.field.add(h, Set.of());
		assertThat(h.val, is(3));
	}

	@Test
	public void shouldRemoveValuesFromType() {
		Holder h = new Holder();
		h.val = 15;
		Holder.field.remove(h, E.c, E.a);
		assertThat(h.val, is(2));
		Holder.field.remove(h);
		Holder.field.remove(h, Set.of());
		assertThat(h.val, is(2));
	}

	@Test
	public void shouldValidateType() {
		Holder h = new Holder();
		h.val = 0;
		assertThat(Holder.field.isValid(h), is(true));
		h.val = 7;
		assertThat(Holder.field.isValid(h), is(false));
		h.val = 3;
		assertThat(Holder.field.isValid(h), is(true));
	}

	@Test
	public void shouldDetermineIfTypeHasValues() {
		Holder h = new Holder();
		h.val = 3;
		assertThat(Holder.field.has(h, E.a), is(true));
		assertThat(Holder.field.hasAny(h, E.a, E.c), is(true));
		assertThat(Holder.field.hasAny(h, E.c), is(false));
		assertThat(Holder.field.hasAll(h, E.a), is(true));
		assertThat(Holder.field.hasAll(h, E.a, E.b), is(true));
		assertThat(Holder.field.hasAll(h, E.a, E.b, E.c), is(false));
	}

}
