package ceri.common.text;

import java.util.Formatter;
import java.util.PrimitiveIterator;
import ceri.common.stream.IntStream;

public class StringBuilders {

	public StringBuilders() {}

	/**
	 * Append code points.
	 */
	public static <E extends Exception> StringBuilder append(StringBuilder b,
		IntStream<E> codePoints) throws E {
		if (b != null && codePoints != null) codePoints.forEach(b::appendCodePoint);
		return b;
	}

	/**
	 * Append code points.
	 */
	public static StringBuilder append(StringBuilder b, PrimitiveIterator.OfInt codePoints) {
		if (b != null && codePoints != null) while (codePoints.hasNext())
			b.appendCodePoint(codePoints.nextInt());
		return b;
	}

	/**
	 * Appends formatted text, or unformatted if no args.
	 */
	public static StringBuilder format(StringBuilder sb, String format, Object... objs) {
		if (format == null) return sb;
		if (objs == null || objs.length == 0) return sb.append(format);
		try (var f = new Formatter(sb)) {
			f.format(format, objs);
			return sb;
		}
	}

}
