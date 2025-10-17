package ceri.common.text;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Immutable;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.util.Validate;

/**
 * Support for regex patterns.
 */
public class Regex {
	private static final String PATTERN = "Pattern";
	private static final String MATCHER = "Matcher";
	public static final Joiner OR_CAPTURE = Joiner.of("(", "|", ")");
	public static final Joiner OR = Joiner.of("(?:", "|", ")");
	/** A pattern that matches nothing. */
	public static final Pattern NONE = Pattern.compile("(?!)");
	/** An empty pattern that matches anything. */
	public static final Pattern EMPTY = Pattern.compile("");
	/** A pattern that matches everything, including line terminators. */
	public static final Pattern ALL = Pattern.compile("(?s).*");
	/** A pattern that matches line terminators. */
	public static final Pattern EOL = Pattern.compile("(?:\\r\\n|\\n|\\r)");
	/** A pattern that matches everything up to a line terminator. */
	public static final Pattern LINE = Pattern.compile(".*");
	/** A pattern that matches any whitespace; ' \t\n\x0B\f\r' if unicode not set. */
	public static final Pattern SPACE = Pattern.compile("\\s+");
	/** A pattern that matches any unicode whitespace. */
	public static final Pattern UNICODE_SPACE = Pattern.compile("(?U)\\s+");
	/** A pattern that matches a comma and any surrounding whitespace. */
	public static final Pattern COMMA = Pattern.compile("\\s*,\\s*");

	private Regex() {}

	/**
	 * Common patterns.
	 */
	public static class Common {
		/** Unsigned binary integer with 0b prefix (needs custom parser). */
		public static final String BIN_UINT = "0[bB][01]+";
		/** Signed binary integer with 0b prefix (needs custom parser). */
		public static final String BIN_INT = "[+-]?0[bB][01]+";
		/** Unsigned octal integer with 0 prefix. */
		public static final String OCT_UINT = "0[0-7]+";
		/** Signed Octal integer with 0 prefix. */
		public static final String OCT_INT = "[+-]?0[0-7]+";
		/** Unsigned decimal integer. */
		public static final String DEC_UINT = "(?:0|[1-9]\\d*)";
		/** Signed decimal integer. */
		public static final String DEC_INT = "[+-]?" + DEC_UINT;
		/** Single hexadecimal digit. */
		public static final String HEX_DIGIT = "[a-fA-F0-9]";
		/** Unsigned hexadecimal integer with prefix. */
		public static final String HEX_UINT = "(?:0x|0X|#)[a-fA-F0-9]+";
		/** Signed hexadecimal integer with prefix. */
		public static final String HEX_INT = "[+-]?" + HEX_UINT;
		/** Unsigned binary, octal, decimal, or hexadecimal integer (use decodes below). */
		public static final String UINT_NUM = OR.joinAll(DEC_UINT, HEX_UINT, OCT_UINT, BIN_UINT);
		/** Signed binary, octal, decimal, or hexadecimal integer (use decodes below). */
		public static final String INT_NUM = "[+-]?" + UINT_NUM;
		/** Unsigned decimal number; integer or floating point. */
		public static final String UDEC_NUM = "(?:0|[1-9]\\d*|\\d*\\.\\d+)";
		/** Signed decimal number; integer or floating point. */
		public static final String DEC_NUM = "[+-]?" + UDEC_NUM;
		/** Single ASCII letter. */
		public static final String ALPHABET = "[a-zA-Z]";
		/** Single ASCII letter or number. */
		public static final String ALPHANUM = "[a-zA-Z0-9]";
		/** Java identifier name. */
		public static final String JAVA_NAME = "[\\p{L}$_][\\p{L}0-9$_]*";

		private Common() {}

		/**
		 * Use to decode UINT_NUM and INT_NUM.
		 */
		public static int decodeInt(String s) {
			return isBinaryPrefix(s) ? Integer.parseInt(s.substring(2), 2) : Integer.decode(s);
		}

		/**
		 * Use to decode UINT_NUM and INT_NUM.
		 */
		public static long decodeLong(String s) {
			return isBinaryPrefix(s) ? Long.parseLong(s.substring(2), 2) : Long.decode(s);
		}

		private static boolean isBinaryPrefix(String s) {
			return Strings.length(s) > 2 && s.charAt(0) == '0'
				&& (s.charAt(1) == 'b' || s.charAt(1) == 'B');
		}
	}

