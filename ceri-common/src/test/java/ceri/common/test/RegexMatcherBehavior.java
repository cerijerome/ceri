package ceri.common.test;

import static ceri.common.test.TestUtil.*;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class RegexMatcherBehavior {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(RegexMatcher.class);
	}

	@Test
	public void shouldFindPattern() {
		assertThat("aBc", findsRegex("(?i)b"));
	}

	@Test
	public void shouldMatchPattern() {
		assertThat("aBc", findsRegex("(?i)abc"));
	}

	@Test
	public void shouldFailFindForNonMatchingRegex() {
		assertAssertion(() -> assertThat("aBc", findsRegex("b")));
	}
	
	@Test
	public void shouldFailMatchForNonMatchingRegex() {
		assertAssertion(() -> assertThat("a", matchesRegex("b")));
	}

}
