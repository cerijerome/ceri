package ceri.common.score;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.collection.Immutable;
import ceri.common.math.Maths;

public class CollectionScorersTest {

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(CollectionScorers.class);
	}

	@Test
	public void testSum() {
		List<String> list = Arrays.asList("a", "bbbb", "cc", "dd");
		Scorer<Collection<String>> scorer = CollectionScorers.sum(String::length);
		assertEquals(scorer.score(list), 9.0);
		scorer = CollectionScorers.sum(null);
		assertEquals(scorer.score(list), 0.0);
	}

	@Test
	public void testAverage() {
		List<String> list = Arrays.asList("a", "bbbb", "cc", "dd");
		Scorer<Collection<String>> scorer = CollectionScorers.average(String::length);
		assertEquals(scorer.score(list), 2.25);
		scorer = CollectionScorers.average(null);
		assertEquals(scorer.score(list), 0.0);
	}

	@Test
	public void testMultiply() {
		List<String> list = Arrays.asList("a", "bbbb", "cc", "dd");
		Scorer<Collection<String>> scorer = CollectionScorers.multiply(String::length);
		assertEquals(scorer.score(list), 16.0);
		scorer = CollectionScorers.multiply(null);
		assertEquals(scorer.score(list), 0.0);
	}

	@Test
	public void testMapMultiplySum() {
		var map = Immutable.mapOf(2, 0.1f, 3, 1.1f);
		Scorer<Map<Integer, Float>> scorer = CollectionScorers.mapMultiplySum(Scorers.value());
		assertEquals(Maths.simpleRound(6, scorer.score(map)), 3.5);
		scorer = CollectionScorers.mapMultiplySum(i -> i / 2.0, f -> f + 1.0);
		assertEquals(Maths.simpleRound(6, scorer.score(map)), 4.25);
		scorer = CollectionScorers.mapMultiplySum(null);
		assertEquals(scorer.score(map), 0.0);
		scorer = CollectionScorers.mapMultiplySum(Scorers.value(), null);
		assertEquals(scorer.score(map), 0.0);
	}

}
