package ceri.common.text;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.PrimitiveIterator;
import ceri.common.array.Array;
import ceri.common.function.Functions;
import ceri.common.io.IoStream;
import ceri.common.math.Maths;
import ceri.common.stream.IntStream;
import ceri.common.util.Basics;

public class StringBuilders {
	private StringBuilders() {}

	/**
	 * Stateful container that only starts building when it differs from the char sequence.
	 */
	public static class State {
		public final CharSequence s;
		private int index = 0; // the current index if b is null; may be > length
		private StringBuilder b = null;

		public static State of(CharSequence s) {
			return new State(s, null);
		}

		public static State wrap(CharSequence s, StringBuilder b) {
			return new State(s, b);
		}

		private State(CharSequence s, StringBuilder b) {
			this.s = Chars.safe(s);
			this.b = b;
		}

		/**
		 * Returns the char at index, or NUL if out of range.
		 */
		public char at(int i) {
			return Chars.at(s, i, Chars.NUL);
		}

		/**
		 * Returns the length of the char sequence.
		 */
		public int length() {
			return s.length();
		}

		/**
		 * Returns true if the char sequence is empty.
		 */
		public boolean isEmpty() {
			return s.isEmpty();
		}

		/**
		 * Returns true if the builder is different from the char sequence.
		 */
		public boolean modified() {
			return b != null;
		}

		/**
		 * Ensures the builder is populated up to index i if not modified.
		 */
		public StringBuilder ensure(int i) {
			if (b != null) return b;
			b = new StringBuilder().append(s, 0, Maths.limit(i, 0, length()));
			index = b.length();
			return b;
		}

		/**
		 * If modified, ensures the builder is populated up to index, and appends the range.
		 * Otherwise only the index is updated.
		 */
		public State append(int i, int length) {
			return Array.applySlice(length(), i, length, (o, l) -> {
				if (l > 0 && modified()) b.append(s, o, o + l);
				index = Math.max(o, index) + l;
				return this;
			});
		}

		/**
		 * If modified, ensures the builder is populated up to index, and appends the char.
		 * Otherwise only the index is updated.
		 */
		public State append(int i, char c) {
			i = Maths.limit(i, 0, length());
			if (modified() || i >= length() || at(i) != c) ensure(i).append(c);
			index = Math.max(i, index) + 1;
			return this;
		}

		/**
		 * If modified, ensures the builder is populated up to index, and appends the char sequence.
		 * Otherwise only the index is updated.
		 */
		public State append(int i, CharSequence s) {
			return append(i, s, 0, Integer.MAX_VALUE);
		}

		/**
		 * If modified, ensures the builder is populated up to index, and appends the char sequence.
		 * Otherwise only the index is updated.
		 */
		public State append(int j, CharSequence s, int offset, int length) {
			return Array.applySlice(Strings.length(s), offset, length, (o, l) -> {
				int i = Maths.limit(j, 0, length());
				if (l > 0 && (modified() || i >= length() || !Strings.equals(this.s, i, s, o, l)))
					ensure(i).append(s, o, o + l);
				index = Math.max(i, index) + l;
				return this;
			});
		}

		/**
		 * If modified, appends the char. Otherwise only the index is updated.
		 */
		public State append(char c) {
			return append(index, c);
		}

		/**
		 * If modified, appends the char sequence. Otherwise only the index is updated.
		 */
		public State append(CharSequence s) {
			return append(s, 0, Integer.MAX_VALUE);
		}

		/**
		 * If modified, appends the char sequence. Otherwise only the index is updated.
		 */
		public State append(CharSequence s, int offset, int length) {
			return append(index, s, offset, length);
		}

		/**
		 * Returns the builder if modified, otherwise the char sequence.
		 */
		public CharSequence get() {
			if (modified()) return b;
			if (s.length() > index) return s.subSequence(0, index);
			return s; // return original char sequence
		}

		/**
		 * Returns the builder string if modified, otherwise the char sequence.
		 */
		@Override
		public String toString() {
			return get().toString();
		}
	}

	/**
	 * Returns a new builder if null.
	 */
	public static StringBuilder safe(StringBuilder b) {
		return b == null ? new StringBuilder() : b;
	}

	/**
	 * Clears the builder.
	 */
	public static StringBuilder clear(StringBuilder b) {
		if (b != null) b.setLength(0);
		return b;
	}

