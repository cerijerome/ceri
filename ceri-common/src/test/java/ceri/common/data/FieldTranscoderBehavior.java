package ceri.common.data;

import static ceri.common.data.TypeTranscoderBehavior.assertRemainder;
import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class FieldTranscoderBehavior {
	private final int[] store = { 0 };
	private final IntAccessor accessor = IntAccessor.of(() -> store[0], i -> store[0] = i);
	private final TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, E.class);
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

}
