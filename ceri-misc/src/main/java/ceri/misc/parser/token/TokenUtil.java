package ceri.misc.parser.token;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenUtil {
	private TokenUtil() {}

	/**
	 * Express tokens as their input-string identifiers.
	 */
	public static String asString(List<Token> tokens) {
		StringBuilder b = new StringBuilder();
		for (Token token : tokens)
			b.append(token.asString());
		return b.toString();
	}

	/**
	 * Tries to match a pattern at given position in a string. If no match it returns null,
	 * otherwise increment the index and return the matcher.
	 */
	public static Matcher matcher(Pattern pattern, String str, Index i) {
		Matcher m = pattern.matcher(str.substring(i.value()));
		if (!m.find()) return null;
		i.set(i.value() + m.end());
		return m;
	}

	/**
	 * Tries to match a pattern at given position in a string. If no match it returns false,
	 * otherwise increment the index and return true.
	 */
	public static boolean matches(Pattern pattern, String str, Index i) {
		return matcher(pattern, str, i) != null;
	}

}
