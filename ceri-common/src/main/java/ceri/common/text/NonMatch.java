package ceri.common.text;

import static ceri.common.exception.Exceptions.indexOob;
import java.util.function.Function;
import java.util.regex.Pattern;
import ceri.common.function.Functions;
import ceri.common.stream.Stream;

/**
 * Using regex to find non-matching sequences.
 */
public class NonMatch {
	private static final int INVALID = -1;

	private NonMatch() {}

	/**
	 * A non-matching result group.
	 */
	public interface Result {
		/**
		 * Start index of the non-matched group.
		 */
		int start();

		/**
		 * End index of the non-matched group.
		 */
		int end();

		/**
		 * The non-matched group.
		 */
		String group();
	}

	private record Range(CharSequence input, int start, int end) implements Result {
		@Override
		public String group() {
			verifyMatch(start);
			return input.subSequence(start, end).toString();
		}

		@Override
		public String toString() {
			return isValid(start) ? group() : "[No match]";
		}
	}

	/**
	 * Replaces text that does not match the pattern.
	 */
	public static String replace(Pattern p, String s, String replacement) {
		return replace(p, s, (_, _) -> replacement);
	}

	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement.
	 */
	public static String replace(Pattern p, String s,
		Functions.Function<Result, ? extends CharSequence> replacer) {
		return replace(p, s, (t, _) -> replacer.apply(t));
	}

	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement.
	 * Replacement index is passed to the function.
	 */
	public static String replace(Pattern p, CharSequence s,
		Functions.ObjIntFunction<Result, ? extends CharSequence> replacer) {
		var m = NonMatch.of(p, s);
		var b = new StringBuilder();
		int start = 0; // start position of next append
		int i = 0;
		while (m.find()) {
			var replacement = replacer.apply(m, i++);
			if (replacement == null) continue;
			// Append from last append to m.start, then append replacement
			m.appendReplacement(b, replacement);
			start = m.end();
		}
		if (start == 0 && b.length() == 0) return s.toString();
		return m.appendTail(b).toString();
	}

	/**
	 * Create a non-matching matcher.
	 */
	public static Matcher of(Pattern pattern, CharSequence text) {
		return new Matcher(pattern.matcher(text), text);
	}

	/**
	 * Similar to {@link java.util.regex.Matcher}, but finds sequences that do not match a pattern.
	 */
	public static class Matcher implements NonMatch.Result {
		private java.util.regex.Matcher matcher;
		private CharSequence text;
		private int lastAppendPosition;
		private int first;
		private int last;
		private boolean noMoreFinds;

		private Matcher(java.util.regex.Matcher matcher, CharSequence text) {
			this.matcher = matcher;
			this.text = text;
			reset();
		}

		/**
		 * Appends the region up to the current non-match, and the replacement, to the builder.
		 */
		public Matcher appendReplacement(StringBuilder sb, CharSequence replacement) {
			verifyMatch(first);
			sb.append(text, lastAppendPosition, first);
			sb.append(replacement);
			lastAppendPosition = last;
			return this;
		}

		/**
		 * Appends the region after the current non-match to the builder.
		 */
		public StringBuilder appendTail(StringBuilder sb) {
			sb.append(text, lastAppendPosition, text.length());
			return sb;
		}

		/**
		 * Replaces all non-matching regions.
		 */
		public String replaceAll(CharSequence replacement) {
			return replaceAll(_ -> replacement);
		}

		/**
		 * Replaces all non-matching regions, based on result.
		 */
		public String replaceAll(Function<NonMatch.Result, ? extends CharSequence> replacer) {
			reset();
			if (!find()) return text.toString();
			StringBuilder sb = new StringBuilder();
			do {
				appendReplacement(sb, replacer.apply(this));
			} while (find());
			appendTail(sb);
			return sb.toString();
		}

		/**
		 * Replaces the first non-matching region.
		 */
		public String replaceFirst(CharSequence replacement) {
			return replaceFirst(_ -> replacement);
		}

		/**
		 * Replaces the first non-matching region, based on result.
		 */
		public String replaceFirst(Function<NonMatch.Result, ? extends CharSequence> replacer) {
			reset();
			if (!find()) return text.toString();
			var sb = new StringBuilder();
			appendReplacement(sb, replacer.apply(this));
			appendTail(sb);
			return sb.toString();
		}

		/**
		 * Returns true if the region is non-matching.
		 */
		public boolean matches() {
			boolean match = matcher.matches();
			first = match ? INVALID : matcher.regionStart();
			last = matcher.regionEnd();
			noMoreFinds = true;
			return !match;
		}

		/**
		 * Returns true if a non-match is found.
		 */
		public boolean find() {
			if (noMoreFinds()) return false;
			first = isValid(first) ? matcher.end() : matcher.regionStart();
			noMoreFinds = !matcher.find();
			last = noMoreFinds ? matcher.regionEnd() : matcher.start();
			return first != last ? true : find();
		}

		/**
		 * Returns true if a non-match is found, starting at the index.
		 */
		public boolean find(int start) {
			verifyBounds(start, text);
			reset();
			first = start;
			noMoreFinds = !matcher.find(start);
			last = noMoreFinds ? matcher.regionEnd() : matcher.start();
			return first != last ? true : find();
		}

		/**
		 * Save as an immutable result.
		 */
		public Result toResult() {
			return toResult(text.toString());
		}

		/**
		 * Stream results.
		 */
		public Stream<RuntimeException, Result> results() {
			return Stream.ofSupplier(c -> {
				if (!find()) return false;
				c.accept(toResult(text));
				return true;
			});
		}

		@Override
		public String group() {
			return text.subSequence(start(), end()).toString();
		}

		@Override
		public int start() {
			verifyMatch(first);
			return first;
		}

		@Override
		public int end() {
			verifyMatch(first);
			return last;
		}

		/**
		 * The pattern used for non-matches.
		 */
		public Pattern pattern() {
			return matcher.pattern();
		}

		/**
		 * Sets the region search bounds.
		 */
		public Matcher region(int start, int end) {
			reset();
			matcher.region(start, end);
			return this;
		}

		/**
		 * The start position of the search region.
		 */
		public int regionStart() {
			return matcher.regionStart();
		}

		/**
		 * The end position of the search region.
		 */
		public int regionEnd() {
			return matcher.regionEnd();
		}

		/**
		 * Resets the matcher with new input text.
		 */
		public Matcher reset(CharSequence input) {
			matcher.reset(input);
			text = input;
			return reset();
		}

		/**
		 * Resets the matcher, discarding state.
		 */
		public Matcher reset() {
			matcher.reset();
			first = INVALID;
			last = 0;
			noMoreFinds = false;
			lastAppendPosition = 0;
			return this;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, pattern(), regionStart(), regionEnd(),
				isValid(first) ? group() : null);
		}

		private boolean noMoreFinds() {
			// Invalidate non-match if previous matcher.find() failed, we are at the end already
			if (noMoreFinds) first = INVALID;
			return noMoreFinds;
		}

		private Range toResult(CharSequence text) {
			return new Range(text.toString(), first, last);
		}
	}

	private static boolean isValid(int value) {
		return value != INVALID;
	}

	private static void verifyMatch(int start) {
		if (!isValid(start)) throw new IllegalStateException("No match found");
	}

	private static void verifyBounds(int start, CharSequence text) {
		if (start < 0 || start > text.length()) throw indexOob("start", start, 0, text.length());
	}
}
