package ceri.common.score;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ScoreLookupBehavior {

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
