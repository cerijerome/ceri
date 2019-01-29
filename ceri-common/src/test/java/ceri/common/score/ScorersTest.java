package ceri.common.score;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import ceri.common.filter.Filter;

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
		assertThat(Scorers.score(null), is(0.0));
		assertThat(Scorers.score(0f), is(0.0));
		assertThat(Scorers.score((byte) 100), is(100.0));
		assertThat(Scorers.score(Long.MAX_VALUE), is((double) Long.MAX_VALUE));
		assertThat(Scorers.nonNull(null).score(1), is(0.0));
	}

	@Test
	public void testConstant() {
		Scorer<String> constant = Scorers.constant(99.999);
		assertThat(constant.score(null), is(99.999));
		assertThat(constant.score(""), is(99.999));
		assertThat(constant.score("A"), is(99.999));
		assertThat(constant.score("CCCCCCCCCC"), is(99.999));
		assertThat(Scorers.constant(1).score(123), is(1.0));
		assertThat(Scorers.constant(0).score(null), is(0.0));
	}

	@Test
	public void testValue() {
		Scorer<Float> scorer = Scorers.value();
		assertThat(scorer.score(null), is(0.0));
		assertThat(scorer.score(0f), is(0.0));
		assertThat(scorer.score(1.0f), is(1.0));
		assertThat(scorer.score(Float.MAX_VALUE), is((double) Float.MAX_VALUE));
	}

	@Test
	public void testAverage() {
		Scorer<String> len = Scorers.nonNull(String::length);
		Scorer<String> firstChar = Scorers.nonNull(s -> s.isEmpty() ? 0.0 : s.charAt(0) - 'A');
		Scorer<String> scorer = Scorers.average(len, firstChar);
		assertThat(scorer.score(null), is(0.0));
		assertThat(scorer.score(""), is(0.0));
		assertThat(scorer.score("AAA"), is(1.5));
		assertThat(scorer.score("B"), is(1.0));
		assertThat(scorer.score("CC"), is(2.0));
	}

	@Test
	public void testMultiply() {
		Scorer<String> len = Scorers.nonNull(String::length);
		Scorer<String> firstChar = Scorers.nonNull(s -> s.isEmpty() ? 0.0 : s.charAt(0) - 'A');
		Scorer<String> scorer = Scorers.multiply(len, firstChar);
		assertThat(scorer.score(null), is(0.0));
		assertThat(scorer.score(""), is(0.0));
		assertThat(scorer.score("AAA"), is(0.0));
		assertThat(scorer.score("B"), is(1.0));
		assertThat(scorer.score("CC"), is(4.0));
	}

	@Test
	public void testFilter() {
		Scorer<String> len = Scorers.nonNull(String::length);
		Filter<String> filter = Scorers.filter(len, 2.0, 3.0);
		assertThat(filter.filter("AAA"), is(true));
		assertThat(filter.filter("B"), is(false));
		assertThat(filter.filter("CCCC"), is(false));
		assertThat(filter.filter("DD"), is(true));
	}

	@Test
	public void testSort() {
		List<Integer> list = new ArrayList<>(Arrays.asList(3, 6, 8, 3, 2));
		Scorers.sort(list, i -> 10 - i);
		assertCollection(list, 8, 6, 3, 3, 2);
	}

}