	/**
	 * Regex filters.
	 */
	public static class Filter {
		public static final Functions.Predicate<Matcher> FIND = m -> m != null && m.find();
		public static final Functions.Predicate<Matcher> NON_FIND = m -> m != null && !m.find();
		public static final Functions.Predicate<Matcher> MATCH = m -> m != null && m.matches();
		public static final Functions.Predicate<Matcher> NON_MATCH = m -> m != null && !m.matches();

		private Filter() {}

		/**
		 * Returns true if the pattern is found.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> find(String format,
			Object... objs) {
			return find(compile(format, objs));
		}

		/**
		 * Returns true if the pattern is found.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> find(Pattern pattern) {
			return matching(pattern, Matcher::find);
		}

		/**
		 * Returns true if the pattern matches.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> match(String format,
			Object... objs) {
			return match(compile(format, objs));
		}

		/**
		 * Returns true if the pattern matches.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> match(Pattern pattern) {
			return matching(pattern, Matcher::matches);
		}

		/**
		 * Applies the predicate to the matcher (before find or match is called).
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> matching(Pattern pattern,
			Excepts.Predicate<? extends E, ? super Matcher> predicate) {
			if (pattern == null || predicate == null) return Filters.no();
			return t -> t != null && predicate.test(pattern.matcher(t));
		}
	}

	/**
	 * Mapping functions for streams.
	 */
	public static class Mapper {
		private Mapper() {}

		/**
		 * Provides the indexed group from the matcher, or empty string if invalid.
		 */
		public static <E extends Exception> Excepts.Function<E, Matcher, String> group(int index) {
			return m -> Regex.group(m, index);
		}
	}

	/**
	 * Splits char sequences by pattern and optional modifier.
	 */
	public static class Split {
		public static final Split LINE = of(null, Regex.EOL);
		public static final Split COMMA = of(Strings::trim, Regex.COMMA);
		public static final Split SPACE = of(Strings::trim, Regex.SPACE);
		public final Pattern pattern;
		public final Functions.Function<? super String, String> modifier;

		/**
		 * Splits the string into an array.
		 */
		public static String[] array(Pattern p, CharSequence s) {
			return array(p, s, 0, null);
		}

		/**
		 * Splits the string into an array, with item modifier.
		 */
		public static <E extends Exception> String[] array(Pattern p, CharSequence s, int limit,
			Excepts.Function<? extends E, ? super String, String> modifier) throws E {
			if (p == null || Strings.isEmpty(s)) return ArrayUtil.Empty.strings;
			var split = p.split(s, limit);
			if (modifier != null) for (int i = 0; i < split.length; i++)
				split[i] = modifier.apply(split[i]);
			return split;
		}

		/**
		 * Splits the string into an immutable list.
		 */
		public static List<String> list(Pattern p, CharSequence s) {
			return list(p, s, 0, null);
		}

		/**
		 * Splits the string into an immutable list, with item modifier.
		 */
		public static <E extends Exception> List<String> list(Pattern p, CharSequence s, int limit,
			Excepts.Function<? extends E, ? super String, String> modifier) throws E {
			return Immutable.wrapListOf(array(p, s, limit, modifier));
		}

		/**
		 * Splits the string as a stream.
		 */
		public static Stream<RuntimeException, String> stream(Pattern p, CharSequence s) {
			return stream(p, s, null);
		}

		/**
		 * Splits the string as a stream, with item modifier.
		 */
		public static <E extends Exception> Stream<E, String> stream(Pattern p, CharSequence s,
			Excepts.Function<? extends E, ? super String, ? extends String> modifier) {
			if (p == null || Strings.isEmpty(s)) return Stream.empty();
			var stream = Stream.<E, String>from(p.splitAsStream(s));
			if (modifier != null) stream = stream.map(modifier);
			return stream;
		}

		/**
		 * Creates by compiling the pattern.
		 */
		public static Split of(String format, Object... args) {
			return of(null, format, args);
		}

		/**
		 * Creates with modifier by compiling the pattern.
		 */
		public static Split of(Functions.Function<? super String, String> modifier, String format,
			Object... args) {
			return of(modifier, compile(format, args));
		}

		/**
		 * Creates from modifier and pattern.
		 */
		public static Split of(Functions.Function<? super String, String> modifier,
			Pattern pattern) {
			return new Split(pattern, modifier);
		}

		private Split(Pattern pattern, Functions.Function<? super String, String> modifier) {
			this.pattern = pattern;
			this.modifier = modifier;
		}

		/**
		 * Splits the char sequence into an array.
		 */
		public String[] array(CharSequence s) {
			return array(s, 0);
		}

		/**
		 * Splits the char sequence into a limited-size array.
		 */
		public String[] array(CharSequence s, int limit) {
			return array(pattern, s, limit, modifier);
		}

