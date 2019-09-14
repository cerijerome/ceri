package ceri.common.text;

/**
 * Common interface for String and StringBuilder.
 */
public interface StringType extends CharSequence {
	String substring(int start);

	String substring(int start, int end);

	int indexOf(String str);

	int indexOf(String str, int fromIndex);

	default boolean isEmpty() {
		return length() == 0;
	}

	static StringType of(String s) {
		return of(Applicator.STRING, s);
	}

	static StringType of(StringBuilder s) {
		return of(Applicator.STRING_BUILDER, s);
	}

	static <T extends CharSequence> StringType of(Applicator<T> applicator, T t) {
		return new StringType() {
			@Override
			public String substring(int start) {
				return applicator.substring(t, start);
			}

			@Override
			public String substring(int start, int end) {
				return applicator.substring(t, start, end);
			}

			@Override
			public int indexOf(String str) {
				return applicator.indexOf(t, str);
			}

			@Override
			public int indexOf(String str, int fromIndex) {
				return applicator.indexOf(t, str, fromIndex);
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
		};
	}

	interface Applicator<T extends CharSequence> {
		Applicator<String> STRING = new Applicator<>() {
			@Override
			public String substring(String s, int start) {
				return s.substring(start);
			}

			@Override
			public String substring(String s, int start, int end) {
				return s.substring(start, end);
			}

			@Override
			public int indexOf(String s, String str) {
				return s.indexOf(str);
			}

			@Override
			public int indexOf(String s, String str, int fromIndex) {
				return s.indexOf(str, fromIndex);
			}
		};
		Applicator<StringBuilder> STRING_BUILDER = new Applicator<>() {
			@Override
			public String substring(StringBuilder s, int start) {
				return s.substring(start);
			}

			@Override
			public String substring(StringBuilder s, int start, int end) {
				return s.substring(start, end);
			}

			@Override
			public int indexOf(StringBuilder s, String str) {
				return s.indexOf(str);
			}

			@Override
			public int indexOf(StringBuilder s, String str, int fromIndex) {
				return s.indexOf(str, fromIndex);
			}
		};

		String substring(T t, int start);

		String substring(T t, int start, int end);

		int indexOf(T t, String str);

		int indexOf(T t, String str, int fromIndex);

		default boolean isEmpty(T t) {
			return t.length() == 0;
		}
	}

}
