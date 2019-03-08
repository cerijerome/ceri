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
	private final FieldTranscoder.Single<E> single =
		TypeTranscoder.single(t -> t.value, E.class).field(accessor);
	private final FieldTranscoder.Flag<E> flag =
		TypeTranscoder.flag(t -> t.value, E.class).field(accessor);

	static enum E {
		a(1),
		b(2),
		c(12);

		public final int value;

		private E(int value) {
			this.value = value;
		}
	}

	@Before
	public void init() {
		store[0] = 0;
	}

	@Test
	public void shouldDecodeSingleValues() {
		assertNull(single.get());
		store[0] = E.c.value;
		assertThat(single.get(), is(E.c));
	}

	@Test
	public void shouldDecodeFlagValues() {
		assertCollection(flag.get());
		store[0] = E.a.value + E.c.value;
		assertCollection(flag.get(), E.a, E.c);
	}

	@Test
	public void shouldValidateSingleFields() {
		assertThat(single.isValid(), is(false));
		single.set(E.c);
		assertThat(single.isValid(), is(true));
		store[0] = 6;
		assertThat(single.isValid(), is(false));
	}

	@Test
	public void shouldValidateFlagFields() {
		assertThat(flag.isValid(), is(true));
		flag.set(E.a, E.c);
		assertThat(flag.isValid(), is(true));
		store[0] = 6;
		assertThat(flag.isValid(), is(false));
	}

}
