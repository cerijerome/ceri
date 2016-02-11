package ceri.common.score;

import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ScoreLookupBehavior {

	@Test
	public void shouldNotBreakEqualsContract() {
		ScoreLookup<String> l0 = ScoreLookup.<String>builder().score("A", 1).score("B", 3).build();
		ScoreLookup<String> l1 = ScoreLookup.<String>builder().score("A", 1).score("B", 3).build();
		ScoreLookup<String> l2 = ScoreLookup.<String>builder().score("A", 1).score("B", 2).build();
		ScoreLookup<String> l3 = ScoreLookup.<String>builder().score("A", 1).score("B", 3).normalize().build();
		ScoreLookup<String> l4 = ScoreLookup.<String>builder().score("A", 0).normalize().build();
		exerciseEquals(l0, l1);
		assertNotEquals(l0, l2);
		assertNotEquals(l0, l3);
		assertNotEquals(l0, l4);
		assertNotEquals(l0, null);
	}
	
	@Test
	public void shouldNormalizeScores() {
		ScoreLookup<String> lookup =
			ScoreLookup.<String>builder().score("A", 1.0).score("B", 4.0).score("C", 0.0)
				.normalize().build();
		assertThat(lookup.score("A"), is(0.2));
		assertThat(lookup.score("B"), is(0.8));
		assertThat(lookup.score("C"), is(0.0));
		assertThat(lookup.score("D"), is(0.0));
	}

}
