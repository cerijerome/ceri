package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class MultiPatternBehavior {

	@Test
	public void shouldMatchFirstCorrectStringPattern() {
		MultiPattern mp = MultiPattern.builder().pattern("cde", "bcd", "abc").build();
		Matcher m = mp.find("abcd");
		assertEquals(m.pattern().pattern(), "bcd");
	}

	@Test
	public void shouldReturnNullForNonMatchingFind() {
		MultiPattern mp = MultiPattern.builder().pattern("cde", "bcd", "abc").build();
		Matcher m = mp.find("z");
		assertEquals(m, (Matcher) null);
	}

	@Test
	public void shouldAcceptPatterns() {
		MultiPattern mp = MultiPattern.builder().pattern(Pattern.compile("cde")).build();
		Matcher m = mp.find("abcdefgh");
		assertEquals(m.pattern().pattern(), "cde");
	}

}
