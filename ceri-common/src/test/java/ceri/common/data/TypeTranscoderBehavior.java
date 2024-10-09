package ceri.common.data;

import static ceri.common.data.Mask.ofInt;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
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
	public void shouldDecodeWithRemainder() {
		var rem = xcoder.decodeWithRemainder(0);
		assertRemainder(rem, 0);
		assertEquals(rem.isExact(), true);
		assertEquals(rem.isEmpty(), true);
		rem = xcoder.decodeWithRemainder(0xf);
		assertRemainder(rem, 0, E.a, E.b, E.c);
		assertEquals(rem.isExact(), true);
		assertEquals(rem.isEmpty(), false);
		rem = xcoder.decodeWithRemainder(7);
		assertRemainder(rem, 4, E.a, E.b);
		assertEquals(rem.isExact(), false);
		assertEquals(rem.isEmpty(), false);
		rem = xcoder.decodeWithRemainder(4);
		assertRemainder(rem, 4);
		assertEquals(rem.isExact(), false);
		assertEquals(rem.isEmpty(), false);
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
		assertCollection(xcoder.decodeAll(0));
		assertCollection(xcoder.decodeAll(4));
		assertCollection(xcoder.decodeAll(3), E.a, E.b);
		assertCollection(xcoder.decodeAll(15), E.a, E.b, E.c);
		assertCollection(xcoder.decodeAll(31), E.a, E.b, E.c);
	}

	@Test
	public void shouldDecodeValueWithDefault() {
		assertEquals(xcoder.decode(0, E.c), E.c);
		assertEquals(xcoder.decode(3, E.b), E.b);
		assertEquals(xcoder.decode(2, E.a), E.b);
	}

	@Test
	public void shouldDecodeOverlappingValues() {
		var array = Dup.values();
		var xcoder = TypeTranscoder.ofDup(t -> t.value, null, Dup.class);
		assertCollection(xcoder.decodeAll(3), Dup.a, Dup.b);
		ArrayUtil.reverse(array);
		xcoder = TypeTranscoder.ofDup(t -> t.value, null, Arrays.asList(array));
		assertCollection(xcoder.decodeAll(3), Dup.e);
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
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, null, List.of(E.a, E.c));
		assertEquals(xcoder.encodeInt(), 0);
		assertEquals(xcoder.encodeInt((E[]) null), 0);
		assertEquals(xcoder.encodeInt((List<E>) null), 0);
		assertEquals(xcoder.encodeInt(List.of()), 0);
		assertEquals(xcoder.encodeInt(E.b), 0);
		assertEquals(xcoder.encodeInt(E.a), E.a.value);
		assertEquals(xcoder.encodeInt(E.b, E.c), E.c.value);
	}

	@Test
	public void shouldTranscodeMaskValues() {
		TypeTranscoder<E> xcoder = TypeTranscoder.of(t -> t.value, ofInt(0, 0x0e), E.class);
		assertEquals(xcoder.encodeInt(E.a), 0);
		assertEquals(xcoder.encodeInt(E.c), E.c.value);
		assertEquals(xcoder.encodeInt(E.a, E.c), E.c.value);
		assertEquals(xcoder.decode(0xd), E.c);
		assertCollection(xcoder.decodeAll(0xff), E.b, E.c);
	}

	@Test
	public void shouldTranscodeOverlappingMaskValues() {
		TypeTranscoder<E> xcoder =
			TypeTranscoder.<E>of(t -> t.value, ofInt(0, 0x0e), List.of(E.b, E.c));
		assertEquals(xcoder.encodeInt(E.a), 0);
		assertEquals(xcoder.encodeInt(E.c), E.c.value);
		assertEquals(xcoder.encodeInt(E.a, E.c), E.c.value);
		assertEquals(xcoder.decode(0xd), E.c);
		assertCollection(xcoder.decodeAll(0xff), E.b, E.c);
	}

	@SafeVarargs
	private static <T> void assertRemainder(Remainder<T> actual, long rem, T... ts) {
		assertEquals(actual.diff(), rem);
		assertCollection(actual.types(), ts);
	}

}
