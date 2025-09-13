package ceri.common.property;

import java.util.List;
import java.util.Objects;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.text.Strings;

/**
 * A key path separator.
 */
public record Separator(String value) {
	public static final Separator NULL = new Separator("");
	public static final Separator DOT = new Separator(".");
	public static final Separator DASH = new Separator("-");
	public static final Separator SLASH = new Separator("/");
	public static final Separator COMMA = new Separator(",");
	public static final Separator COLON = new Separator(":");
	public static final Separator SEMICOLON = new Separator(";");

	/**
	 * For notification of matched substrings.
	 */
	private interface SubstringConsumer {
		/**
		 * Substring has been matched.
		 */
		void accept(String key, int start, int end);

		/**
		 * Full string has matched.
		 */
		default void accept(String key) {
			if (key != null) accept(key, 0, key.length());
		}
	}

	/**
	 * Collects and joins substrings, optimizing to avoid creation of new strings.
	 */
	private static class Collector implements SubstringConsumer {
		private final String separator;
		private StringBuilder b = null;
		private String full = null;

		private Collector(String separator) {
			this.separator = separator;
		}

		/**
		 * Returns the collected and joined substrings.
		 */
		public String get() {
			if (b != null) return b.toString();
			if (full != null) return full;
			return "";
		}

		@Override
		public void accept(String key, int start, int end) {
			if (start >= end) return;
			if (b == null && full == null && full(key, start, end)) full = key;
			else {
				if (full != null) builder().append(full);
				builder().append(key, start, end);
				full = null;
			}
		}

		private StringBuilder builder() {
			if (b == null) b = new StringBuilder();
			else b.append(separator);
			return b;
		}
	}

	/**
	 * Separator string must not be null.
	 */
	public Separator {
		Objects.requireNonNull(value);
	}

	/**
	 * Returns true if separator is blank.
	 */
	public boolean isNull() {
		return value().isEmpty();
	}

	/**
	 * Provide a root key.
	 */
	public Key root() {
		return new Key(this, "");
	}

	/**
	 * Returns true if this separator is at the given position of the string.
	 */
	public boolean matches(String s, int pos) {
		if (s == null) return false;
		return value.regionMatches(0, s, pos, value.length());
	}

	/**
	 * Join non-empty strings with separators.
	 */
	public String join(String key, String... subs) {
		var collector = new Collector(value());
		collector.accept(key);
		for (String sub : subs)
			collector.accept(sub);
		return collector.get();
	}

	/**
	 * Join non-empty strings with separators.
	 */
	public String join(String[] pre, String... subs) {
		var collector = new Collector(value());
		for (String sub : pre)
			collector.accept(sub);
		for (String sub : subs)
			collector.accept(sub);
		return collector.get();
	}

	/**
	 * Join non-empty strings with separators, first removing any leading and trailing separators
	 * from each string.
	 */
	public String chomp(String key, String... subs) {
		var collector = new Collector(value());
		chomp(collector, key, value());
		for (String sub : subs)
			chomp(collector, sub, value());
		return collector.get();
	}

	/**
	 * Join non-empty strings with separators, first removing any leading and trailing separators
	 * from each string.
	 */
	public String chomp(String[] pre, String... subs) {
		var collector = new Collector(value());
		for (String sub : pre)
			chomp(collector, sub, value());
		for (String sub : subs)
			chomp(collector, sub, value());
		return collector.get();
	}

	/**
	 * Join non-empty strings with separators, first removing any leading, trailing or duplicate
	 * separators from each string.
	 */
	public String normalize(String key, String... subs) {
		var collector = new Collector(value());
		split(collector, key, value(), true);
		for (String sub : subs)
			split(collector, sub, value(), true);
		return collector.get();
	}

	/**
	 * Join non-empty strings with separators, first removing any leading, trailing or duplicate
	 * separators from each string.
	 */
	public String normalize(String[] pre, String... subs) {
		var collector = new Collector(value());
		for (String sub : pre)
			split(collector, sub, value(), true);
		for (String sub : subs)
			split(collector, sub, value(), true);
		return collector.get();
	}

	/**
	 * Normalizes and converts separators.
	 */
	public String normalize(Separator separator, String key, String... subs) {
		boolean allowSingle = equals(separator);
		var collector = new Collector(separator.value());
		split(collector, key, value(), allowSingle);
		for (String sub : subs)
			split(collector, sub, value(), allowSingle);
		return collector.get();
	}

	/**
	 * Normalizes and converts separators.
	 */
	public String normalize(Separator separator, String[] pre, String... subs) {
		boolean allowSingle = equals(separator);
		var collector = new Collector(separator.value());
		for (String sub : pre)
			split(collector, sub, value(), allowSingle);
		for (String sub : subs)
			split(collector, sub, value(), allowSingle);
		return collector.get();
	}

	/**
	 * Splits the key into normalized parts.
	 */
	public List<String> split(String key) {
		var list = Lists.<String>of();
		split((_, start, end) -> list.add(key.substring(start, end)), key, value(), false);
		return Immutable.wrap(list);
	}

	/**
	 * Calls the collector for the key without any leading and trailing separators.
	 */
	private static void chomp(SubstringConsumer consumer, String key, String separator) {
		if (Strings.isEmpty(key)) return;
		int start = 0, end = key.length();
		if (!Strings.isEmpty(separator)) {
			while (Strings.equalsAt(key, start, separator))
				start += separator.length();
			while (end > start && Strings.equalsAt(key, end - separator.length(), separator))
				end -= separator.length();
		}
		if (start < end) consumer.accept(key, start, end);
	}

	/**
	 * Iterates over the key, collecting substrings between separators. 'Allow single' lets the
	 * range include a single separator, such as with normalization.
	 */
	private static void split(SubstringConsumer consumer, String key, String separator,
		boolean allowSingle) {
		if (Strings.isEmpty(key)) return;
		if (Strings.isEmpty(separator)) consumer.accept(key);
		else {
			int i = 0, copyFrom = 0, copyTo = 0, last = 0;
			while (i < key.length()) {
				if (!Strings.equalsAt(key, i, separator)) { // no separator
					i++;
					copyTo = i;
				} else if (allowSingle && last < i) { // allow singles && is first separator
					i += separator.length();
					last = i;
				} else { // disallow singles || is repeat separator
					if (copyFrom < copyTo) consumer.accept(key, copyFrom, copyTo);
					i += separator.length();
					last = i;
					copyFrom = i;
				}
			}
			if (copyFrom < copyTo) consumer.accept(key, copyFrom, copyTo);
		}
	}

	private static boolean full(String key, int start, int end) {
		return start == 0 && end == Strings.length(key);
	}
}
