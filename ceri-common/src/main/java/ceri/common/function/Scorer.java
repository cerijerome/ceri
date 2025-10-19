package ceri.common.function;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Collect;
import ceri.common.stream.DoubleStream;
import ceri.common.stream.Streams;

/**
 * Provides a score value for a typed object.
 */
public interface Scorer<T> {
	/** A scorer that returns the double value of the number, with null as 0. */
	Scorer<Number> VALUE = Scorer::value;

	/**
	 * Calculates the score for an element.
	 */
	double score(T t);

	/**
	 * Returns an element and its score.
	 */
	default Result<T> result(T t) {
		return result(this, t);
	}

	/**
	 * A holder of an element and its score.
	 */
	record Result<T>(T ref, double score) implements Comparable<Result<T>> {
		private static final Result<Object> NULL = new Result<>(null, 0.0);
		private static final Comparator<Result<?>> COMPARATOR =
			Compares.not(Compares.asDouble(Result::score));

		public static <T> Result<T> ofNull() {
			return Reflect.unchecked(NULL);
		}

		@Override
		public int compareTo(Result<T> other) {
			return COMPARATOR.compare(this, other);
		}

		@Override
		public String toString() {
			return ref() + "=" + score();
		}
	}

	/**
	 * Holds fixed score lookup values.
	 */
	class Lookup<T> implements Scorer<T> {
		public final Map<T, Double> map;

		public static class Builder<T> {
			final Map<T, Double> map = Maps.of();

			private Builder() {}

			/**
			 * Put values with fixed score.
			 */
			@SafeVarargs
			public final Builder<T> put(double score, T... ts) {
				return put(score, Lists.wrap(ts));
			}

			/**
			 * Put values with fixed score.
			 */
			public Builder<T> put(double score, Collection<? extends T> ts) {
				ts.forEach(t -> map.put(t, score));
				return this;
			}

			/**
			 * Builds the lookup map, with option to normalize values to sum to 1.
			 */
			public Lookup<T> build(boolean normalize) {
				return new Lookup<>(normalize ? normalize(map) : Immutable.map(map));
			}

			private static <T> Map<T, Double> normalize(Map<T, Double> map) {
				double sum = Math.abs(Streams.from(map.values()).mapToDouble(d -> d).sum());
				if (sum == 0.0) return Immutable.map(map);
				return Immutable.adaptMap(k -> k, v -> v / sum, map);
			}
		}

		/**
		 * Start building a new lookup map.
		 */
		public static <T> Builder<T> builder() {
			return new Builder<>();
		}

		private Lookup(Map<T, Double> map) {
			this.map = map;
		}

		/**
		 * Returns the lookup score, or applies the scorer if not mapped.
		 */
		public double score(Scorer<? super T> scorer, T t) {
			var value = map.get(t);
			return value != null ? value : Scorer.score(scorer, t);
		}

		/**
		 * Returns the lookup score, or 0 if not mapped.
		 */
		@Override
		public double score(T t) {
			return map.getOrDefault(t, 0.0);
		}
	}

	/**
	 * Returns the number double value, with null as 0.
	 */
	static double value(Number n) {
		if (n == null) return 0.0;
		return n.doubleValue();
	}

	/**
	 * Calculates the score for an element, or 0 if the scorer or element is null.
	 */
	static <T> double score(Scorer<? super T> scorer, T t) {
		return (scorer == null || t == null) ? 0.0 : scorer.score(t);
	}

	/**
	 * Calculates the result score for an element.
	 */
	static <T> Result<T> result(Scorer<? super T> scorer, T t) {
		return new Result<>(t, score(scorer, t));
	}

	/**
	 * A scorer that returns a constant for all elements.
	 */
	static <T> Scorer<T> constant(double value) {
		return _ -> value;
	}

	/**
	 * Creates a scorer that applies the scorer to the extracted type.
	 */
	static <T, U> Scorer<T> scoring(Functions.Function<? super T, ? extends U> extractor,
		Scorer<? super U> scorer) {
		return t -> score(scorer, Functional.apply(extractor, t, null));
	}

	/**
	 * Returns score results.
	 */
	@SafeVarargs
	static <T> List<Result<T>> results(Scorer<? super T> scorer, T... ts) {
		return results(scorer, Lists.wrap(ts));
	}

	/**
	 * Returns sorted score results.
	 */
	static <T> List<Result<T>> results(Scorer<? super T> scorer, Iterable<T> ts) {
		return Streams.from(ts).map(t -> result(scorer, t)).collect(Collect.sortedList());
	}

	/**
	 * Sorts values based on score, and returns the list.
	 */
	static <T, L extends List<T>> L sort(L list, Scorer<? super T> scorer) {
		return Lists.sort(list, comparator(scorer));
	}

	/**
	 * Returns a scoring comparator.
	 */
	static <T> Comparator<T> comparator(Scorer<T> scorer) {
		return (l, r) -> Compares.DOUBLE.compare(score(scorer, r), score(scorer, l));
	}

	/**
	 * Returns a filter based on score.
	 */
	static <E extends Exception, T> Excepts.Predicate<E, T> filter(Scorer<? super T> scorer,
		Excepts.Predicate<? extends E, ? super Double> filter) {
		return Filters.as(t -> score(scorer, t), filter);
	}

	/**
	 * Returns a filter based on score range.
	 */
	static <T> Excepts.Predicate<RuntimeException, T> filter(Scorer<? super T> scorer, Double min,
		Double max) {
		return filter(scorer, Filters.range(min, max));
	}

	/**
	 * Creates a scorer by multiplying scorer values together.
	 */
	@SafeVarargs
	static <T> Scorer<T> multiplied(Scorer<? super T>... scorers) {
		return multiplied(Lists.wrap(scorers));
	}

	/**
	 * Creates a scorer by multiplying scorer values together.
	 */
	static <T> Scorer<T> multiplied(Iterable<? extends Scorer<? super T>> scorers) {
		return t -> stream(scorers, t).reduce((i, j) -> i * j, 0.0);
	}

	/**
	 * Creates a scorer by averaging scorer values.
	 */
	@SafeVarargs
	static <T> Scorer<T> averaged(Scorer<? super T>... scorers) {
		return averaged(Lists.wrap(scorers));
	}

	/**
	 * Creates a scorer by averaging scorer values.
	 */
	static <T> Scorer<T> averaged(Iterable<? extends Scorer<? super T>> scorers) {
		return t -> stream(scorers, t).average();
	}

	/**
	 * Creates a scorer that multiplies scores over a collection.
	 */
	static <T> Scorer<Iterable<T>> multiply(Scorer<? super T> scorer) {
		return ts -> stream(scorer, ts).reduce((i, j) -> i * j, 0.0);
	}

	/**
	 * Creates a scorer that sums scores over a collection.
	 */
	static <T> Scorer<Iterable<T>> sum(Scorer<? super T> scorer) {
		return ts -> stream(scorer, ts).sum();
	}

	/**
	 * Creates a scorer that averages scores over a collection.
	 */
	static <T> Scorer<Iterable<T>> average(Scorer<? super T> scorer) {
		return ts -> stream(scorer, ts).average();
	}

	// support

	private static <T> DoubleStream<RuntimeException> stream(Scorer<? super T> scorer,
		Iterable<T> ts) {
		return Streams.from(ts).mapToDouble(t -> score(scorer, t));
	}

	private static <T> DoubleStream<RuntimeException>
		stream(Iterable<? extends Scorer<? super T>> scorers, T t) {
		return Streams.from(scorers).mapToDouble(s -> score(s, t));
	}
}
