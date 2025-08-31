package ceri.common.text;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.Immutable;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Predicates;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;

/**
 * Support for regex patterns.
 */
public class Patterns {
	public static final Joiner OR_CAPTURE = Joiner.of("(", "|", ")");
	public static final Joiner OR = Joiner.of("(?:", "|", ")");
	private static final Pattern GROUP_NAME_REGEX = Pattern.compile("\\(\\?\\<([^>]+)\\>");
	public static final Pattern ALL = Pattern.compile(".*");

	private Patterns() {}

	/**
	 * Common patterns.
	 */
	public static class Common {
		/** Unsigned binary integer with 0b prefix (needs custom parser). */
		public static final String binUint = "0[bB][01]+";
		/** Signed binary integer with 0b prefix (needs custom parser). */
		public static final String binInt = "[+-]?0[bB][01]+";
		/** Unsigned octal integer with 0 prefix. */
		public static final String octUint = "0[0-7]+";
		/** Signed Octal integer with 0 prefix. */
		public static final String octInt = "[+-]?0[0-7]+";
		/** Unsigned decimal integer. */
		public static final String decUint = "(?:0|[1-9]\\d*)";
		/** Signed decimal integer. */
		public static final String decInt = "[+-]?" + decUint;
		/** Single hexadecimal digit. */
		public static final String hexDigit = "[a-fA-F0-9]";
		/** Unsigned hexadecimal integer with prefix. */
		public static final String hexUint = "(?:0x|0X|#)[a-fA-F0-9]+";
		/** Signed hexadecimal integer with prefix. */
		public static final String hexInt = "[+-]?" + hexUint;
		/** Unsigned binary, octal, decimal, or hexadecimal integer (use decodes below). */
		public static final String uintNumber = OR.joinAll(decUint, hexUint, octUint, binUint);
		/** Signed binary, octal, decimal, or hexadecimal integer (use decodes below). */
		public static final String intNumber = "[+-]?" + uintNumber;
		/** Unsigned decimal number; integer or floating point. */
		public static final String udecNumber = "(?:0|[1-9]\\d*|\\d*\\.\\d+)";
		/** Signed decimal number; integer or floating point. */
		public static final String decNumber = "[+-]?" + udecNumber;
		/** Single ASCII letter. */
		public static final String alphabet = "[a-zA-Z]";
		/** Single ASCII letter or number. */
		public static final String alphanum = "[a-zA-Z0-9]";
		/** Java identifier name. */
		public static final String javaName = "[\\p{L}$_][\\p{L}0-9$_]*";

		private Common() {}

		/**
		 * Use to decode UINT_NUMBER and INT_NUMBER.
		 */
		public static int decodeInt(String s) {
			return isBinaryPrefix(s) ? Integer.parseInt(s.substring(2), 2) : Integer.decode(s);
		}

		/**
		 * Use to decode UINT_NUMBER and INT_NUMBER.
		 */
		public static long decodeLong(String s) {
			return isBinaryPrefix(s) ? Long.parseLong(s.substring(2), 2) : Long.decode(s);
		}

		private static boolean isBinaryPrefix(String s) {
			return s.length() > 2 && s.charAt(0) == '0'
				&& (s.charAt(1) == 'b' || s.charAt(1) == 'B');
		}
	}

	/**
	 * Splits char sequences a pattern and optional modifier.
	 */
	public static class Split {
		public static final Split LINE = of("(?:\\r\\n|\\n|\\r)");
		public static final Split COMMA = of(Strings::trim, "\\s*,\\s*");
		public static final Split SPACE = of(Strings::trim, "\\s+");
		public final Pattern pattern;
		public final Functions.Function<? super String, String> modifier;

		/**
		 * Split the string into an array.
		 */
		public static String[] array(Pattern p, CharSequence s) {
			return array(p, s, null);
		}

		/**
		 * Split the string into an array, with item modifier.
		 */
		public static <E extends Exception> String[] array(Pattern p, CharSequence s,
			Excepts.Function<? extends E, ? super String, String> modifier) throws E {
			if (p == null || Strings.isEmpty(s)) return ArrayUtil.Empty.strings;
			var split = p.split(s);
			if (modifier != null) for (int i = 0; i < split.length; i++)
				split[i] = modifier.apply(split[i]);
			return split;
		}

		/**
		 * Split the string into an immutable list.
		 */
		public static List<String> list(Pattern p, CharSequence s) {
			return list(p, s, null);
		}

		/**
		 * Split the string into an immutable list, with item modifier.
		 */
		public static <E extends Exception> List<String> list(Pattern p, CharSequence s,
			Excepts.Function<? extends E, ? super String, String> modifier) throws E {
			return Immutable.wrapListOf(array(p, s, modifier));
		}

		/**
		 * Split the string as a stream.
		 */
		public static Stream<RuntimeException, String> stream(Pattern p, CharSequence s) {
			return stream(p, s, null);
		}

		/**
		 * Split the string as a stream, with item modifier.
		 */
		public static <E extends Exception> Stream<E, String> stream(Pattern p, CharSequence s,
			Excepts.Function<? extends E, ? super String, ? extends String> modifier) {
			if (p == null || Strings.isEmpty(s)) return Stream.empty();
			var stream = Stream.<E, String>from(p.splitAsStream(s));
			if (modifier != null) stream = stream.map(modifier);
			return stream;
		}

		public static Split of(String format, Object... args) {
			return of(null, format, args);
		}

		public static Split of(Functions.Function<? super String, String> modifier,
			String format, Object... args) {
			return of(modifier, compile(format, args));
		}

		public static Split of(Functions.Function<? super String, String> modifier,
			Pattern pattern) {
			return new Split(pattern, modifier);
		}

