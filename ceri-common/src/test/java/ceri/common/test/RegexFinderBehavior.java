package ceri.common.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertFind;
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
