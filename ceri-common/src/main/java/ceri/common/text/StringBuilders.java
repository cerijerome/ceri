package ceri.common.text;

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

}
