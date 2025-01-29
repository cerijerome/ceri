package ceri.common.text;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.collection.StreamUtil;
import ceri.common.concurrent.Lazy;

/**
 * Similar to {@link java.util.regex.Matcher}, but finds sequences that do not match a pattern.
 */
public class NonMatcher implements NonMatchResult {
	private static final int INVALID = -1;
	private Matcher matcher;
	private CharSequence text;
	private int lastAppendPosition;
	private int first;
	private int last;
	private boolean noMoreFinds;

	private record Result(String input, int start, int end) implements NonMatchResult {
		@Override
		public String group() {
			verifyMatch(start);
			return input.substring(start, end);
		}

		@Override
		public String toString() {
			return isValid(start) ? group() : "[No match]";
		}
	}

	public static NonMatcher of(Pattern pattern, CharSequence text) {
		return new NonMatcher(pattern, text);
	}

	private NonMatcher(Pattern pattern, CharSequence text) {
		matcher = pattern.matcher(text);
		this.text = text;
		reset();
	}

	public NonMatcher appendReplacement(StringBuilder sb, String replacement) {
		verifyMatch(first);
		sb.append(text, lastAppendPosition, first);
		sb.append(replacement);
		lastAppendPosition = last;
		return this;
	}

	public StringBuilder appendTail(StringBuilder sb) {
		sb.append(text, lastAppendPosition, text.length());
		return sb;
	}

	public String replaceAll(String replacement) {
		return replaceAll(_ -> replacement);
	}

	public String replaceAll(Function<NonMatchResult, String> replacer) {
		reset();
		if (!find()) return text.toString();
		StringBuilder sb = new StringBuilder();
		do {
			String replacement = replacer.apply(this);
			appendReplacement(sb, replacement);
		} while (find());
		appendTail(sb);
		return sb.toString();
	}

	public String replaceFirst(String replacement) {
		return replaceFirst(_ -> replacement);
	}

	public String replaceFirst(Function<NonMatchResult, String> replacer) {
		reset();
		if (!find()) return text.toString();
		StringBuilder sb = new StringBuilder();
		String replacement = replacer.apply(this);
		appendReplacement(sb, replacement);
		appendTail(sb);
		return sb.toString();
	}

	public boolean matches() {
		boolean match = matcher.matches();
		first = match ? INVALID : matcher.regionStart();
		last = matcher.regionEnd();
		noMoreFinds = true;
		return !match;
	}

	public boolean find() {
		if (noMoreFinds()) return false;
		first = isValid(first) ? matcher.end() : matcher.regionStart();
		noMoreFinds = !matcher.find();
		last = noMoreFinds ? matcher.regionEnd() : matcher.start();
		return first != last ? true : find();
	}

	public boolean find(int start) {
		if (start < 0 || start > text.length()) throw new IndexOutOfBoundsException("start");
		reset();
		first = start;
		noMoreFinds = !matcher.find(start);
		last = noMoreFinds ? matcher.regionEnd() : matcher.start();
		return first != last ? true : find();
	}

	public NonMatchResult toResult() {
		return toResult(text.toString());
	}

	private NonMatchResult toResult(String text) {
		return new Result(text, first, last);
	}

	public Stream<NonMatchResult> results() {
		var textAsString = Lazy.unsafe(() -> text.toString());
		return StreamUtil.stream(this::find, () -> toResult(textAsString.get()));
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

	public Pattern pattern() {
		return matcher.pattern();
	}

	public NonMatcher region(int start, int end) {
		reset();
		matcher.region(start, end);
		return this;
	}

	public int regionStart() {
		return matcher.regionStart();
	}

	public int regionEnd() {
		return matcher.regionEnd();
	}

	public NonMatcher reset(CharSequence input) {
		matcher.reset(input);
		text = input;
		return reset();
	}

	public NonMatcher reset() {
		matcher.reset();
		first = INVALID;
		last = 0;
		noMoreFinds = false;
		lastAppendPosition = 0;
		return this;
	}

	@Override
	public String toString() {
		String group = isValid(first) ? group() : null;
		return ToString.forClass(this, pattern(), regionStart(), regionEnd(), group);
	}

	/**
	 * Invalidate non-match if previous matcher.find() failed, we are at the end already.
	 */
	private boolean noMoreFinds() {
		if (noMoreFinds) first = INVALID;
		return noMoreFinds;
	}

	/**
	 * Has find() been called?
	 */
	private static boolean isValid(int value) {
		return value != INVALID;
	}

	/**
	 * Throw exception if no match.
	 */
	private static void verifyMatch(int start) {
		if (!isValid(start)) throw new IllegalStateException("No match found");
	}

}
