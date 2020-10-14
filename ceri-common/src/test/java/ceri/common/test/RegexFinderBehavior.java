package ceri.common.test;

import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.findsRegex;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class RegexFinderBehavior {

	@Test
	public void shouldFindPattern() {
		assertThat("aBc", findsRegex("(?i)b"));
	}

	@Test
	public void shouldFailForNonMatchingRegex() {
		assertAssertion(() -> assertThat("aBc", findsRegex("b")));
	}

}
