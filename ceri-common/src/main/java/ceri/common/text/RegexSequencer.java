package ceri.common.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies subsequent pattern matchers to a string. Can be replaced by Matcher.usePattern?
 */
public class RegexSequencer {
	private String s;
	private Matcher lastMatcher = null;

	public static RegexSequencer of(String s) {
		return new RegexSequencer(s);
	}

	private RegexSequencer(String s) {
		this.s = s;
	}

	public Matcher matcher(String pattern) {
		return matcher(Pattern.compile(pattern));
	}

	public Matcher matcher(Pattern pattern) {
		updateString(lastMatcher);
		lastMatcher = pattern.matcher(s);
		return lastMatcher;
	}

	private void updateString(Matcher lastMatcher) {
		if (lastMatcher == null) return;
		try {
			s = s.substring(lastMatcher.end());
		} catch (IllegalStateException e) {
			// ignore
		}
	}

}
