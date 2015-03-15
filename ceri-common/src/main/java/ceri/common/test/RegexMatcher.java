package ceri.common.test;

import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexMatcher<T> extends BaseMatcher<T> {
	private final Pattern pattern;

	public RegexMatcher(String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("matches regex \"" + pattern.pattern() + "\"");
	}

	@Override
	public boolean matches(Object item) {
		return pattern.matcher(item.toString()).matches();
	}

}
