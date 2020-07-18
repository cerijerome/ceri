package ceri.common.test;

import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexFinder<T> extends BaseMatcher<T> {
	private final Pattern pattern;

	public RegexFinder(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("find regex \"" + pattern.pattern() + "\"");
	}

	@Override
	public boolean matches(Object item) {
		return pattern.matcher(item.toString()).find();
	}

}