		/**
		 * Splits the char sequence into an immutable list.
		 */
		public List<String> list(CharSequence s) {
			return list(s, 0);
		}

		/**
		 * Splits the char sequence into an immutable limited-size list.
		 */
		public List<String> list(CharSequence s, int limit) {
			return list(pattern, s, limit, modifier);
		}

		/**
		 * Splits the char sequence into a stream.
		 */
		public Stream<RuntimeException, String> stream(CharSequence s) {
			return stream(pattern, s, modifier);
		}

		/**
		 * Provides a matcher for the pattern, ignoring modifier.
		 */
		public Matcher matcher(CharSequence s) {
			return pattern.matcher(s);
		}
	}

	/**
	 * Allows chaining of patterns without losing position.
	 */
	public static class Chain {
		private final CharSequence s;
		private Matcher matcher = null;

		public static Chain of(CharSequence s) {
			return new Chain(s);
		}

		private Chain(CharSequence s) {
			this.s = s;
		}

		public Matcher matcher(String pattern) {
			return matcher(Pattern.compile(pattern));
		}

		public Matcher matcher(Pattern pattern) {
			if (matcher == null) matcher = pattern.matcher(s);
			else matcher.usePattern(pattern);
			return matcher;
		}
	}

	/**
	 * Generates a hash code; Pattern does not override hashCode().
	 */
	public static int hash(Pattern pattern) {
		if (pattern == null) return Objects.hash();
		return Objects.hash(pattern.pattern(), pattern.flags());
	}

	/**
	 * Returns true if patterns are equal by regex and flags; Pattern does not override equals().
	 */
	public static boolean equals(Pattern lhs, Pattern rhs) {
		if (lhs == rhs) return true;
		return lhs != null && rhs != null && Objects.equals(lhs.pattern(), rhs.pattern())
			&& lhs.flags() == rhs.flags();
	}

	/**
	 * Returns true if the matcher has a match.
	 */
	public static boolean hasMatch(Matcher m) {
		return m != null && m.hasMatch();
	}

	/**
	 * Fails if the matcher has no current match.
	 */
	public static Matcher validMatcher(Matcher m) {
		return validMatcher(m, "");
	}

	/**
	 * Fails if the matcher has no current match.
	 */
	public static Matcher validMatcher(Matcher m, String format, Object... args) {
		if (hasMatch(m)) return m;
		throw Validate.failed("%s has no match", f(MATCHER, format, args));
	}

	/**
	 * Compiles a pattern from string format.
	 */
	public static Pattern compile(String format, Object... objs) {
		return Pattern.compile(Strings.format(format, objs));
	}

	/**
	 * Compiles a pattern from joined strings.
	 */
	public static Pattern compile(Joiner joiner, Object... objs) {
		if (joiner == null || ArrayUtil.isEmpty(objs)) return EMPTY;
		return Pattern.compile(joiner.joinAll(objs));
	}

	/**
	 * Creates a pattern to search for quoted text, ignoring case.
	 */
	public static Pattern ignoreCase(CharSequence text) {
		return compile("(?i)\\Q" + Chars.safe(text) + "\\E");
	}

	/**
	 * Returns an empty pattern if null.
	 */
	public static Pattern safe(Pattern pattern) {
		return pattern == null ? EMPTY : pattern;
	}

	/**
	 * Returns a matcher for the pattern and char sequence, handling null values.
	 */
	public static Matcher matcher(Pattern pattern, CharSequence s) {
		return pattern == null ? null : pattern.matcher(Chars.safe(s));
	}

	/**
	 * Returns the matcher after an attempted matches.
	 */
	public static Matcher match(Pattern pattern, CharSequence s) {
		return match(matcher(pattern, s));
	}

	/**
	 * Returns the matcher after an attempted matches.
	 */
	public static Matcher match(Matcher m) {
		if (m != null) m.matches();
		return m;
	}

	/**
	 * Fails if the pattern has no match, or returns the matcher.
	 */
	public static Matcher validMatch(Pattern pattern, CharSequence s) {
		return validMatch(pattern, s, "");
	}

	/**
	 * Fails if the pattern has no match, or returns the matcher.
	 */
	public static Matcher validMatch(Pattern pattern, CharSequence s, String format,
		Object... args) {
		var m = match(pattern, s);
		if (hasMatch(m)) return m;
		throw Validate.failed("%s did not match: %s", f(PATTERN, format, args), s);
	}

