package ceri.common.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.stream.Streams;
import ceri.common.util.Validate;

/**
 * Utility for extracting text sequentially.
 */
public class Splitter {
	private static final Extractor REMAINDER_EXTRACTOR = regexGroupExtractor("(.*?)\\s*$");
	private static final Extractor SPACE_EXTRACTOR = regexGroupExtractor("(\\S*)\\s*(?:$|\\s)");
	private static final Extractor TAB_EXTRACTOR = regexGroupExtractor("(.*?)\t*(?:$|\t)");
	private final String text;
	private int pos = 0;

	public record Extraction(String text, int size) {
		public static final Extraction NULL = of("", 0);

		public static Extraction of(String text, int size) {
			Validate.nonNull(text);
			Validate.min(size, 0);
			return new Extraction(text, size);
		}

		public boolean isNull() {
			return size == 0 && text.isEmpty();
		}

		@Override
		public String toString() {
			return text + "[" + size + "]";
		}
	}

	/**
	 * Interface for extracting text starting at given position.
	 */
	public static interface Extractor extends Functions.ObjIntFunction<String, Extraction> {
		/**
		 * Extract text starting at given position. Pass the extracted text to the consumer, and
		 * return the next text position.
		 */
		// int extract(String text, int pos, Consumer<String> consumer);

		/**
		 * Collects the remaining text. The extracted text has whitespace truncated from the end.
		 */
		static Extractor byRemainder() {
			return Splitter.REMAINDER_EXTRACTOR;
		}

		/**
		 * Split by one or more tabs. Extracted text does not include the tabs.
		 */
		static Extractor byTabs() {
			return Splitter.TAB_EXTRACTOR;
		}

		/**
		 * Split by one or more spaces. Extracted text does not include the spaces.
		 */
		static Extractor bySpaces() {
			return Splitter.SPACE_EXTRACTOR;
		}

		/**
		 * Split by fixed a character width. The extracted text has whitespace truncated from the
		 * end.
		 */
		static Extractor byWidth(int count) {
			return Splitter.fixedWidthTruncationExtractor(count);
		}

		/**
		 * Split by fixed character widths. The extracted text has whitespace truncated from the
		 * end.
		 */
		static List<Extractor> byWidths(int... counts) {
			return Streams.ints(counts).mapToObj(Extractor::byWidth).toList();
		}

		/**
		 * Extract text using regex. The extracted text is match group 1 if it exists, otherwise
		 * group 0, the full match.
		 */
		static Extractor byRegex(String format, Object... args) {
			return byRegex(Regex.compile(format, args));
		}

		/**
		 * Extract text using regex. The extracted text is match group 1 if it exists, otherwise
		 * group 0, the full match.
		 */
		static Extractor byRegex(Pattern pattern) {
			return Splitter.regexGroupExtractor(pattern);
		}
	}

	public static Splitter of(String text) {
		return new Splitter(text);
	}

	private Splitter(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}

	public int position() {
		return pos;
	}

	public String remainder() {
		return text.substring(pos);
	}

	/**
	 * Run extractors from the current position, repeating the last one until end of text, or until
	 * no change in position.
	 */
	public List<Extraction> extractToCompletion(Extractor... extractors) {
		return extractToCompletion(Arrays.asList(extractors));
	}

	/**
	 * Run extractors from the current position, repeating the last one until end of text, or until
	 * no change in position.
	 */
	public List<Extraction> extractToCompletion(Collection<Extractor> extractors) {
		var list = new ArrayList<Extraction>();
		for (var i = extractors.iterator(); i.hasNext();) {
			var extractor = i.next();
			if (i.hasNext()) list.add(extract(extractor));
			else repeatExtraction(list, extractor);
		}
		return list;
	}

	/**
	 * Run extractors from the current position.
	 */
	public List<Extraction> extractAll(Extractor... extractors) {
		return extractAll(Arrays.asList(extractors));
	}

	/**
	 * Run extractors from the current position.
	 */
	public List<Extraction> extractAll(Collection<Extractor> extractors) {
		var list = Lists.<Extraction>of();
		for (var extractor : extractors)
			list.add(extract(extractor));
		return list;
	}

	/**
	 * Run an extractor from the current position.
	 */
	public Extraction extract(Extractor extractor) {
		if (pos >= text.length()) return Extraction.NULL;
		var extraction = extractor.apply(text, pos);
		pos += extraction.size;
		return extraction;
	}

	private void repeatExtraction(List<Extraction> list, Extractor extractor) {
		while (pos < text.length()) {
			var extraction = extractor.apply(text, pos);
			if (extraction.size == 0) return;
			list.add(extraction);
			pos += extraction.size;
		}
	}

	private static Extractor fixedWidthTruncationExtractor(long count) {
		return (text, pos) -> {
			int end = count == 0 ? text.length() : (int) Math.min(text.length(), pos + count);
			int size = end - pos;
			while (end > pos && Character.isWhitespace(text.charAt(end - 1)))
				end--;
			return Extraction.of(text.substring(pos, end), size);
		};
	}

	private static Extractor regexGroupExtractor(String pattern) {
		return regexGroupExtractor(Pattern.compile(pattern));
	}

	private static Extractor regexGroupExtractor(Pattern pattern) {
		return (text, pos) -> {
			var m = pattern.matcher(text);
			if (!m.find(pos)) return Extraction.NULL;
			var s = m.groupCount() >= 1 ? m.group(1) : null;
			if (s == null) s = m.group();
			return Extraction.of(s, m.end() - pos);
		};
	}
}
