package ceri.common.data;

import static ceri.common.data.MaskTranscoder.mask;
import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.List;
import org.junit.Test;

public class TypeTranscoderBehavior {
	private static final TypeTranscoder.Single<E> single =
		TypeTranscoder.single(t -> t.value, E.class);
	private static final TypeTranscoder.Flag<E> flag = TypeTranscoder.flag(t -> t.value, E.class);

	enum E {
		a(1),
		b(2),
		c(12);

		public final int value;

		E(int value) {
			this.value = value;
		}
	}

	@Test
	public void shouldReturnAllValues() {
		assertCollection(single.all(), E.a, E.b, E.c);
		assertCollection(flag.all(), E.a, E.b, E.c);
	}

	@Test
	public void shouldEncodeValues() {
		assertThat(single.encode(null), is(0));
		assertThat(single.encode(E.b), is(E.b.value));
		assertThat(flag.encode(), is(0));
		assertThat(flag.encode((E[]) null), is(0));
		assertThat(flag.encode((List<E>) null), is(0));
		assertThat(flag.encode(List.of()), is(0));
		assertThat(flag.encode(E.b, E.c), is(E.b.value + E.c.value));
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(single.decode(0));
		assertNull(single.decode(3));
		assertThat(single.decode(2), is(E.b));
		assertCollection(flag.decode(0));
		assertCollection(flag.decode(4));
		assertCollection(flag.decode(3), E.a, E.b);
		assertCollection(flag.decode(15), E.a, E.b, E.c);
		assertCollection(flag.decode(31), E.a, E.b, E.c);
	}

	@Test
	public void shouldValidateValues() {
		assertThat(single.isValid(0), is(false));
		assertThat(single.isValid(-1), is(false));
		assertThat(single.isValid(12), is(true));
		assertThat(single.isValid(3), is(false));
		assertThat(flag.isValid(0), is(true));
		assertThat(flag.isValid(-1), is(false));
		assertThat(flag.isValid(12), is(true));
		assertThat(flag.isValid(3), is(true));
		assertThat(flag.isValid(4), is(false));
	}

	@Test
	public void shouldEncodeSubset() {
		TypeTranscoder.Single<E> single = TypeTranscoder.single(t -> t.value, E.a, E.c);
		TypeTranscoder.Flag<E> flag = TypeTranscoder.flag(t -> t.value, E.a, E.c);
		assertThat(single.encode(null), is(0));
		assertThat(single.encode(E.b), is(0));
		assertThat(single.encode(E.a), is(E.a.value));
		assertThat(flag.encode(), is(0));
		assertThat(flag.encode((E[]) null), is(0));
		assertThat(flag.encode((List<E>) null), is(0));
		assertThat(flag.encode(List.of()), is(0));
		assertThat(flag.encode(E.b, E.c), is(E.c.value));
	}

	@Test
	public void shouldConvertBetweenSingleAndFlagTranscoders() {
		TypeTranscoder.Single<E> single = TypeTranscoderBehavior.flag.single();
		TypeTranscoder.Flag<E> flag = TypeTranscoderBehavior.single.flag();
		assertThat(single.encode(null), is(0));
		assertThat(single.encode(E.b), is(E.b.value));
		assertThat(flag.encode(), is(0));
		assertThat(flag.encode((E[]) null), is(0));
		assertThat(flag.encode((List<E>) null), is(0));
		assertThat(flag.encode(List.of()), is(0));
		assertThat(flag.encode(E.b, E.c), is(E.b.value + E.c.value));
	}

	@Test
	public void shouldTranscodeMaskValues() {
		TypeTranscoder.Single<E> single = TypeTranscoderBehavior.single.mask(mask(0x0e));
		TypeTranscoder.Flag<E> flag = TypeTranscoderBehavior.flag.mask(mask(0x0e));
		assertThat(single.encode(E.a), is(0));
		assertThat(single.encode(E.c), is(E.c.value));
		assertThat(flag.encode(E.a), is(0));
		assertThat(flag.encode(E.a, E.c), is(E.c.value));
		assertThat(single.decode(0xd), is(E.c));
		assertCollection(flag.decode(0xff), E.b, E.c);
	}

	@Test
	public void shouldTranscodeFields() {
		int[] store = { 0 };
		IntAccessor accessor = IntAccessor.of(() -> store[0], i -> store[0] = i);
		FieldTranscoder.Single<E> single = TypeTranscoderBehavior.single.field(accessor);
		FieldTranscoder.Flag<E> flag = TypeTranscoderBehavior.flag.field(accessor);
		single.set(E.b);
		assertThat(store[0], is(E.b.value));
		assertThat(single.get(), is(E.b));
		flag.set(E.a, E.c);
		assertThat(store[0], is(E.a.value + E.c.value));
		assertCollection(flag.get(), E.a, E.c);
	}

}
