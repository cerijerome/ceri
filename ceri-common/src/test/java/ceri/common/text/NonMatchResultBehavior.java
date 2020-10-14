package ceri.common.text;

import static ceri.common.test.TestUtil.assertEq;
import java.util.regex.Pattern;
import org.junit.Test;

public class NonMatchResultBehavior {

	@Test
	public void shouldNotThrowExceptionForToString() {
		NonMatcher m = NonMatcher.of(Pattern.compile("[a-c]"), "abcDEF");
		assertEq(m.toResult().toString().isEmpty(), false);
		m.find();
		assertEq(m.toResult().toString().isEmpty(), false);
	}

	public static void assertNonMatchResult(NonMatchResult r, String group, int start, int end) {
		assertEq(r.group(), group, "group");
		assertEq(r.start(), start, "start");
		assertEq(r.end(), end, "end");
	}
}
