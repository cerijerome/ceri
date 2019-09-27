package ceri.common.text;

/**
 * Common interface for String and StringBuilder.
 */
public interface StringType extends CharSequence {
	default String substring(int start) {
		return substring(start, length());
	}

	default String substring(int start, int end) {
		return subSequence(start, end).toString();
	}

	default boolean equalsString(String s) {
		return regionMatches(false, 0, s, 0, s.length());
	}

	default boolean equalsIgnoreCase(String s) {
		return regionMatches(true, 0, s, 0, s.length());
	}

	default int indexOf(String s) {
		return indexOf(s, 0);
	}

	default int indexOf(String s, int fromIndex) {
		int i = substring(fromIndex).indexOf(s);
		return i == -1 ? i : i + fromIndex;
	}

	default boolean startsWith(String s) {
		return startsWith(s, 0);
	}

	default boolean startsWith(String s, int offset) {
		return regionMatches(false, offset, s, 0, s.length());
	}

	default boolean startsWithIgnoreCase(String s) {
		return startsWithIgnoreCase(s, 0);
	}

	default boolean startsWithIgnoreCase(String s, int offset) {
		return regionMatches(true, offset, s, 0, s.length());
	}

	default boolean regionMatches(int offset, String s, int sOffset, int len) {
		return regionMatches(false, offset, s, sOffset, len);
	}

	default boolean regionMatches(boolean ignoreCase, int offset, String s, int sOffset, int len) {
		return substring(offset, offset + len).regionMatches(ignoreCase, 0, s, sOffset, len);
	}

	default boolean isEmpty() {
		return length() == 0;
	}

	static StringType of(String t) {
		return new StringType() {
			@Override
			public int indexOf(String s, int fromIndex) {
				return t.indexOf(s, fromIndex);
			}

			@Override
			public boolean regionMatches(boolean ignoreCase, int offset, String s, int sOffset,
				int len) {
				return t.regionMatches(ignoreCase, offset, s, sOffset, len);
			}

			@Override
			public int length() {
				return t.length();
			}

			@Override
			public char charAt(int index) {
				return t.charAt(index);
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return t.subSequence(start, end);
			}

			@Override
			public String toString() {
				return t;
			}
		};
	}

	static StringType of(StringBuilder t) {
		return new StringType() {
			@Override
			public int indexOf(String s, int fromIndex) {
				return t.indexOf(s, fromIndex);
			}

			@Override
			public int length() {
				return t.length();
			}

			@Override
			public char charAt(int index) {
				return t.charAt(index);
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return t.subSequence(start, end);
			}

			@Override
			public String toString() {
				return t.toString();
			}
		};
	}

}
