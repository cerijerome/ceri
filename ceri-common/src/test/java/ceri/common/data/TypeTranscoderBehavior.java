package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
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

	enum Dup {
		a(1),
		b(2),
		c(2),
		d(1),
		e(3);

		public final int value;

		Dup(int value) {
			this.value = value;
		}
	}

	@Test
	public void shouldSkipValues() {
		var xc = TypeTranscoder.of(t -> t.value, E.class, E.a);
		assertUnordered(xc.decodeAll(-1), E.b, E.c);
	}

	@Test
	public void shouldProvideRemainderStringRepresentation() {
		Remainder<E> t = xcoder.decodeRemainder(7);
		assertString(t, "[a, b]+4");
	}

	@Test
	public void shouldDecodeRemainder() {
		var rem = xcoder.decodeRemainder(0);
		assertRemainder(rem, 0);
		assertEquals(rem.isExact(), true);
		assertEquals(rem.isEmpty(), true);
		rem = xcoder.decodeRemainder(0xf);
		assertRemainder(rem, 0, E.a, E.b, E.c);
		assertEquals(rem.isExact(), true);
		assertEquals(rem.isEmpty(), false);
		rem = xcoder.decodeRemainder(7);
		assertRemainder(rem, 4, E.a, E.b);
		assertEquals(rem.isExact(), false);
		assertEquals(rem.isEmpty(), false);
		rem = xcoder.decodeRemainder(4);
		assertRemainder(rem, 4);
		assertEquals(rem.isExact(), false);
		assertEquals(rem.isEmpty(), false);
	}

	@Test
	public void shouldDecodeRemainderWithoutReceiver() {
		assertEquals(xcoder.decodeRemainder(null, 0xf), 0L);
		assertEquals(xcoder.decodeRemainder(null, 7), 4L);
		assertEquals(xcoder.decodeRemainderInt(null, 7), 4);
	}

	@Test
	public void shouldCreateRemainder() {
		var rem = TypeTranscoder.Remainder.of(4, E.a, E.b);
		assertEquals(rem.diffInt(), 4);
		assertEquals(rem.isExact(), false);
		assertEquals(rem.isEmpty(), false);
	}

	@Test
	public void shouldDetermineIfRemainderIsEmpty() {
		assertTrue(xcoder.decodeRemainder(0).isEmpty());
		assertFalse(xcoder.decodeRemainder(4).isEmpty());
		assertFalse(xcoder.decodeRemainder(1).isEmpty());
		assertFalse(xcoder.decodeRemainder(5).isEmpty());
	}

	@Test
	public void shouldDetermineIfRemainderIsExact() {
		assertTrue(xcoder.decodeRemainder(0).isExact());
		assertTrue(xcoder.decodeRemainder(1).isExact());
		assertTrue(xcoder.decodeRemainder(3).isExact());
		assertFalse(xcoder.decodeRemainder(4).isExact());
		assertFalse(xcoder.decodeRemainder(5).isExact());
	}

	@Test
	public void shouldDecodeFirstMatch() {
		assertEquals(xcoder.decodeFirst(0), null);
		assertEquals(xcoder.decodeFirst(2), E.b);
		assertEquals(xcoder.decodeFirst(0xe), E.b);
		assertEquals(xcoder.decodeFirst(8), null);
	}

	@Test
	public void shouldReturnAllValues() {
		assertUnordered(xcoder.all(), E.a, E.b, E.c);
	}

	@Test
	public void shouldEncodeAllValues() {
		assertEquals(xcoder.encodeAll(), 15L);
		assertEquals(xcoder.encodeAllInt(), 15);
	}

	@Test
	public void shouldEncodeValues() {
		assertEquals(xcoder.encodeInt(), 0);
		assertEquals(xcoder.encodeInt((E[]) null), 0);
		assertEquals(xcoder.encodeInt((List<E>) null), 0);
		assertEquals(xcoder.encodeInt(List.of()), 0);
		assertEquals(xcoder.encodeInt(E.b), E.b.value);
		assertEquals(xcoder.encodeInt(E.b, E.c), E.b.value + E.c.value);
	}

	@Test
	public void shouldDecodeValues() {
		assertNull(xcoder.decode(0));
		assertNull(xcoder.decode(3));
		assertEquals(xcoder.decode(2), E.b);
		assertUnordered(xcoder.decodeAll(0));
		assertUnordered(xcoder.decodeAll(4));
		assertUnordered(xcoder.decodeAll(3), E.a, E.b);
		assertUnordered(xcoder.decodeAll(15), E.a, E.b, E.c);
		assertUnordered(xcoder.decodeAll(31), E.a, E.b, E.c);
	}

	@Test
	public void shouldDecodeValueWithDefault() {
		assertEquals(xcoder.decode(0, E.c), E.c);
		assertEquals(xcoder.decode(3, E.b), E.b);
		assertEquals(xcoder.decode(2, E.a), E.b);
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
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, List.of(E.a, E.c));
		assertEquals(xcoder.encodeInt(), 0);
		assertEquals(xcoder.encodeInt((E[]) null), 0);
		assertEquals(xcoder.encodeInt((List<E>) null), 0);
		assertEquals(xcoder.encodeInt(List.of()), 0);
		assertEquals(xcoder.encodeInt(E.b), 0);
		assertEquals(xcoder.encodeInt(E.a), E.a.value);
		assertEquals(xcoder.encodeInt(E.b, E.c), E.c.value);
	}

	@SafeVarargs
	private static <T> void assertRemainder(Remainder<T> actual, long rem, T... ts) {
		assertEquals(actual.diff(), rem);
		assertUnordered(actual.types(), ts);
	}

}
