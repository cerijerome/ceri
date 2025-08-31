package ceri.common.text;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.PrimitiveIterator;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Functions;
import ceri.common.io.IoStreamUtil;
import ceri.common.math.MathUtil;
import ceri.common.stream.IntStream;
import ceri.common.util.BasicUtil;

public class StringBuilders {
	private StringBuilders() {}

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
		return ArrayUtil.applySlice(s.length(), offset, length, (o, l) -> b.append(s, o, o + l));
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
		if (ArrayUtil.isEmpty(objs)) return b.append(format);
		try (var f = new Formatter(b)) {
			f.format(format, objs);
			return b;
		}
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
		return ArrayUtil.applySlice(s.length(), offset, length, (o, l) -> s.substring(o, o + l));
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
		if (s == null) return IoStreamUtil.nullOut;
		var cs = BasicUtil.def(charset, Charset.defaultCharset());
		return new OutputStream() {
			@Override
			public void write(int b) {
				s.append(new String(ArrayUtil.bytes.of(b), cs));
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
