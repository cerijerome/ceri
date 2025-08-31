package ceri.common.text;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import ceri.common.collection.Lists;
import ceri.common.stream.Streams;

/**
 * For encoding and decoding delimiter-separated values such as csv and tsv.
 */
public class DsvCodec {
	public static final DsvCodec TSV = new DsvCodec('\t');
	public static final DsvCodec CSV = new DsvCodec(',');
	private static final Pattern UNQUOTE_REGEX = Pattern.compile("^\\s*\"(.*?)\"?\\s*$");
	private static final Pattern STARTING_QUOTES_REGEX = Pattern.compile("^\\s*(\"+)\\s*");
	private static final Pattern NEW_LINE_REGEX = Pattern.compile("\r?\n");
	private static final Collector<CharSequence, ?, String> JOIN_LINES = Collectors.joining("\r\n");
	public final char delimiter;
	private final String delimiterStr;

	public static DsvCodec of(char delimiter) {
		if (delimiter == '\t') return TSV;
		if (delimiter == ',') return CSV;
		return new DsvCodec(delimiter);
	}

	private DsvCodec(char delimiter) {
		this.delimiter = delimiter;
		delimiterStr = String.valueOf(delimiter);
	}

	public String encode(String[]... lines) {
		if (lines == null) return null;
		return Streams.of(lines).map(this::encodeLine).nonNull().collect(JOIN_LINES);
	}

	public String encode(List<List<String>> lines) {
		if (lines == null) return null;
		return Streams.from(lines).map(this::encodeLine).nonNull().collect(JOIN_LINES);
	}

	public String encodeLine(String... values) {
		if (values == null) return null;
		return encodeLine(Arrays.asList(values));
	}

	public String encodeLine(List<String> values) {
		if (values == null) return null;
		if (values.isEmpty()) return "";
		return Streams.from(values).map(this::encodeValue).nonNull()
			.collect(Collectors.joining(delimiterStr));
	}

	public String encodeValue(String s) {
		if (s == null) return null;
		s = s.replaceAll("\"", "\"\"");
		if (s.indexOf(delimiter) != -1) s = "\"" + s + "\"";
		return s;
	}

	public List<List<String>> decode(String document) {
		if (document == null) return null;
		if (document.isEmpty()) return List.of();
		return Streams.of(NEW_LINE_REGEX.split(document)).map(this::decodeLine).nonNull().toList();
	}

	public List<String> decodeLine(String line) {
		if (line == null) return null;
		return Lists.adapt(this::decodeValue, splitLine(line));
	}

	public String decodeValue(String value) {
		if (value == null) return null;
		return unquote(value).replaceAll("\"\"", "\"");
	}

	private String unquote(String s) {
		if (!quoted(s)) return s;
		Matcher m = UNQUOTE_REGEX.matcher(s);
		m.find(); // always matches quoted strings
		return m.group(1);
	}

	private boolean quoted(String s) {
		Matcher m = STARTING_QUOTES_REGEX.matcher(s);
		if (!m.find()) return false;
		int len = m.group().length();
		int quotes = m.group(1).length();
		if (quotes % 2 == 1) return true;
		if (len != s.length()) return false;
		return quotes % 4 != 0;
	}

	private List<String> splitLine(String line) {
		if (line.isEmpty()) return List.of();
		var values = Lists.<String>of();
		var b = new StringBuilder();
		boolean inQuotes = false;
		int len = line.length();
		for (int i = 0; i < len; i++) {
			char c = line.charAt(i);
			if (c == delimiter && !inQuotes) values.add(flush(b));
			else b.append(c);
			if (c == '"') inQuotes = !inQuotes;
		}
		values.add(flush(b));
		return values;
	}

	private static String flush(StringBuilder b) {
		return StringBuilders.flush(b).trim();
	}
}