	/**
	 * Appends code points.
	 */
	public static <E extends Exception> StringBuilder append(StringBuilder b,
		IntStream<E> codePoints) throws E {
		if (b != null && codePoints != null) codePoints.forEach(b::appendCodePoint);
		return b;
	}

	/**
	 * Appends code points.
	 */
	public static StringBuilder append(StringBuilder b, PrimitiveIterator.OfInt codePoints) {
		if (b != null && codePoints != null) while (codePoints.hasNext())
			b.appendCodePoint(codePoints.nextInt());
		return b;
	}

	/**
	 * Appends bounded char sequence.
	 */
	public static StringBuilder append(StringBuilder b, CharSequence s) {
		return append(b, s, 0);
	}

	/**
	 * Appends bounded char sequence.
	 */
	public static StringBuilder append(StringBuilder b, CharSequence s, int offset) {
		return append(b, s, offset, Integer.MAX_VALUE);
	}

	/**
	 * Appends bounded char sequence.
	 */
	public static StringBuilder append(StringBuilder b, CharSequence s, int offset, int length) {
		if (b == null || s == null) return b;
		return Array.applySlice(s.length(), offset, length, (o, l) -> b.append(s, o, o + l));
	}

	/**
	 * Appends formatted text, or unformatted if no args.
	 */
	public static StringBuilder format(String format, Object... objs) {
		return format(new StringBuilder(), format, objs);
	}

	/**
	 * Appends formatted text, or unformatted if no args.
	 */
	public static StringBuilder format(StringBuilder b, String format, Object... objs) {
		if (format == null) return b;
		if (Array.isEmpty(objs)) return b.append(format);
		try (var f = new Formatter(b)) {
			f.format(format, objs);
			return b;
		}
	}

	/**
	 * Appends the char n times.
	 */
	public static StringBuilder repeat(StringBuilder b, char c, int n) {
		if (b != null) while (n-- > 0)
			b.append(c);
		return b;
	}

	/**
	 * Appends the char sequence n times.
	 */
	public static StringBuilder repeat(StringBuilder b, CharSequence s, int n) {
		if (b != null && !Strings.isEmpty(s)) while (n-- > 0)
			b.append(s);
		return b;
	}

	/**
	 * Removes chars from the start and end with value <= space.
	 */
	public static StringBuilder trim(StringBuilder b) {
		return trim(b, c -> c <= ' ');
	}

	/**
	 * Gets the current string then clears the builder.
	 */
	public static String flush(StringBuilder b) {
		String s = b.toString();
		clear(b);
		return s;
	}

	/**
	 * Returns a bounded substring.
	 */
	public static String sub(StringBuilder s, int offset) {
		return sub(s, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a bounded substring.
	 */
	public static String sub(StringBuilder s, int offset, int length) {
		if (Strings.isEmpty(s)) return "";
		return Array.applySlice(s.length(), offset, length, (o, l) -> s.substring(o, o + l));
	}

	/**
	 * Wrap a PrintStream around a string builder; automatic flush.
	 */
	public static PrintStream printStream(StringBuilder b) {
		return printStream(b, null);
	}

	/**
	 * Wrap a PrintStream around a string builder; automatic flush.
	 */
	public static PrintStream printStream(StringBuilder b, Charset charset) {
		return new PrintStream(outputStream(b, charset), true);
	}

	/**
	 * Wrap an OutputStream around a string builder.
	 */
	public static OutputStream outputStream(StringBuilder b) {
		return outputStream(b, null);
	}

	/**
	 * Wrap an OutputStream around a string builder.
	 */
	public static OutputStream outputStream(StringBuilder s, Charset charset) {
		if (s == null) return IoStream.nullOut;
		var cs = Basics.def(charset, Charset.defaultCharset());
		return new OutputStream() {
			@Override
			public void write(int b) {
				s.append(new String(Array.BYTE.of(b), cs));
			}

			@Override
			public void write(byte[] b, int off, int len) {
				s.append(new String(b, off, len, cs));
			}
		};
	}

	// support

	private static StringBuilder trim(StringBuilder b, Functions.IntPredicate predicate) {
		if (Strings.isEmpty(b)) return b;
		int start = 0, end = b.length();
		while (start < end && predicate.test(b.charAt(start)))
			start++;
		while (start < end && predicate.test(b.charAt(end - 1)))
			end--;
		if (start == end) return clear(b);
		if (end < b.length()) b.setLength(end);
		if (start > 0) b.delete(0, start);
		return b;
	}
}