	/**
	 * Returns the matches group, or empty string if not found.
	 */
	public static String matchGroup(Pattern pattern, CharSequence s, int group) {
		return matchGroup(matcher(pattern, s), group);
	}

	/**
	 * Returns the matches group, or empty string if not found.
	 */
	public static String matchGroup(Matcher m, int group) {
		return group(match(m), group);
	}

	/**
	 * Returns the matcher after an attempted find.
	 */
	public static Matcher find(Pattern pattern, CharSequence s) {
		return find(matcher(pattern, s));
	}

	/**
	 * Returns the matcher after an attempted find.
	 */
	public static Matcher find(Matcher m) {
		if (m != null) m.find();
		return m;
	}

	/**
	 * Fails if the pattern is not found, or returns the matcher.
	 */
	public static Matcher validFind(Pattern pattern, CharSequence s) {
		return validFind(pattern, s, "");
	}

	/**
	 * Fails if the pattern is not found, or returns the matcher.
	 */
	public static Matcher validFind(Pattern pattern, CharSequence s, String format,
		Object... args) {
		var m = find(pattern, s);
		if (hasMatch(m)) return m;
		throw Validate.failed("%s not found: %s", f(PATTERN, format, args), s);
	}

	/**
	 * Returns the find group, or empty string if not found.
	 */
	public static String findGroup(Pattern pattern, CharSequence s, int group) {
		return findGroup(matcher(pattern, s), group);
	}

	/**
	 * Returns the find group, or empty string if not found.
	 */
	public static String findGroup(Matcher m, int group) {
		return group(find(m), group);
	}

	/**
	 * Calls the consumer if the matcher has a match.
	 */
	public static <E extends Exception> boolean accept(Matcher m,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		if (consumer == null || !hasMatch(m)) return false;
		consumer.accept(m);
		return true;
	}

	/**
	 * Calls the function if the matcher has a match, returns default if not.
	 */
	public static <E extends Exception, T> T apply(Matcher m,
		Excepts.Function<E, ? super Matcher, T> function, T def) throws E {
		if (function == null || !hasMatch(m)) return def;
		return function.apply(m);
	}

	/**
	 * Calls the consumer on successful match, or returns false.
	 */
	public static <E extends Exception> boolean matchAccept(Pattern pattern, CharSequence text,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		return accept(match(matcher(pattern, text)), consumer);
	}

	/**
	 * Calls the function on successful match, or returns default.
	 */
	public static <E extends Exception, T> T matchApply(Pattern pattern, CharSequence text,
		Excepts.Function<E, ? super Matcher, T> function, T def) throws E {
		return apply(match(matcher(pattern, text)), function, def);
	}

	/**
	 * Calls the consumer on the first successful find, or returns false.
	 */
	public static <E extends Exception> boolean findAccept(Pattern pattern, CharSequence text,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		return accept(find(matcher(pattern, text)), consumer);
	}

	/**
	 * Calls the function on the first successful find, or returns default.
	 */
	public static <E extends Exception, T> T findApply(Pattern pattern, CharSequence text,
		Excepts.Function<E, ? super Matcher, T> function, T def) throws E {
		return apply(find(matcher(pattern, text)), function, def);
	}

	/**
	 * Calls the consumer for each successful find with matcher and index, or returns false.
	 */
	public static <E extends Exception> boolean findAcceptAll(Pattern pattern, CharSequence text,
		Excepts.ObjIntConsumer<E, ? super Matcher> consumer) throws E {
		var m = matcher(pattern, text);
		if (m == null || consumer == null) return false;
		boolean found = false;
		int i = 0;
		while (m.find()) {
			consumer.accept(m, i++);
			found = true;
		}
		return found;
	}

	/**
	 * Returns a stream that provides the matcher on each successful find, with the intention of
	 * extracting information from the matcher with stream mapping.
	 */
	public static Stream<RuntimeException, Matcher> finds(Pattern pattern, CharSequence s) {
		return finds(matcher(pattern, s));
	}

	/**
	 * Returns a stream that provides the matcher on each successful find. More efficient than
	 * providing results, but not suitable for generating a collection for future processing.
	 */
	public static Stream<RuntimeException, Matcher> finds(Matcher m) {
		if (m == null) return Stream.empty();
		return Stream.ofSupplier(c -> {
			if (!m.find()) return false;
			c.accept(m);
			return true;
		});
	}

	/**
	 * Returns a stream that provides the group on each successful find. Groups may be null.
	 */
	public static Stream<RuntimeException, String> finds(Pattern pattern, CharSequence s,
		int group) {
		return finds(matcher(pattern, s), group);
	}

