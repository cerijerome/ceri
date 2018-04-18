package ceri.common.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.StreamUtil;

/**
 * For encoding and decoding delimiter-separated values such as csv and tsv.
 */
public class DsvCodec {
	public static final DsvCodec TSV = new DsvCodec('\t');
	public static final DsvCodec CSV = new DsvCodec(',');
	private static final Pattern UNQUOTE_REGEX = Pattern.compile("^\\s*\"(.*?)\"?\\s*$");
	private static final Pattern STARTING_QUOTES_REGEX = Pattern.compile("^\\s*(\"+)\\s*");
	private static final Pattern NEW_LINE_REGEX = Pattern.compile("\r?\n");
	private static final String NEW_LINE = "\r\n";
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
		return Stream.of(lines).map(this::encodeLine).filter(Objects::nonNull)
			.collect(Collectors.joining(NEW_LINE));
	}

	public String encode(List<List<String>> lines) {
		if (lines == null) return null;
		return lines.stream().map(this::encodeLine).filter(Objects::nonNull)
			.collect(Collectors.joining(NEW_LINE));
	}

	public String encodeLine(String... values) {
		if (values == null) return null;
		return encodeLine(Arrays.asList(values));
	}

	public String encodeLine(List<String> values) {
		if (values == null) return null;
		if (values.isEmpty()) return "";
		return values.stream().map(this::encodeValue).filter(Objects::nonNull)
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
		if (document.isEmpty()) return Collections.emptyList();
		return StreamUtil.toList(
			NEW_LINE_REGEX.splitAsStream(document).map(this::decodeLine).filter(Objects::nonNull));
	}

	public List<String> decodeLine(String line) {
		if (line == null) return null;
		return CollectionUtil.toList(this::decodeValue, splitLine(line));
	}

	public String decodeValue(String value) {
		if (value == null) return null;
		return unquote(value).replaceAll("\"\"", "\"");
	}

	private String unquote(String s) {
		if (!quoted(s)) return s;
		Matcher m = UNQUOTE_REGEX.matcher(s);
		if (!m.find()) return s;
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
		if (line.isEmpty()) return Collections.emptyList();
		List<String> values = new ArrayList<>();
		StringBuilder b = new StringBuilder();
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
		String s = b.toString().trim();
		b.setLength(0);
		return s;
	}

}
