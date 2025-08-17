package ceri.common.score;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import ceri.common.collection.Immutable;
import ceri.common.text.ToString;

public class ScoreLookup<T> implements Scorer<T> {
	private final Map<T, Double> map;

	public static class Builder<T> {
		final Map<T, Double> map = new HashMap<>();
		boolean normalize = false;

		Builder() {}

		@SafeVarargs
		public final Builder<T> score(double score, T... ts) {
			return score(score, Arrays.asList(ts));
		}

		public Builder<T> score(double score, Collection<? extends T> ts) {
			ts.forEach(t -> map.put(t, score));
			return this;
		}

		public Builder<T> normalize() {
			normalize = true;
			return this;
		}

		public ScoreLookup<T> build() {
			return new ScoreLookup<>(this);
		}
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	ScoreLookup(Builder<T> builder) {
		map = builder.normalize ? normalize(builder.map) : Map.copyOf(builder.map);
	}

	private Map<T, Double> normalize(Map<T, Double> map) {
		double sum = sum(map.values());
		if (sum == 0.0) return Map.of();
		return Immutable.adaptMap(k -> k, v -> v / sum, map);
	}

	private double sum(Collection<Double> values) {
		double sum = 0;
		for (var f : values)
			sum += f;
		return sum;
	}

	@Override
	public double score(T t) {
		var value = map.get(t);
		if (value == null) return 0.0;
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(map);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof ScoreLookup<?> other) && Objects.equals(map, other.map);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, map);
	}
}
