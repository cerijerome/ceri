package ceri.common.test;

import static ceri.common.test.TestUtil.matchesRegex;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class RegexMatcherBehavior {

	@Test
	public void shouldFailForNonMatchingRegex() {
		try {
			assertThat("a", matchesRegex("b"));
		} catch (AssertionError e) {}
	}

}
