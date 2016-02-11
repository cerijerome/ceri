package ceri.common.score;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.collection.MapBuilder;
import ceri.common.math.MathUtil;

public class CollectionScorersTest {

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(CollectionScorers.class);
	}

	@Test
	public void testSum() {
		List<String> list = Arrays.asList("a", "bbbb", "cc", "dd");
		Scorer<Collection<String>> scorer = CollectionScorers.sum(s -> s.length());
		assertThat(scorer.score(list), is(9.0));
		scorer = CollectionScorers.sum(null);
		assertThat(scorer.score(list), is(0.0));
	}

	@Test
	public void testAverage() {
		List<String> list = Arrays.asList("a", "bbbb", "cc", "dd");
		Scorer<Collection<String>> scorer = CollectionScorers.average(s -> s.length());
		assertThat(scorer.score(list), is(2.25));
		scorer = CollectionScorers.average(null);
		assertThat(scorer.score(list), is(0.0));
	}

	@Test
	public void testMultiply() {
		List<String> list = Arrays.asList("a", "bbbb", "cc", "dd");
		Scorer<Collection<String>> scorer = CollectionScorers.multiply(s -> s.length());
		assertThat(scorer.score(list), is(16.0));
		scorer = CollectionScorers.multiply(null);
		assertThat(scorer.score(list), is(0.0));
	}

	@Test
	public void testMapMultiplySum() {
		Map<Integer, Float> map = MapBuilder.of(2, 0.1f, 3, 1.1f).build();
		Scorer<Map<Integer, Float>> scorer = CollectionScorers.mapMultiplySum(Scorers.value());
		assertThat(MathUtil.simpleRound(scorer.score(map), 6), is(3.5));
		scorer = CollectionScorers.mapMultiplySum(i -> i / 2.0, f -> f + 1.0);
		assertThat(MathUtil.simpleRound(scorer.score(map), 6), is(4.25));
		scorer = CollectionScorers.mapMultiplySum(null);
		assertThat(scorer.score(map), is(0.0));
		scorer = CollectionScorers.mapMultiplySum(Scorers.value(), null);
		assertThat(scorer.score(map), is(0.0));
	}

}
