package ceri.common.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.property.Parser;
import ceri.common.stream.Streams;

public class Dsv {
	private static final Pattern UNQUOTE_REGEX = Pattern.compile("^\\s*\"(.*?)\"?\\s*$");
	private static final Pattern STARTING_QUOTES_REGEX = Pattern.compile("^\\s*(\"+)\\s*");
	private static final Pattern NEW_LINE_REGEX = Pattern.compile("\r?\n");
	private static final Collector<CharSequence, ?, String> JOIN_LINES = Collectors.joining("\r\n");
	private final Codec codec;
	private final List<String> header = new ArrayList<>();
	private final List<String> current = new ArrayList<>();
	private final List<String> immutableHeader = Immutable.wrap(header);
	private final List<String> immutableCurrent = Immutable.wrap(current);

	public static Codec codec(char delimiter) {
		if (delimiter == '\t') return Codec.TSV;
		if (delimiter == ',') return Codec.CSV;
		return new Codec(delimiter);
	}

	/**
	 * For encoding and decoding delimiter-separated values such as csv and tsv.
	 */
	public static class Codec {
		public static final Codec TSV = new Codec('\t');
		public static final Codec CSV = new Codec(',');
		public final char delimiter;
		private final String delimiterStr;

		private Codec(char delimiter) {
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
			return Streams.of(NEW_LINE_REGEX.split(document)).map(this::decodeLine).nonNull()
				.toList();
		}

		public List<String> decodeLine(String line) {
			if (line == null) return null;
			return Lists.adapt(Codec::decodeValue, splitLine(delimiter, line));
		}

		public static String decodeValue(String value) {
			if (value == null) return null;
			return unquote(value).replaceAll("\"\"", "\"");
		}
	}

	public static List<String> split(String s, Codec codec) {
		if (s == null) return List.of();
		return of(codec).parseLine(s);
	}

	public static List<String> split(String s, char separator) {
		return split(s, codec(separator));
	}

	public static Dsv of(Codec codec) {
		return new Dsv(codec);
	}

	private Dsv(Codec codec) {
		this.codec = codec;
	}

	public List<String> header() {
		return immutableHeader;
	}

	public List<String> current() {
		return immutableCurrent;
	}

	public List<String> parseLine(String line) {
		current.clear();
		current.addAll(codec.decodeLine(line));
		return current();
	}

	public boolean hasFields() {
		return !current.isEmpty();
	}

	public void applyHeader() {
		header.clear();
		header.addAll(current);
	}

	public boolean hasHeader() {
		return !header.isEmpty();
	}

	public boolean hasHeaderValue(String headerValue) {
		return header.indexOf(headerValue) != -1;
	}

	public String field(int index) {
		if (index < 0 || index >= current.size()) return null;
		return current.get(index);
	}

	public String field(String headerValue) {
		return field(header.indexOf(headerValue));
	}

	public String field(String headerValue, String def) {
		String field = field(headerValue);
		return field != null ? field : def;
	}

	public Parser.String parse(int index) {
		return parseField(field(index));
	}

	public Parser.String parse(String headerValue) {
		return parseField(field(headerValue));
	}

	private static Parser.String parseField(String value) {
		var trimmed = Strings.trim(value);
		if (Strings.isEmpty(trimmed)) trimmed = null;
		return Parser.string(trimmed);
	}

	private static List<String> splitLine(char delimiter, String line) {
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

	private static String unquote(String s) {
		if (!quoted(s)) return s;
		var m = UNQUOTE_REGEX.matcher(s);
		m.find(); // always matches quoted strings
		return m.group(1);
	}

	private static boolean quoted(String s) {
		var m = STARTING_QUOTES_REGEX.matcher(s);
		if (!m.find()) return false;
		int len = m.group().length();
		int quotes = m.group(1).length();
		if (quotes % 2 == 1) return true;
		if (len != s.length()) return false;
		return quotes % 4 != 0;
	}
}
