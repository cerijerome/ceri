package ceri.common.function;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertString;
import org.junit.Test;
import ceri.common.collect.Lists;

public class ScorerTest {
	private static final Scorer<Object> LEN = o -> String.valueOf(o).length();

	@Test
	public void testResultFromScorer() {
		assertResult(Scorer.Result.ofNull(), null, 0.0);
		assertResult(LEN.result(null), null, 0.0);
		assertResult(LEN.result(""), "", 0.0);
		assertResult(LEN.result("abc"), "abc", 3.0);
	}

	@Test
	public void testResultComparison() {
		var a = new Scorer.Result<>("A", 0.1);
		var b = new Scorer.Result<>("B", 0.1);
		var c = new Scorer.Result<>("B", -0.1);
		assertEquals(a.compareTo(b), 0);
		assertEquals(b.compareTo(c), -1);
		assertEquals(c.compareTo(a), 1);
	}

	@Test
	public void testResultStringRepresentation() {
		assertString(r(null, 0), "null=0.0");
		assertString(r("test", 123), "test=123.0");
	}

	@Test
	public void testLookup() {
		var lookup = Scorer.Lookup.builder().build(false);
		assertEquals(lookup.score(null), 0.0);
		assertEquals(lookup.score("abc"), 0.0);
		lookup = Scorer.Lookup.builder().build(true);
		assertEquals(lookup.score(null), 0.0);
		assertEquals(lookup.score("abc"), 0.0);
		lookup = Scorer.Lookup.builder().put(4, "abc", "ab").put(2, "a").build(false);
		assertEquals(lookup.score(null), 0.0);
		assertEquals(lookup.score("abc"), 4.0);
		assertEquals(lookup.score("a"), 2.0);
		assertEquals(lookup.score("b"), 0.0);
		lookup = Scorer.Lookup.builder().put(4, "abc", "ab").put(2, "a").build(true);
		assertEquals(lookup.score(null), 0.0);
		assertEquals(lookup.score("abc"), 0.4);
		assertEquals(lookup.score("a"), 0.2);
		assertEquals(lookup.score("b"), 0.0);
	}

	@Test
	public void testLookupScore() {
		var lookup = Scorer.Lookup.builder().put(4, "abc", "ab").put(2, "a").build(true);
		assertEquals(lookup.score(LEN, null), 0.0);
		assertEquals(lookup.score(LEN, "abc"), 0.4);
		assertEquals(lookup.score(LEN, "a"), 0.2);
		assertEquals(lookup.score(LEN, "b"), 1.0);
	}

	@Test
	public void testValue() {
		assertEquals(Scorer.value(null), 0.0);
		assertEquals(Scorer.value(1), 1.0);
		assertEquals(Scorer.value(3L), 3.0);
		assertEquals(Scorer.value(-1f), -1.0);
		assertEquals(Scorer.value(Double.NaN), Double.NaN);
	}

	@Test
	public void testScore() {
		assertEquals(Scorer.score(null, "a"), 0.0);
		assertEquals(Scorer.score(LEN, null), 0.0);
		assertEquals(Scorer.score(LEN, ""), 0.0);
		assertEquals(Scorer.score(LEN, "abc"), 3.0);
	}

	@Test
	public void testResult() {
		assertResult(Scorer.result(null, "a"), "a", 0.0);
		assertResult(Scorer.result(LEN, null), null, 0.0);
		assertResult(Scorer.result(LEN, ""), "", 0.0);
		assertResult(Scorer.result(LEN, "abc"), "abc", 3.0);
	}

	@Test
	public void testConstant() {
		assertEquals(Scorer.constant(0.1).score(null), 0.1);
		assertEquals(Scorer.constant(0.1).score(1), 0.1);
	}

	@Test
	public void testScoring() {
		assertEquals(Scorer.scoring(null, LEN).score(1), 0.0);
		assertEquals(Scorer.scoring(String::valueOf, null).score(1), 0.0);
		assertEquals(Scorer.scoring(String::valueOf, LEN).score(null), 0.0);
		assertEquals(Scorer.scoring(String::valueOf, LEN).score(1), 1.0);
		assertEquals(Scorer.scoring(String::valueOf, LEN).score(1.01), 4.0);
	}

	@Test
	public void testResults() {
		assertOrdered(Scorer.results(null, "", null, "abc", "a"), r("", 0), r(null, 0), r("abc", 0),
			r("a", 0));
		assertOrdered(Scorer.results(LEN, (String[]) null));
		assertOrdered(Scorer.results(LEN));
		assertOrdered(Scorer.results(LEN, "", null, "abc", "a"), r("abc", 3), r("a", 1), r("", 0),
			r(null, 0));
	}

