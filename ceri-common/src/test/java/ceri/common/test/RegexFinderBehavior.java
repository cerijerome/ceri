package ceri.common.test;

import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertFind;
import org.junit.Test;

public class RegexFinderBehavior {

	@Test
	public void shouldFindPattern() {
		assertFind("aBc", "(?i)b");
	}

	@Test
	public void shouldFailForNonMatchingRegex() {
		assertAssertion(() -> assertFind("aBc", "b"));
	}

}
