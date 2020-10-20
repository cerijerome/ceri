package ceri.common.score;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.junit.Test;

public class ScorersTest {

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(Scorers.class);
	}

	@Test
	public void testResults() {
		Scorer<String> len = Scorers.nonNull(String::length);
		assertIterable(Scorers.results(len, "abc", "de", "f", ""), len.result("abc"),
			len.result("de"), len.result("f"), len.result(""));
	}

	@Test
	public void testScore() {
		assertEquals(Scorers.score(null), 0.0);
		assertEquals(Scorers.score(0f), 0.0);
		assertEquals(Scorers.score((byte) 100), 100.0);
		assertEquals(Scorers.score(Long.MAX_VALUE), (double) Long.MAX_VALUE);
		assertEquals(Scorers.nonNull(null).score(1), 0.0);
	}

	@Test
	public void testConstant() {
		Scorer<String> constant = Scorers.constant(99.999);
		assertEquals(constant.score(null), 99.999);
		assertEquals(constant.score(""), 99.999);
		assertEquals(constant.score("A"), 99.999);
		assertEquals(constant.score("CCCCCCCCCC"), 99.999);
		assertEquals(Scorers.constant(1).score(123), 1.0);
		assertEquals(Scorers.constant(0).score(null), 0.0);
	}

	@Test
	public void testValue() {
		Scorer<Float> scorer = Scorers.value();
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(0f), 0.0);
		assertEquals(scorer.score(1.0f), 1.0);
		assertEquals(scorer.score(Float.MAX_VALUE), (double) Float.MAX_VALUE);
	}

	@Test
	public void testAverage() {
		Scorer<String> len = Scorers.nonNull(String::length);
		Scorer<String> firstChar = Scorers.nonNull(s -> s.isEmpty() ? 0.0 : s.charAt(0) - 'A');
		Scorer<String> scorer = Scorers.average(len, firstChar);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(""), 0.0);
		assertEquals(scorer.score("AAA"), 1.5);
		assertEquals(scorer.score("B"), 1.0);
		assertEquals(scorer.score("CC"), 2.0);
	}

	@Test
	public void testMultiply() {
		Scorer<String> len = Scorers.nonNull(String::length);
		Scorer<String> firstChar = Scorers.nonNull(s -> s.isEmpty() ? 0.0 : s.charAt(0) - 'A');
		Scorer<String> scorer = Scorers.multiply(len, firstChar);
		assertEquals(scorer.score(null), 0.0);
		assertEquals(scorer.score(""), 0.0);
		assertEquals(scorer.score("AAA"), 0.0);
		assertEquals(scorer.score("B"), 1.0);
		assertEquals(scorer.score("CC"), 4.0);
	}

	@Test
	public void testFilter() {
		Scorer<String> len = Scorers.nonNull(String::length);
		Predicate<String> filter = Scorers.filter(len, 2.0, 3.0);
		assertTrue(filter.test("AAA"));
		assertFalse(filter.test("B"));
		assertFalse(filter.test("CCCC"));
		assertTrue(filter.test("DD"));
	}

	@Test
	public void testSort() {
		List<Integer> list = new ArrayList<>(Arrays.asList(3, 6, 8, 3, 2));
		Scorers.sort(list, i -> 10 - i);
		assertCollection(list, 8, 6, 3, 3, 2);
	}

}
