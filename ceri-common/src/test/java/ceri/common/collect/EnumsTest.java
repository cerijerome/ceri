package ceri.common.collect;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertUnordered;
import static ceri.common.test.Assert.illegalArg;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.text.Strings;

public class EnumsTest {
	private static final String nullString = null;
	private static final Class<E> nullClass = null;
	private static final E[] nullArray = null;
	private static final E[] emptyArray = new E[0];
	private static final List<E> list = Lists.ofAll(E.a, null, E.B);
	private static final Comparator<String> scomp = Comparator.comparing(Strings::lower);
	private static final Functions.Predicate<String> spred =
		s -> Objects.equals(s, Strings.lower(s));

	private static enum None {}

	public static enum E {
		a,
		B,
		c;

		public final int value;
		public final char ch;

		private E() {
			value = ordinal() + 1;
			ch = (char) ('A' + value);
		}
	}

	private static enum Prefix {
		a_b_c_123,
		a_b_c_123456,
		a_b_c_12345;
	}

	private static enum Prefix2 {
		abc,
		abcd,
		abcde;
	}

	private static E[] array() {
		return list.toArray(E[]::new);
	}

	@Test
	public void testComparators() {
		assertOrdered(sort(Enums.Compare.ordinal(), array()), null, E.a, E.B);
		assertOrdered(sort(Enums.Compare.name(), array()), null, E.B, E.a);
		assertOrdered(sort(Enums.Compare.name(scomp), array()), null, E.a, E.B);
	}

	@Test
	public void testPredicates() throws Exception {
		assertEquals(Enums.Filter.name(nullString).test(null), false);
		assertEquals(Enums.Filter.name(nullString).test(E.a), false);
		assertEquals(Enums.Filter.name("a").test(null), false);
		assertEquals(Enums.Filter.name("a").test(E.B), false);
		assertEquals(Enums.Filter.name("a").test(E.a), true);
		assertEquals(Enums.Filter.name(spred).test(null), false);
		assertEquals(Enums.Filter.name(spred).test(E.B), false);
		assertEquals(Enums.Filter.name(spred).test(E.a), true);
	}

	@SafeVarargs
	private static <T> List<T> sort(Comparator<? super T> comparator, T... values) {
		return Lists.sort(Lists.ofAll(values), comparator);
	}

	@Test
	public void testOf() {
		assertOrdered(Enums.of(nullClass));
		assertOrdered(Enums.of(String.class)); // no enums
		assertOrdered(Enums.of(None.class));
		assertOrdered(Enums.of(E.class), E.a, E.B, E.c);
	}

	@Test
	public void testSet() {
		assertUnordered(Enums.set(nullClass));
		assertUnordered(Enums.set(None.class));
		assertUnordered(Enums.set(E.class), E.a, E.B, E.c);
		assertUnordered(Enums.<E>set());
		assertUnordered(Enums.set(nullArray));
		assertUnordered(Enums.set(emptyArray));
		assertUnordered(Enums.set(E.a, null, E.B), E.a, null, E.B);
		assertUnordered(Enums.set(E.a, E.B), E.a, E.B);
	}

	@Test
	public void testName() {
		assertEquals(Enums.name(null), null);
		assertEquals(Enums.name(E.a), "a");
	}

	@Test
	public void testShortName() {
		assertEquals(Enums.shortName(null), "null");
		assertEquals(Enums.shortName((None) null), "null");
		assertEquals(Enums.shortName(E.a), "a");
		assertEquals(Enums.shortName(E.B), "B");
		assertEquals(Enums.shortName(E.c), "c");
		assertEquals(Enums.shortName(Prefix.a_b_c_123), "123");
		assertEquals(Enums.shortName(Prefix.a_b_c_12345), "12345");
		assertEquals(Enums.shortName(Prefix.a_b_c_123456), "123456");
		assertEquals(Enums.shortName(Prefix2.abc), "abc");
		assertEquals(Enums.shortName(Prefix2.abcd), "abcd");
		assertEquals(Enums.shortName(Prefix2.abcde), "abcde");
	}

	@Test
	public void testValueAccessor() {
		illegalArg(() -> Enums.valueAccessor(null));
		illegalArg(() -> Enums.valueAccessor(Prefix.class));
		illegalArg(() -> Enums.valueAccessor(E.class, "values"));
		illegalArg(() -> Enums.valueAccessor(E.class, "ch"));
		assertEquals(Enums.valueAccessor(E.class).apply(null), null);
		assertEquals(Enums.valueAccessor(E.class).apply(E.a), 1L);
		assertEquals(Enums.valueAccessor(E.class, "value").apply(E.B), 2L);
	}

	@Test
	public void testValueOf() {
		assertEquals(Enums.valueOf(nullClass, "a", E.a), E.a);
		assertEquals(Enums.valueOf(E.class, "a"), E.a);
		Assert.isNull(Enums.valueOf(E.class, "ab"));
		Assert.isNull(Enums.valueOf(E.class, null, null));
		assertEquals(Enums.valueOf(E.class, "B", null), E.B);
		assertEquals(Enums.valueOf(E.class, null, E.a), E.a);
		assertEquals(Enums.valueOf(E.class, "ab", E.c), E.c);
		Assert.isNull(Enums.valueOf(E.class, "ab", null));
		assertEquals(Enums.valueOf("a", E.c), E.a);
		assertEquals(Enums.valueOf("ab", E.c), E.c);
		assertEquals(Enums.valueOf("a", (E) null), null);
	}

	@Test
	public void testFind() {
		assertEquals(Enums.find(E.class, t -> t != E.a), E.B);
		assertEquals(Enums.find(E.class, t -> t.name().equals("c")), E.c);
		assertEquals(Enums.find(E.class, t -> t.name().equals("x")), null);
		assertEquals(Enums.find(E.class, t -> t.name().equals("x"), E.B), E.B);
	}
}
