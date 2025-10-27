package ceri.common.function;

import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import ceri.common.collect.Lists;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;

public class ComparesTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Compares.class);
	}

	@Test
	public void testNullsSafe() {
		Assert.equal(Compares.Nulls.safe(null), Compares.Nulls.fail);
		Assert.equal(Compares.Nulls.safe(Compares.Nulls.none), Compares.Nulls.fail);
		Assert.equal(Compares.Nulls.safe(Compares.Nulls.first), Compares.Nulls.first);
		Assert.equal(Compares.Nulls.safe(Compares.Nulls.last), Compares.Nulls.last);
		Assert.equal(Compares.Nulls.safe(Compares.Nulls.fail), Compares.Nulls.fail);
	}

	@Test
	public void testNullsNot() {
		Assert.equal(Compares.Nulls.not(null), Compares.Nulls.none);
		Assert.equal(Compares.Nulls.not(Compares.Nulls.none), Compares.Nulls.none);
		Assert.equal(Compares.Nulls.not(Compares.Nulls.first), Compares.Nulls.last);
		Assert.equal(Compares.Nulls.not(Compares.Nulls.last), Compares.Nulls.first);
		Assert.equal(Compares.Nulls.not(Compares.Nulls.fail), Compares.Nulls.fail);
	}

	@Test
	public void testNullNone() {
		var c = Compares.<Integer>of(Compares.Nulls.none);
		Assert.nullPointer(() -> c.compare(null, null));
		Assert.nullPointer(() -> c.compare(null, 1));
		Assert.nullPointer(() -> c.compare(1, null));
		assertSort(c, l(0, 1, -1), -1, 0, 1);
	}

	@Test
	public void testNullFails() {
		var c = Compares.<Integer>of(Compares.Nulls.fail);
		Assert.illegalArg(() -> c.compare(null, null));
		Assert.illegalArg(() -> c.compare(null, 1));
		Assert.illegalArg(() -> c.compare(1, null));
		assertSort(c, l(0, 1, -1), -1, 0, 1);
	}

	@Test
	public void testApply() throws Exception {
		Assert.equal(Compares.apply(null), null);
		Assert.stream(Compares.apply(Streams.of(-1, null, 1, 0)::sorted), null, -1, 0, 1);
	}

	@Test
	public void testSafe() {
		Assert.equal(Compares.safe(null).compare(null, ""), 0);
		Assert.equal(Compares.safe(Compares.INT).compare(null, 1), -1);
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
		Assert.equal(Lists.sort(values, comparator), l(sorted));
	}

	@SafeVarargs
	private static <T> List<T> l(T... values) {
		return Lists.wrap(values);
	}
}
