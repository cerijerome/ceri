package ceri.process.util.parse;

import static ceri.common.collection.CollectionUtil.toList;
import static ceri.common.collection.StreamUtil.toList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Columns {
	private static final Pattern HEADER_SPLIT_REGEX = Pattern.compile("(\\S+\\s+)");
	private static final String REGEX_ALL = ".*";
	private final List<String> names;
	private final List<Pattern> patterns;

	public static List<Map<String, String>> parseOutputWithHeader(String output) {
		List<String> lines = ParseUtil.lines(output);
		if (lines.size() <= 1) return Collections.emptyList();
		Columns columns = Columns.fromHeader(lines.get(0));
		return CollectionUtil.toList(columns::parse, lines.subList(1, lines.size()));
	}

	public static Columns fromHeader(String header) {
		List<String> columns = RegexUtil.findAll(HEADER_SPLIT_REGEX, header);
		int count = columns.size();
		Builder b = builder();
		int i = 0;
		for (String column : columns) {
			int width = ++i < count ? column.length() : 0;
			b.add(column.trim(), width);
		}
		return b.build();
	}

	public static Columns fromHeader(String header, int... widths) {
		List<String> patterns = toList(IntStream.of(widths).mapToObj(Columns::pattern));
		return fromHeader(header, patterns);
	}

	static String pattern(int width) {
		if (width == 0) return REGEX_ALL;
		return ".{" + width + "}";
	}

	public static Columns fromHeader(String header, Collection<String> patterns) {
		return fromHeaderPatterns(header, toList(Pattern::compile, patterns));
	}

	public static Columns fromHeaderPatterns(String header, Collection<Pattern> patterns) {
		List<String> names = ParseUtil.parseValues(header, patterns);
		return new Columns(names, patterns);
	}

	public static class Builder {
		final Collection<String> names = new ArrayList<>();
		final Collection<Pattern> patterns = new ArrayList<>();

		Builder() {}

		public Builder add(String name, int width) {
			return add(name, pattern(width));
		}

		public Builder add(String name, String pattern) {
			return add(name, Pattern.compile(pattern));
		}

		public Builder add(String name, Pattern pattern) {
			if (names.contains(name)) throw new IllegalArgumentException("Name already added: " +
				name);
			names.add(name);
			patterns.add(pattern);
			return this;
		}

		public Columns build() {
			return new Columns(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Columns(Builder builder) {
		this(builder.names, builder.patterns);
	}

	private Columns(Collection<String> names, Collection<Pattern> patterns) {
		this.names = ImmutableUtil.copyAsList(names);
		this.patterns = ImmutableUtil.copyAsList(patterns);
	}

	public int indexOf(String name) {
		return names.indexOf(name);
	}

	public Map<String, String> parse(String line) {
		List<String> values = ParseUtil.parseValues(line, patterns);
		int max = Math.min(values.size(), names.size());
		return StreamUtil.toMap(IntStream.range(0, max).boxed(), names::get, values::get);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(names, patterns);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Columns)) return false;
		Columns other = (Columns) obj;
		if (!EqualsUtil.equals(names, other.names)) return false;
		if (!EqualsUtil.equals(patterns, other.patterns)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, names, patterns).toString();
	}

}
