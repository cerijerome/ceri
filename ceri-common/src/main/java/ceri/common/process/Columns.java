package ceri.common.process;

import static ceri.common.collection.StreamUtil.collect;
import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.text.Splitter.Extractor.bySpaces;
import static java.util.function.Predicate.not;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import ceri.common.collection.ImmutableUtil;
import ceri.common.text.Splitter;
import ceri.common.text.Splitter.Extraction;
import ceri.common.text.Splitter.Extractor;

public class Columns {
	public final List<String> names;
	public final List<Extractor> extractors;

	/**
	 * Creates an instance that uses fixed-width extractors based on header width. Header names
	 * should not have spaces.
	 */
	public static Columns fromFixedWidthHeader(String header) {
		return collect(Splitter.of(header).extractToCompletion(bySpaces()).stream()
			.filter(not(Extraction::isNull)), Columns::builder, (b, ex) -> b.add(ex)).build();
	}

	/**
	 * Creates an instance that uses the same extractor for all columns, including headers.
	 */
	public static Columns fromHeader(String header, Extractor extractor) {
		return collect(Splitter.of(header).extractToCompletion(extractor).stream().filter(
			not(Extraction::isNull)), Columns::builder, (b, ex) -> b.add(ex.text(), extractor))
				.build();
	}

	public static class Builder {
		final Collection<String> names = new ArrayList<>();
		final Collection<Extractor> extractors = new ArrayList<>();

		Builder() {}

		public Builder add(int... widths) {
			IntStream.of(widths).mapToObj(Extractor::byWidth).forEach(this::add);
			return this;
		}

		public Builder add(String name, int width) {
			return add(name, Extractor.byWidth(width));
		}

		public Builder add(Extraction extraction) {
			return add(extraction.text(), extraction.size());
		}

		public Builder add(Extractor extractor) {
			return add(String.valueOf(names.size()), extractor);
		}

		public Builder add(String name, Extractor extractor) {
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
		names = ImmutableUtil.copyAsList(builder.names);
		extractors = ImmutableUtil.copyAsList(builder.extractors);
	}

	/**
	 * Extract values from a line.
	 */
	public List<String> parse(String line) {
		return toList(Splitter.of(line).extractAll(extractors).stream().map(ex -> ex.text()));
	}

	/**
	 * Extract values from a line, and apply header names.
	 */
	public Map<String, String> parseAsMap(String line) {
		Map<String, String> map = new LinkedHashMap<>();
		int i = 0;
		for (String value : parse(line))
			map.put(names.get(i++), value); // cannot exceed number of names
		return map;
	}

}
