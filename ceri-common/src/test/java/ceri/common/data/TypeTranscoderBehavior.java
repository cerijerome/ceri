package ceri.common.data;

import static ceri.common.data.MaskTranscoder.mask;
import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
		assertThat(xcoder.decodeWithRemainder(0).isEmpty(), is(true));
		assertThat(xcoder.decodeWithRemainder(4).isEmpty(), is(false));
		assertThat(xcoder.decodeWithRemainder(1).isEmpty(), is(false));
		assertThat(xcoder.decodeWithRemainder(5).isEmpty(), is(false));
	}

	@Test
	public void shouldDetermineIfRemainderIsExact() {
		assertThat(xcoder.decodeWithRemainder(0).isExact(), is(true));
		assertThat(xcoder.decodeWithRemainder(1).isExact(), is(true));
		assertThat(xcoder.decodeWithRemainder(3).isExact(), is(true));
		assertThat(xcoder.decodeWithRemainder(4).isExact(), is(false));
		assertThat(xcoder.decodeWithRemainder(5).isExact(), is(false));
	}

	@Test
	public void shouldReturnAllValues() {
		assertCollection(xcoder.all(), E.a, E.b, E.c);
	}

	@Test
	public void shouldEncodeValues() {
		assertThat(xcoder.encode(), is(0));
		assertThat(xcoder.encode((E[]) null), is(0));
		assertThat(xcoder.encode((List<E>) null), is(0));
		assertThat(xcoder.encode(List.of()), is(0));
		assertThat(xcoder.encode((Remainder<E>) null), is(0));
		assertThat(xcoder.encode(E.b), is(E.b.value));
		assertThat(xcoder.encode(E.b, E.c), is(E.b.value + E.c.value));
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(xcoder.decode(0));
		assertNull(xcoder.decode(3));
		assertThat(xcoder.decode(2), is(E.b));
		assertCollection(xcoder.decodeAll(0));
		assertCollection(xcoder.decodeAll(4));
		assertCollection(xcoder.decodeAll(3), E.a, E.b);
		assertCollection(xcoder.decodeAll(15), E.a, E.b, E.c);
		assertCollection(xcoder.decodeAll(31), E.a, E.b, E.c);
	}

	@Test
	public void shouldDetermineIfValueHasAnyTypes() {
		assertThat(xcoder.hasAny(0, E.a, E.b, E.c), is(false));
		assertThat(xcoder.hasAny(1, E.a, E.b, E.c), is(true));
		assertThat(xcoder.hasAny(13, E.a, E.b, E.c), is(true));
		assertThat(xcoder.hasAny(13, E.b), is(false));
	}

	@Test
	public void shouldDetermineIfValueHasAllTypes() {
		assertThat(xcoder.hasAll(0), is(true));
		assertThat(xcoder.hasAll(1, E.a), is(true));
		assertThat(xcoder.hasAll(13, E.a, E.c), is(true));
		assertThat(xcoder.hasAll(13, E.a, E.b, E.c), is(false));
		assertThat(xcoder.hasAll(13, E.b), is(false));
	}

	@Test
	public void shouldValidateValues() {
		assertThat(xcoder.isValid(0), is(true));
		assertThat(xcoder.isValid(-1), is(false));
		assertThat(xcoder.isValid(12), is(true));
		assertThat(xcoder.isValid(3), is(true));
		assertThat(xcoder.isValid(4), is(false));
	}

	@Test
	public void shouldEncodeSubset() {
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, E.a, E.c);
		assertThat(xcoder.encode(), is(0));
		assertThat(xcoder.encode((E[]) null), is(0));
		assertThat(xcoder.encode((List<E>) null), is(0));
		assertThat(xcoder.encode(List.of()), is(0));
		assertThat(xcoder.encode(E.b), is(0));
		assertThat(xcoder.encode(E.a), is(E.a.value));
		assertThat(xcoder.encode(E.b, E.c), is(E.c.value));
	}

	@Test
	public void shouldTranscodeMaskValues() {
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, mask(0x0e), E.class);
		assertThat(xcoder.encode(E.a), is(0));
		assertThat(xcoder.encode(E.c), is(E.c.value));
		assertThat(xcoder.encode(E.a, E.c), is(E.c.value));
		assertThat(xcoder.decode(0xd), is(E.c));
		assertCollection(xcoder.decodeAll(0xff), E.b, E.c);
	}

	@Test
	public void shouldTranscodeOverlappingMaskValues() {
		TypeTranscoder<E> xcoder = TypeTranscoder.<E>of(t -> t.value, mask(0x0e), E.b, E.c);
		assertThat(xcoder.encode(E.a), is(0));
		assertThat(xcoder.encode(E.c), is(E.c.value));
		assertThat(xcoder.encode(E.a, E.c), is(E.c.value));
		assertThat(xcoder.decode(0xd), is(E.c));
		assertCollection(xcoder.decodeAll(0xff), E.b, E.c);
	}

	@Test
	public void shouldTranscodeFields() {
		int[] store = { 0 };
		IntAccessor accessor = IntAccessor.of(() -> store[0], i -> store[0] = i);
		FieldTranscoder<E> field = xcoder.field(accessor);
		field.set(E.b);
		assertThat(store[0], is(E.b.value));
		assertThat(field.get(), is(E.b));
		field.set(E.a, E.c);
		assertThat(store[0], is(E.a.value + E.c.value));
		assertCollection(field.getAll(), E.a, E.c);
	}

	@SafeVarargs
	public static <T> void assertRemainder(Remainder<T> actual, int rem, T... ts) {
		assertRemainder(actual, rem, Arrays.asList(ts));
	}

	public static <T> void assertRemainder(Remainder<T> actual, int rem, Collection<T> ts) {
		assertThat(actual.remainder, is(rem));
		assertCollection(actual.types, ts);
	}

}
