package ceri.common.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class RegexMatcherBehavior {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(RegexMatcher.class);
	}

	@Test
	public void shouldFindPattern() {
		assertFind("aBc", "(?i)b");
	}

	@Test
	public void shouldMatchPattern() {
		assertMatch("aBc", "(?i)abc");
	}

	@Test
	public void shouldFailFindForNonMatchingRegex() {
		assertAssertion(() -> assertFind("aBc", "b"));
	}

	@Test
	public void shouldFailMatchForNonMatchingRegex() {
		assertAssertion(() -> assertMatch("a", "b"));
	}

}
