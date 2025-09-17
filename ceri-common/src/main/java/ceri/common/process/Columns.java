package ceri.common.process;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.collection.Maps;
import ceri.common.stream.Streams;
import ceri.common.text.Splitter;

public class Columns {
	public final List<String> names;
	public final List<Splitter.Extractor> extractors;

	/**
	 * Creates an instance that uses fixed-width extractors based on header width. Header names
	 * should not have spaces.
	 */
	public static Columns fromFixedWidthHeader(String header) {
		return Streams.from(Splitter.of(header).extractToCompletion(Splitter.Extractor.bySpaces()))
			.nonNull().collect(Columns::builder, (b, ex) -> b.add(ex), Columns.Builder::build);
	}

	/**
	 * Creates an instance that uses the same extractor for all columns, including headers.
	 */
	public static Columns fromHeader(String header, Splitter.Extractor extractor) {
		return Streams.from(Splitter.of(header).extractToCompletion(extractor)).nonNull().collect(
			Columns::builder, (b, ex) -> b.add(ex.text(), extractor), Columns.Builder::build);
	}

	public static class Builder {
		final Collection<String> names = Lists.of();
		final Collection<Splitter.Extractor> extractors = Lists.of();

		Builder() {}

		public Builder add(int... widths) {
			Streams.ints(widths).mapToObj(Splitter.Extractor::byWidth).forEach(this::add);
			return this;
		}

		public Builder add(String name, int width) {
			return add(name, Splitter.Extractor.byWidth(width));
		}

		public Builder add(Splitter.Extraction extraction) {
			return add(extraction.text(), extraction.size());
		}

		public Builder add(Splitter.Extractor extractor) {
			return add(String.valueOf(names.size()), extractor);
		}

		public Builder add(String name, Splitter.Extractor extractor) {
			names.add(name);
			extractors.add(extractor);
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
		names = Immutable.list(builder.names);
		extractors = Immutable.list(builder.extractors);
	}

	/**
	 * Extract values from a line.
	 */
	public List<String> parse(String line) {
		return Streams.from(Splitter.of(line).extractAll(extractors)).map(Splitter.Extraction::text)
			.toList();
	}

	/**
	 * Extract values from a line, and apply header names.
	 */
	public Map<String, String> parseAsMap(String line) {
		var map = Maps.<String, String>link();
		int i = 0;
		for (var value : parse(line))
			map.put(names.get(i++), value); // cannot exceed number of names
		return map;
	}
}