	@Test
	public void testSort() {
		assertEquals(Scorer.sort(null, LEN), null);
		assertOrdered(Scorer.sort(Lists.ofAll("", null, "abc", "a"), null), "", null, "abc", "a");
		assertOrdered(Scorer.sort(Lists.ofAll("", null, "abc", "a"), LEN), "abc", "a", "", null);
	}

	@Test
	public void testFilter() throws Exception {
		assertEquals(Scorer.filter(null, d -> d > 1).test("abc"), false);
		assertEquals(Scorer.filter(LEN, null).test("abc"), false);
		assertEquals(Scorer.filter(LEN, d -> d > 1).test(null), false);
		assertEquals(Scorer.filter(LEN, d -> d > 1).test(""), false);
		assertEquals(Scorer.filter(LEN, d -> d > 1).test("a"), false);
		assertEquals(Scorer.filter(LEN, d -> d > 1).test("abc"), true);
	}

	@Test
	public void testFilterWithinLimits() {
		assertEquals(Scorer.filter(null, 1.0, 3.0).test("ab"), false);
		assertEquals(Scorer.filter(LEN, null, null).test("abc"), true);
		assertEquals(Scorer.filter(LEN, 2.0, null).test("a"), false);
		assertEquals(Scorer.filter(LEN, 2.0, null).test("ab"), true);
		assertEquals(Scorer.filter(LEN, 2.0, null).test("abc"), true);
		assertEquals(Scorer.filter(LEN, null, 2.0).test("a"), true);
		assertEquals(Scorer.filter(LEN, null, 2.0).test("ab"), true);
		assertEquals(Scorer.filter(LEN, null, 2.0).test("abc"), false);
		assertEquals(Scorer.filter(LEN, 1.0, 2.0).test(""), false);
		assertEquals(Scorer.filter(LEN, 1.0, 2.0).test("a"), true);
		assertEquals(Scorer.filter(LEN, 1.0, 2.0).test("ab"), true);
		assertEquals(Scorer.filter(LEN, 1.0, 2.0).test("abc"), false);
	}

	@Test
	public void testMultiplied() {
		assertEquals(Scorer.multiplied().score(null), 0.0);
		assertEquals(Scorer.multiplied().score("abc"), 0.0);
		var scorer = Scorer.multiplied(LEN, LEN, _ -> 0.5);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score("a"), 0.5);
		assertEquals(scorer.score("abc"), 4.5);
	}

	@Test
	public void testAveraged() {
		assertEquals(Scorer.averaged().score(null), 0.0);
		assertEquals(Scorer.averaged().score("abc"), 0.0);
		var scorer = Scorer.averaged(LEN, _ -> 0.5);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score("a"), 0.75);
		assertEquals(scorer.score("abc"), 1.75);
	}

	@Test
	public void testMultiply() {
		var scorer = Scorer.multiply(null);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(Lists.wrap()), 0.0);
		assertEquals(scorer.score(Lists.wrap("a", "abc")), 0.0);
		scorer = Scorer.multiply(LEN);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(Lists.wrap()), 0.0);
		assertEquals(scorer.score(Lists.wrap(null, "abc")), 0.0);
		assertEquals(scorer.score(Lists.wrap("a", "abc")), 3.0);
	}

	@Test
	public void testSum() {
		var scorer = Scorer.sum(null);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(Lists.wrap()), 0.0);
		assertEquals(scorer.score(Lists.wrap("a", "abc")), 0.0);
		scorer = Scorer.sum(LEN);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(Lists.wrap()), 0.0);
		assertEquals(scorer.score(Lists.wrap(null, "abc")), 3.0);
		assertEquals(scorer.score(Lists.wrap("a", "abc")), 4.0);
	}

	@Test
	public void testAverage() {
		var scorer = Scorer.average(null);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(Lists.wrap()), 0.0);
		assertEquals(scorer.score(Lists.wrap("a", "abc")), 0.0);
		scorer = Scorer.average(LEN);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(Lists.wrap()), 0.0);
		assertEquals(scorer.score(Lists.wrap(null, "abc")), 1.5);
		assertEquals(scorer.score(Lists.wrap("a", "abc")), 2.0);
	}

	private static <T> Scorer.Result<T> r(T ref, double score) {
		return new Scorer.Result<>(ref, score);
	}

	private static <T> void assertResult(Scorer.Result<T> result, T ref, double score) {
		assertEquals(result.ref(), ref);
		assertEquals(result.score(), score);
	}
}