		private Split(Pattern pattern, Functions.Function<? super String, String> modifier) {
			this.pattern = pattern;
			this.modifier = modifier;
		}

		public String[] array(CharSequence s) {
			return array(pattern, s, modifier);
		}

		public List<String> list(CharSequence s) {
			return list(pattern, s, modifier);
		}

		public Stream<RuntimeException, String> stream(CharSequence s) {
			return stream(pattern, s, modifier);
		}
	}

	/**
	 * Regex filters.
	 */
	public static class Filter {
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
			if (pattern == null || predicate == null) return Predicates.no();
			return t -> t != null && predicate.test(pattern.matcher(t));
		}
	}

	/**
	 * Wrapper for matcher.
	 */
	public static class Match {
		public final Matcher matcher;

		private Match(Matcher matcher) {
			this.matcher = matcher;
		}

		/**
		 * Returns the matcher groups as a stream, or empty stream if no matches.
		 */
		public static Stream<RuntimeException, String> groups(Matcher m) {
			if (!hasMatch(m)) return Stream.empty();
			int count = m.groupCount();
			if (count <= 0) return Stream.empty();
			return Streams.slice(1, count).mapToObj(m::group);
		}

		public <E extends Exception> boolean accept(Excepts.Consumer<E, ? super Matcher> consumer)
			throws E {
			if (!matcher.hasMatch() || consumer == null) return false;
			consumer.accept(matcher);
			return true;
		}

		public <E extends Exception, T> T accept(Excepts.Function<E, ? super Matcher, T> function,
			T def) throws E {
			if (!matcher.hasMatch() || function == null) return def;
			return function.apply(matcher);
		}
	}

	/**
	 * Pattern does not override hashCode(); this method generates a hash code for a Pattern
	 * instance.
	 */
	public static int hash(Pattern pattern) {
		if (pattern == null) return Objects.hash();
		return Objects.hash(pattern.pattern(), pattern.flags());
	}

	/**
	 * Pattern does not override equals(); this method checks if patterns are equal.
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
	 * Compiles a pattern from string format.
	 */
	public static Pattern compile(String format, Object... objs) {
		return Pattern.compile(Strings.format(format, objs));
	}

	/**
	 * Compiles a pattern from joined strings.
	 */
	public static Pattern compile(Joiner joiner, Object... objs) {
		return Pattern.compile(joiner.joinAll(objs));
	}
	
	/**
	 * Creates a pattern to search for quoted text, ignoring case.
	 */
	public static Pattern ignoreCase(String text) {
		return compile("(?i)\\Q" + text + "\\E");
	}

	/**
	 * Returns the matcher after find().
	 */
	public static Matcher find(Matcher m) {
		if (m != null) m.find();
		return m;
	}

	/**
	 * Calls the consumer on successful find().
	 */
	public static <E extends Exception> boolean find(Matcher m,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		if (!hasMatch(find(m))) return false;
		consumer.accept(m);
		return true;
	}

	/**
	 * Calls the consumer on successful matches().
	 */
	public static <E extends Exception> boolean find(Pattern pattern, CharSequence text,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		return accept(find(pattern.matcher(text)), consumer);
	}

	/**
	 * Returns the matcher after matches().
	 */
	public static Matcher match(Matcher m) {
		if (m != null) m.matches();
		return m;
	}

	/**
	 * Calls the consumer on successful matches().
	 */
	public static <E extends Exception> boolean match(Pattern pattern, CharSequence text,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		return accept(match(pattern.matcher(text)), consumer);
	}

	/**
	 * Calls the consumer if the matcher has a match.
	 */
	public static <E extends Exception> boolean accept(Matcher m,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		if (!hasMatch(m)) return false;
		consumer.accept(m);
		return true;
	}

	/**
	 * Returns found groups as a stream.
	 */
	public static Stream<RuntimeException, String> findGroups(Pattern p, CharSequence s) {
		return groups(find(p.matcher(s)));
	}

	/**
	 * Returns matched groups as a stream.
	 */
	public static Stream<RuntimeException, String> matchGroups(Pattern p, CharSequence s) {
		return groups(match(p.matcher(s)));
	}

	/**
	 * Returns the groups of the given matcher as a stream, or empty stream if no match.
	 */
	public static Stream<RuntimeException, String> groups(Matcher m) {
		if (!hasMatch(m)) return Stream.empty();
		int count = m.groupCount();
		if (count <= 0) return Stream.empty();
		return Streams.slice(1, count).mapToObj(m::group);
	}

	/**
	 * Append chars up to each find match, and call the consumer for the match.
	 */
	public static String findAllAccept(Pattern p, CharSequence s,
		Functions.BiConsumer<StringBuilder, Matcher> consumer) {
		var b = findAllAppend0(null, p, s, consumer);
		return b == null ? s.toString() : b.toString();
	}
	
	/**
	 * Append chars up to each find match, and call the consumer for the match.
	 */
	public static StringBuilder findAllAppend(StringBuilder b, Pattern p, CharSequence s,
		Functions.BiConsumer<StringBuilder, Matcher> consumer) {
		if (b == null) return null;
		return findAllAppend0(b, p, s, consumer);
	}
	
	private static StringBuilder findAllAppend0(StringBuilder b, Pattern p, CharSequence s,
		Functions.BiConsumer<StringBuilder, Matcher> consumer) {
		if (p == null || Strings.isEmpty(s)) return b;
		var m = p.matcher(s);
		int last = 0; // start position of next append
		while (m.find()) {
			if (b == null) b = new StringBuilder();
			StringBuilders.append(b, s, last, m.start() - last);
			consumer.accept(b, m);
			last = m.end();
		}
		return StringBuilders.append(b, s, last);
	}
}
