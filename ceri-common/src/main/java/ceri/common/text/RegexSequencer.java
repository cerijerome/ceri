package ceri.common.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies subsequent pattern matchers to a string. Can be replaced by Matcher.usePattern?
 */
public class RegexSequencer {
	private CharSequence s;
	private Matcher lastMatcher = null;

	public static RegexSequencer of(CharSequence s) {
		return new RegexSequencer(s);
	}

	private RegexSequencer(CharSequence s) {
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
			s = s.subSequence(lastMatcher.end(), s.length());
		} catch (IllegalStateException e) {
			// ignore
		}
	}

}
