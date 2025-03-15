package ceri.common.text;

import static ceri.common.text.StringUtil.trim;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ceri.common.property.Parser;

public class DsvParser {
	private final DsvCodec codec;
	private final List<String> header = new ArrayList<>();
	private final List<String> current = new ArrayList<>();
	private final List<String> immutableHeader = Collections.unmodifiableList(header);
	private final List<String> immutableCurrent = Collections.unmodifiableList(current);

	public static List<String> split(String s, DsvCodec codec) {
		if (s == null) return List.of();
		return of(codec).parseLine(s);
	}

	public static List<String> split(String s, char separator) {
		return split(s, DsvCodec.of(separator));
	}

	public static DsvParser of(DsvCodec codec) {
		return new DsvParser(codec);
	}

	private DsvParser(DsvCodec codec) {
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

	private Parser.String parseField(String value) {
		var trimmed = trim(value);
		if (StringUtil.empty(trimmed)) trimmed = null;
		return Parser.string(trimmed);
	}

}
