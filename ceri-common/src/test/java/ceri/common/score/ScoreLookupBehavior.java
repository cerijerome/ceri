package ceri.common.score;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
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
		assertEquals(lookup.score("A"), 0.2);
		assertEquals(lookup.score("B"), 0.8);
		assertEquals(lookup.score("C"), 0.0);
		assertEquals(lookup.score("D"), 0.0);
	}

}
