package ceri.common.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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
		assertThat("group", r.group(), is(group));
		assertThat("start", r.start(), is(start));
		assertThat("end", r.end(), is(end));
	}
}
