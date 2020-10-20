package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import java.util.regex.Pattern;
import org.junit.Test;

public class NonMatchResultBehavior {

	@Test
	public void shouldNotThrowExceptionForToString() {
		NonMatcher m = NonMatcher.of(Pattern.compile("[a-c]"), "abcDEF");
		assertFalse(m.toResult().toString().isEmpty());
		m.find();
		assertFalse(m.toResult().toString().isEmpty());
	}

	public static void assertNonMatchResult(NonMatchResult r, String group, int start, int end) {
		assertEquals(r.group(), group, "group");
		assertEquals(r.start(), start, "start");
		assertEquals(r.end(), end, "end");
	}
}
