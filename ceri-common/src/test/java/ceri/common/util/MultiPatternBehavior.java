package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.regex.Matcher;
import org.junit.Test;

public class MultiPatternBehavior {

	@Test
	public void shouldMatchFirstCorrectStringPattern() {
		MultiPattern mp = MultiPattern.builder().pattern("cde", "bcd", "abc").build();
		Matcher m = mp.find("abcd");
		assertThat(m.pattern().pattern(), is("bcd"));
	}

	@Test
	public void shouldReturnNullForNonMatchingFind() {
		MultiPattern mp = MultiPattern.builder().pattern("cde", "bcd", "abc").build();
		Matcher m = mp.find("z");
		assertThat(m, is((Matcher)null));
	}

}