package ceri.common.score;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ScoreLookupBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ScoreLookup<String> l0 = ScoreLookup.<String>builder().score(1, "A").score(3, "B").build();
		ScoreLookup<String> l1 = ScoreLookup.<String>builder().score(1, "A").score(3, "B").build();
		ScoreLookup<String> l2 = ScoreLookup.<String>builder().score(1, "A").score(2, "B").build();
		ScoreLookup<String> l3 =
			ScoreLookup.<String>builder().score(1, "A").score(3, "B").normalize().build();
		ScoreLookup<String> l4 = ScoreLookup.<String>builder().score(0, "A").normalize().build();
		exerciseEquals(l0, l1);
		assertNotEquals(l0, l2);
		assertNotEquals(l0, l3);
		assertNotEquals(l0, l4);
		assertNotEquals(l0, null);
	}

	@Test
	public void shouldNormalizeScores() {
		ScoreLookup<String> lookup = ScoreLookup.<String>builder().score(1.0, "A").score(4.0, "B")
			.score(0.0, "C").normalize().build();
		assertThat(lookup.score("A"), is(0.2));
		assertThat(lookup.score("B"), is(0.8));
		assertThat(lookup.score("C"), is(0.0));
		assertThat(lookup.score("D"), is(0.0));
	}

}
