package ceri.common.score;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ceri.common.collection.ImmutableUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class ScoreLookup<T> implements Scorer<T> {
	private final Map<T, Double> map;

	public static class Builder<T> {
		Map<T, Double> map = new HashMap<>();
		boolean normalize = false;

		Builder() {}

		public Builder<T> score(T t, Double score) {
			map.put(t, score);
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
		map =
			builder.normalize ? Collections.unmodifiableMap(normalize(builder.map)) : ImmutableUtil
				.copyAsMap(builder.map);
	}

	private Map<T, Double> normalize(Map<T, Double> map) {
		Map<T, Double> normalizedMap = new HashMap<>();
		double sum = sum(map.values());
		if (sum == 0.0) return Collections.emptyMap();
		for (Map.Entry<T, Double> entry : map.entrySet())
			normalizedMap.put(entry.getKey(), entry.getValue() / sum);
		return normalizedMap;
	}

	private double sum(Collection<Double> values) {
		double sum = 0;
		for (Double f : values)
			sum += f.doubleValue();
		return sum;
	}

	@Override
	public double score(T t) {
		Double value = map.get(t);
		if (value == null) return 0.0;
		return value.doubleValue();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(map);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ScoreLookup)) return false;
		ScoreLookup<?> other = (ScoreLookup<?>) obj;
		if (!EqualsUtil.equals(map, other.map)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, map).toString();
	}

}
