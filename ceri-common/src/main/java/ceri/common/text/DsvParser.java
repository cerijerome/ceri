package ceri.common.text;

import static ceri.common.text.StringUtil.trim;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ceri.common.util.PrimitiveUtil;

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
	
	public Boolean booleanField(String headerValue) {
		return PrimitiveUtil.booleanValue(trim(field(headerValue)));
	}
	
	public Boolean booleanField(String headerValue, Boolean def) {
		return PrimitiveUtil.valueOf(trim(field(headerValue)), def);
	}
	
	public Integer intField(String headerValue) {
		return PrimitiveUtil.intValue(trim(field(headerValue)));
	}
	
	public Integer intField(String headerValue, Integer def) {
		return PrimitiveUtil.valueOf(trim(field(headerValue)), def);
	}
	
	public Long longField(String headerValue) {
		return PrimitiveUtil.longValue(trim(field(headerValue)));
	}
	
	public Long longField(String headerValue, Long def) {
		return PrimitiveUtil.valueOf(trim(field(headerValue)), def);
	}
	
	public Double doubleField(String headerValue) {
		return PrimitiveUtil.doubleValue(trim(field(headerValue)));
	}
	
	public Double doubleField(String headerValue, Double def) {
		return PrimitiveUtil.valueOf(trim(field(headerValue)), def);
	}
	
}
