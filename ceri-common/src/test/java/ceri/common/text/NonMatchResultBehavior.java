package ceri.common.text;

import static ceri.common.test.TestUtil.assertEquals;
import java.util.regex.Pattern;
import org.junit.Test;

public class NonMatchResultBehavior {

	@Test
	public void shouldNotThrowExceptionForToString() {
		NonMatcher m = NonMatcher.of(Pattern.compile("[a-c]"), "abcDEF");
		assertEquals(m.toResult().toString().isEmpty(), false);
		m.find();
		assertEquals(m.toResult().toString().isEmpty(), false);
	}

	public static void assertNonMatchResult(NonMatchResult r, String group, int start, int end) {
		assertEquals(r.group(), group, "group");
		assertEquals(r.start(), start, "start");
		assertEquals(r.end(), end, "end");
	}
}
