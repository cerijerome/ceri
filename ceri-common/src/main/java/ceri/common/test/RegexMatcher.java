package ceri.common.test;

import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexMatcher {

	private RegexMatcher() {}

	public static <T> Find<T> find(Pattern pattern) {
		return new Find<>(pattern);
	}

	public static <T> Match<T> match(Pattern pattern) {
		return new Match<>(pattern);
	}

	public static class Match<T> extends BaseMatcher<T> {
		private final Pattern pattern;

		private Match(Pattern pattern) {
			this.pattern = pattern;
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

	public static class Find<T> extends BaseMatcher<T> {
		private final Pattern pattern;

		private Find(Pattern pattern) {
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
}