	/**
	 * Returns a stream that provides the group on each successful find. Groups may be null.
	 */
	public static Stream<RuntimeException, String> finds(Matcher matcher, int group) {
		return finds(matcher).map(m -> group(m, group));
	}

	/**
	 * Returns the matched group, or null if no match, or the index is out of range.
	 */
	public static String group(Matcher m, int index) {
		if (!hasMatch(m) || !Maths.within(index, 0, m.groupCount())) return null;
		return m.group(index);
	}

	/**
	 * Returns groups from 1 of the first find, or empty stream if no match.
	 */
	public static Stream<RuntimeException, String> matchGroups(Pattern pattern, CharSequence s) {
		return groups(match(pattern , s));
	}

	/**
	 * Returns groups from 1 of the first find, or empty stream if no match.
	 */
	public static Stream<RuntimeException, String> findGroups(Pattern pattern, CharSequence s) {
		return groups(find(pattern , s));
	}

	/**
	 * Returns groups from 1 of the given matcher as a stream, or empty stream if no match.
	 */
	public static Stream<RuntimeException, String> groups(Matcher m) {
		if (!hasMatch(m)) return Stream.empty();
		int count = m.groupCount();
		if (count <= 0) return Stream.empty();
		return Streams.slice(1, count).mapToObj(m::group);
	}

	/**
	 * Calls the consumer with the group and returns true if non-empty.
	 */
	public static <E extends Exception> boolean acceptGroup(Matcher m, int group,
		Excepts.Consumer<E, String> consumer) throws E {
		var s = group(m, group);
		if (consumer == null || s == null) return false;
		consumer.accept(s);
		return true;
	}

	/**
	 * Calls the function with the group if non-empty, otherwise returns default.
	 */
	public static <E extends Exception, T> T applyGroup(Matcher m, int group,
		Excepts.Function<E, String, T> function, T def) throws E {
		var s = group(m, group);
		if (function == null || s == null) return def;
		return function.apply(s);
	}

	/**
	 * Appends chars up to, and skips the match, for each find, then appends the remaining chars.
	 */
	public static String removeAll(Pattern p, CharSequence s) {
		return appendAll(p, s, (_, _) -> {});
	}

	/**
	 * Appends chars up to, and appends the replacement, for each find, then appends the remaining
	 * chars.
	 */
	public static String replaceAll(Pattern p, CharSequence s, CharSequence replace) {
		return appendAll(p, s, (b, _) -> b.append(Chars.safe(replace)));
	}

	/**
	 * Appends chars up to, and appends the replacement, for each find, then appends the remaining
	 * chars.
	 */
	public static StringBuilder replaceAll(StringBuilder b, Pattern p, CharSequence s,
		CharSequence replace) {
		return appendAll(b, p, s, (_, _) -> b.append(Chars.safe(replace)));
	}

	/**
	 * Appends chars up to, and calls the consumer, for each find, then appends the remaining chars.
	 * Optimized to only build the string if matches are found.
	 */
	public static String appendAll(Pattern p, CharSequence s,
		Functions.BiConsumer<StringBuilder, Matcher> consumer) {
		if (s == null || consumer == null) return "";
		return appendAll(StringBuilders.State.of(s), p,
			(b, m) -> consumer.accept(b.ensure(m.start()), m)).toString();
	}

	/**
	 * Appends chars up to, and calls the consumer, for each find, then appends the remaining chars.
	 * Optimized to only build the string if matches are found.
	 */
	public static StringBuilder appendAll(StringBuilder b, Pattern p, CharSequence s,
		Functions.BiConsumer<StringBuilder, Matcher> consumer) {
		if (b == null || consumer == null) return b;
		appendAll(StringBuilders.State.wrap(s, b), p, (_, m) -> consumer.accept(b, m));
		return b;
	}

	/**
	 * Appends chars up to, and calls the consumer, for each find, then appends the remaining chars.
	 * Optimized to only build the string if matches are found.
	 */
	public static StringBuilders.State appendAll(StringBuilders.State b, Pattern p,
		Functions.BiConsumer<StringBuilders.State, Matcher> consumer) {
		if (p == null || b.isEmpty()) return b;
		var m = p.matcher(b.s);
		int last = 0; // start position of next append
		while (m.find()) {
			b.append(last, m.start() - last);
			consumer.accept(b, m);
			last = m.end();
		}
		b.append(last, b.length());
		return b;
	}

	// support

	private static String f(String def, String format, Object... args) {
		if (Strings.isEmpty(format)) return def;
		return Strings.format(format, args);
	}
}
