package ceri.common.function;

import org.junit.Test;
import ceri.common.collect.Lists;
import ceri.common.test.Assert;

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
		Assert.equal(a.compareTo(b), 0);
		Assert.equal(b.compareTo(c), -1);
		Assert.equal(c.compareTo(a), 1);
	}

	@Test
	public void testResultStringRepresentation() {
		Assert.string(r(null, 0), "null=0.0");
		Assert.string(r("test", 123), "test=123.0");
	}

	@Test
	public void testLookup() {
		var lookup = Scorer.Lookup.builder().build(false);
		Assert.equal(lookup.score(null), 0.0);
		Assert.equal(lookup.score("abc"), 0.0);
		lookup = Scorer.Lookup.builder().build(true);
		Assert.equal(lookup.score(null), 0.0);
		Assert.equal(lookup.score("abc"), 0.0);
		lookup = Scorer.Lookup.builder().put(4, "abc", "ab").put(2, "a").build(false);
		Assert.equal(lookup.score(null), 0.0);
		Assert.equal(lookup.score("abc"), 4.0);
		Assert.equal(lookup.score("a"), 2.0);
		Assert.equal(lookup.score("b"), 0.0);
		lookup = Scorer.Lookup.builder().put(4, "abc", "ab").put(2, "a").build(true);
		Assert.equal(lookup.score(null), 0.0);
		Assert.equal(lookup.score("abc"), 0.4);
		Assert.equal(lookup.score("a"), 0.2);
		Assert.equal(lookup.score("b"), 0.0);
	}

	@Test
	public void testLookupScore() {
		var lookup = Scorer.Lookup.builder().put(4, "abc", "ab").put(2, "a").build(true);
		Assert.equal(lookup.score(LEN, null), 0.0);
		Assert.equal(lookup.score(LEN, "abc"), 0.4);
		Assert.equal(lookup.score(LEN, "a"), 0.2);
		Assert.equal(lookup.score(LEN, "b"), 1.0);
	}

	@Test
	public void testValue() {
		Assert.equal(Scorer.value(null), 0.0);
		Assert.equal(Scorer.value(1), 1.0);
		Assert.equal(Scorer.value(3L), 3.0);
		Assert.equal(Scorer.value(-1f), -1.0);
		Assert.equal(Scorer.value(Double.NaN), Double.NaN);
	}

	@Test
	public void testScore() {
		Assert.equal(Scorer.score(null, "a"), 0.0);
		Assert.equal(Scorer.score(LEN, null), 0.0);
		Assert.equal(Scorer.score(LEN, ""), 0.0);
		Assert.equal(Scorer.score(LEN, "abc"), 3.0);
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
		Assert.equal(Scorer.constant(0.1).score(null), 0.1);
		Assert.equal(Scorer.constant(0.1).score(1), 0.1);
	}

	@Test
	public void testScoring() {
		Assert.equal(Scorer.as(null, LEN).score(1), 0.0);
		Assert.equal(Scorer.as(String::valueOf, null).score(1), 0.0);
		Assert.equal(Scorer.as(String::valueOf, LEN).score(null), 0.0);
		Assert.equal(Scorer.as(String::valueOf, LEN).score(1), 1.0);
		Assert.equal(Scorer.as(String::valueOf, LEN).score(1.01), 4.0);
	}

	@Test
	public void testResults() {
		Assert.ordered(Scorer.results(null, "", null, "abc", "a"), r("", 0), r(null, 0), r("abc", 0),
			r("a", 0));
		Assert.ordered(Scorer.results(LEN, (String[]) null));
		Assert.ordered(Scorer.results(LEN));
		Assert.ordered(Scorer.results(LEN, "", null, "abc", "a"), r("abc", 3), r("a", 1), r("", 0),
			r(null, 0));
	}

	@Test
	public void testSort() {
		Assert.equal(Scorer.sort(null, LEN), null);
		Assert.ordered(Scorer.sort(Lists.ofAll("", null, "abc", "a"), null), "", null, "abc", "a");
		Assert.ordered(Scorer.sort(Lists.ofAll("", null, "abc", "a"), LEN), "abc", "a", "", null);
	}

	@Test
	public void testFilter() throws Exception {
		Assert.equal(Scorer.filter(null, d -> d > 1).test("abc"), false);
		Assert.equal(Scorer.filter(LEN, null).test("abc"), false);
		Assert.equal(Scorer.filter(LEN, d -> d > 1).test(null), false);
		Assert.equal(Scorer.filter(LEN, d -> d > 1).test(""), false);
		Assert.equal(Scorer.filter(LEN, d -> d > 1).test("a"), false);
		Assert.equal(Scorer.filter(LEN, d -> d > 1).test("abc"), true);
	}

	@Test
	public void testFilterWithinLimits() {
		Assert.equal(Scorer.filter(null, 1.0, 3.0).test("ab"), false);
		Assert.equal(Scorer.filter(LEN, null, null).test("abc"), true);
		Assert.equal(Scorer.filter(LEN, 2.0, null).test("a"), false);
		Assert.equal(Scorer.filter(LEN, 2.0, null).test("ab"), true);
		Assert.equal(Scorer.filter(LEN, 2.0, null).test("abc"), true);
		Assert.equal(Scorer.filter(LEN, null, 2.0).test("a"), true);
		Assert.equal(Scorer.filter(LEN, null, 2.0).test("ab"), true);
		Assert.equal(Scorer.filter(LEN, null, 2.0).test("abc"), false);
		Assert.equal(Scorer.filter(LEN, 1.0, 2.0).test(""), false);
		Assert.equal(Scorer.filter(LEN, 1.0, 2.0).test("a"), true);
		Assert.equal(Scorer.filter(LEN, 1.0, 2.0).test("ab"), true);
		Assert.equal(Scorer.filter(LEN, 1.0, 2.0).test("abc"), false);
	}

	@Test
	public void testMultiplied() {
		Assert.equal(Scorer.multiplied().score(null), 0.0);
		Assert.equal(Scorer.multiplied().score("abc"), 0.0);
		var scorer = Scorer.multiplied(LEN, LEN, _ -> 0.5);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score("a"), 0.5);
		Assert.equal(scorer.score("abc"), 4.5);
	}

	@Test
	public void testAveraged() {
		Assert.equal(Scorer.averaged().score(null), 0.0);
		Assert.equal(Scorer.averaged().score("abc"), 0.0);
		var scorer = Scorer.averaged(LEN, _ -> 0.5);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score("a"), 0.75);
		Assert.equal(scorer.score("abc"), 1.75);
	}

	@Test
	public void testMultiply() {
		var scorer = Scorer.multiply(null);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score(Lists.wrap()), 0.0);
		Assert.equal(scorer.score(Lists.wrap("a", "abc")), 0.0);
		scorer = Scorer.multiply(LEN);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score(Lists.wrap()), 0.0);
		Assert.equal(scorer.score(Lists.wrap(null, "abc")), 0.0);
		Assert.equal(scorer.score(Lists.wrap("a", "abc")), 3.0);
	}

	@Test
	public void testSum() {
		var scorer = Scorer.sum(null);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score(Lists.wrap()), 0.0);
		Assert.equal(scorer.score(Lists.wrap("a", "abc")), 0.0);
		scorer = Scorer.sum(LEN);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score(Lists.wrap()), 0.0);
		Assert.equal(scorer.score(Lists.wrap(null, "abc")), 3.0);
		Assert.equal(scorer.score(Lists.wrap("a", "abc")), 4.0);
	}

	@Test
	public void testAverage() {
		var scorer = Scorer.average(null);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score(Lists.wrap()), 0.0);
		Assert.equal(scorer.score(Lists.wrap("a", "abc")), 0.0);
		scorer = Scorer.average(LEN);
		Assert.equal(scorer.score(null), 0.0);
		Assert.equal(scorer.score(Lists.wrap()), 0.0);
		Assert.equal(scorer.score(Lists.wrap(null, "abc")), 1.5);
		Assert.equal(scorer.score(Lists.wrap("a", "abc")), 2.0);
	}

	private static <T> Scorer.Result<T> r(T ref, double score) {
		return new Scorer.Result<>(ref, score);
	}

	private static <T> void assertResult(Scorer.Result<T> result, T ref, double score) {
		Assert.equal(result.ref(), ref);
		Assert.equal(result.score(), score);
	}
}
