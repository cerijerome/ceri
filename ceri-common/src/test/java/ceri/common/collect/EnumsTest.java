package ceri.common.collect;

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
		Assert.ordered(sort(Enums.Compare.ordinal(), array()), null, E.a, E.B);
		Assert.ordered(sort(Enums.Compare.name(), array()), null, E.B, E.a);
		Assert.ordered(sort(Enums.Compare.name(scomp), array()), null, E.a, E.B);
	}

	@Test
	public void testPredicates() throws Exception {
		Assert.equal(Enums.Filter.name(nullString).test(null), false);
		Assert.equal(Enums.Filter.name(nullString).test(E.a), false);
		Assert.equal(Enums.Filter.name("a").test(null), false);
		Assert.equal(Enums.Filter.name("a").test(E.B), false);
		Assert.equal(Enums.Filter.name("a").test(E.a), true);
		Assert.equal(Enums.Filter.name(spred).test(null), false);
		Assert.equal(Enums.Filter.name(spred).test(E.B), false);
		Assert.equal(Enums.Filter.name(spred).test(E.a), true);
	}

	@SafeVarargs
	private static <T> List<T> sort(Comparator<? super T> comparator, T... values) {
		return Lists.sort(Lists.ofAll(values), comparator);
	}

	@Test
	public void testOf() {
		Assert.ordered(Enums.of(nullClass));
		Assert.ordered(Enums.of(String.class)); // no enums
		Assert.ordered(Enums.of(None.class));
		Assert.ordered(Enums.of(E.class), E.a, E.B, E.c);
	}

	@Test
	public void testSet() {
		Assert.unordered(Enums.set(nullClass));
		Assert.unordered(Enums.set(None.class));
		Assert.unordered(Enums.set(E.class), E.a, E.B, E.c);
		Assert.unordered(Enums.<E>set());
		Assert.unordered(Enums.set(nullArray));
		Assert.unordered(Enums.set(emptyArray));
		Assert.unordered(Enums.set(E.a, null, E.B), E.a, null, E.B);
		Assert.unordered(Enums.set(E.a, E.B), E.a, E.B);
	}

	@Test
	public void testName() {
		Assert.equal(Enums.name(null), null);
		Assert.equal(Enums.name(E.a), "a");
	}

	@Test
	public void testShortName() {
		Assert.equal(Enums.shortName(null), "null");
		Assert.equal(Enums.shortName((None) null), "null");
		Assert.equal(Enums.shortName(E.a), "a");
		Assert.equal(Enums.shortName(E.B), "B");
		Assert.equal(Enums.shortName(E.c), "c");
		Assert.equal(Enums.shortName(Prefix.a_b_c_123), "123");
		Assert.equal(Enums.shortName(Prefix.a_b_c_12345), "12345");
		Assert.equal(Enums.shortName(Prefix.a_b_c_123456), "123456");
		Assert.equal(Enums.shortName(Prefix2.abc), "abc");
		Assert.equal(Enums.shortName(Prefix2.abcd), "abcd");
		Assert.equal(Enums.shortName(Prefix2.abcde), "abcde");
	}

	@Test
	public void testValueAccessor() {
		Assert.illegalArg(() -> Enums.valueAccessor(null));
		Assert.illegalArg(() -> Enums.valueAccessor(Prefix.class));
		Assert.illegalArg(() -> Enums.valueAccessor(E.class, "values"));
		Assert.illegalArg(() -> Enums.valueAccessor(E.class, "ch"));
		Assert.equal(Enums.valueAccessor(E.class).apply(null), null);
		Assert.equal(Enums.valueAccessor(E.class).apply(E.a), 1L);
		Assert.equal(Enums.valueAccessor(E.class, "value").apply(E.B), 2L);
	}

	@Test
	public void testValueOf() {
		Assert.equal(Enums.valueOf(nullClass, "a", E.a), E.a);
		Assert.equal(Enums.valueOf(E.class, "a"), E.a);
		Assert.isNull(Enums.valueOf(E.class, "ab"));
		Assert.isNull(Enums.valueOf(E.class, null, null));
		Assert.equal(Enums.valueOf(E.class, "B", null), E.B);
		Assert.equal(Enums.valueOf(E.class, null, E.a), E.a);
		Assert.equal(Enums.valueOf(E.class, "ab", E.c), E.c);
		Assert.isNull(Enums.valueOf(E.class, "ab", null));
		Assert.equal(Enums.valueOf("a", E.c), E.a);
		Assert.equal(Enums.valueOf("ab", E.c), E.c);
		Assert.equal(Enums.valueOf("a", (E) null), null);
	}

	@Test
	public void testFind() {
		Assert.equal(Enums.find(E.class, t -> t != E.a), E.B);
		Assert.equal(Enums.find(E.class, t -> t.name().equals("c")), E.c);
		Assert.equal(Enums.find(E.class, t -> t.name().equals("x")), null);
		Assert.equal(Enums.find(E.class, t -> t.name().equals("x"), E.B), E.B);
	}
}
