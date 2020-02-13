package ceri.common.data;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class FieldTranscoderBehavior {
	private final int[] store = { 0 };
	private final IntAccessor accessor = IntAccessor.of(() -> store[0], i -> store[0] = i);
	private final FieldTranscoder<E> field =
		TypeTranscoder.of(t -> t.value, E.class).field(accessor);

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
		field.add(E.a, E.c);
		assertCollection(field.getAll(), E.a, E.b, E.c);
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
