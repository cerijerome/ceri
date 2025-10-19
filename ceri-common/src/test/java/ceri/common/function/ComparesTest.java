package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertNpe;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import ceri.common.collect.Lists;

public class ComparesTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Compares.class);
	}

	@Test
	public void testNullsSafe() {
		assertEquals(Compares.Nulls.safe(null), Compares.Nulls.fail);
		assertEquals(Compares.Nulls.safe(Compares.Nulls.none), Compares.Nulls.fail);
		assertEquals(Compares.Nulls.safe(Compares.Nulls.first), Compares.Nulls.first);
		assertEquals(Compares.Nulls.safe(Compares.Nulls.last), Compares.Nulls.last);
		assertEquals(Compares.Nulls.safe(Compares.Nulls.fail), Compares.Nulls.fail);
	}

	@Test
	public void testNullsNot() {
		assertEquals(Compares.Nulls.not(null), Compares.Nulls.none);
		assertEquals(Compares.Nulls.not(Compares.Nulls.none), Compares.Nulls.none);
		assertEquals(Compares.Nulls.not(Compares.Nulls.first), Compares.Nulls.last);
		assertEquals(Compares.Nulls.not(Compares.Nulls.last), Compares.Nulls.first);
		assertEquals(Compares.Nulls.not(Compares.Nulls.fail), Compares.Nulls.fail);
	}

	@Test
	public void testNullNone() {
		Comparator<Integer> c = Compares.of(Compares.Nulls.none);
		assertNpe(() -> c.compare(null, null));
		assertNpe(() -> c.compare(null, 1));
		assertNpe(() -> c.compare(1, null));
		assertSort(c, l(0, 1, -1), -1, 0, 1);
	}

	@Test
	public void testNullFails() {
		Comparator<Integer> c = Compares.of(Compares.Nulls.fail);
		assertIllegalArg(() -> c.compare(null, null));
		assertIllegalArg(() -> c.compare(null, 1));
		assertIllegalArg(() -> c.compare(1, null));
		assertSort(c, l(0, 1, -1), -1, 0, 1);
	}

	@Test
	public void testSafe() {
		assertEquals(Compares.safe(null).compare(null, ""), 0);
		assertEquals(Compares.safe(Compares.INT).compare(null, 1), -1);
	}

	@Test
	public void testNot() {
		assertSort(Compares.not(null), l(0, "a", null, "", "ab"), null, 0, "a", "", "ab");
		assertSort(Compares.not(Compares.string()), l(1, "1", "A", null, "0"), null, "A", 1, "1",
			"0");
	}

	@Test
	public void testAsInt() {
		assertSort(Compares.asInt(null), l(0, null, 1, -1), null, 0, 1, -1);
		assertSort(Compares.asInt(String::length), l("a", " ", null, "", "ab"), null, "", "a", " ",
			"ab");
		assertSort(Compares.asInt(Compares.Nulls.last, String::length), l("a", " ", null, "", "ab"),
			"", "a", " ", "ab", null);
	}

	@Test
	public void testAsUint() {
		assertSort(Compares.asUint(null), l(0, 1, null, -1), null, 0, 1, -1);
		assertSort(Compares.asUint(Integer::parseInt), l("1", null, "0", "-1"), null, "0", "1",
			"-1");
		assertSort(Compares.asUint(Compares.Nulls.last, Integer::parseInt), l("1", null, "0", "-1"),
			"0", "1", "-1", null);
	}

	@Test
	public void testAsLong() {
		assertSort(Compares.asLong(null), l(0, null, 1, -1), null, 0, 1, -1);
		assertSort(Compares.asLong(String::length), l("a", " ", null, "", "ab"), null, "", "a", " ",
			"ab");
		assertSort(Compares.asLong(Compares.Nulls.last, String::length),
			l("a", " ", null, "", "ab"), "", "a", " ", "ab", null);
	}

	@Test
	public void testAsUlong() {
		assertSort(Compares.asUlong(null), l(0, 1, null, -1), null, 0, 1, -1);
		assertSort(Compares.asUlong(Integer::parseInt), l("1", null, "0", "-1"), null, "0", "1",
			"-1");
		assertSort(Compares.asUlong(Compares.Nulls.last, Integer::parseInt),
			l("1", null, "0", "-1"), "0", "1", "-1", null);
	}

	@Test
	public void testAsDouble() {
		assertSort(Compares.asDouble(null), l(0, null, 1, -1), null, 0, 1, -1);
		assertSort(Compares.asDouble(String::length), l("a", " ", null, "", "ab"), null, "", "a",
			" ", "ab");
		assertSort(Compares.asDouble(Compares.Nulls.last, String::length),
			l("a", " ", null, "", "ab"), "", "a", " ", "ab", null);
	}

	@SafeVarargs
	private static <T> void assertSort(Comparator<? super T> comparator, List<T> values,
		T... sorted) {
		assertEquals(Lists.sort(values, comparator), l(sorted));
	}

	@SafeVarargs
	private static <T> List<T> l(T... values) {
		return Lists.wrap(values);
	}
}
