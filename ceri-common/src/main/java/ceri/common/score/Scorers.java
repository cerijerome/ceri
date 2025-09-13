package ceri.common.score;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
import ceri.common.function.Predicates;
import ceri.common.reflect.Reflect;
import ceri.common.score.Scorer.Result;
import ceri.common.stream.Collect;
import ceri.common.stream.Streams;

/**
 * Basic scores and score utilities.
 */
public class Scorers {
	private static final Scorer<Object> ONE = (_ -> 1.0);
	private static final Scorer<Object> ZERO = (_ -> 0.0);

	private Scorers() {}

	public static double score(Number n) {
		if (n == null) return 0;
		return n.doubleValue();
	}

	public static <T> Comparator<T> comparator(Scorer<T> scorer) {
		return ((lhs, rhs) -> -Comparators.DOUBLE.compare(scorer.score(lhs), scorer.score(rhs)));
	}

	public static <T> void sort(List<T> ts, Scorer<? super T> scorer) {
		Comparator<? super T> comparator = comparator(scorer);
		ts.sort(comparator);
	}

	@SafeVarargs
	public static <T> List<Result<T>> results(Scorer<? super T> scorer, T... ts) {
		return results(scorer, Arrays.asList(ts));
	}

	public static <T> List<Result<T>> results(Scorer<? super T> scorer, Iterable<T> ts) {
		return Streams.from(ts).map(t -> new Result<>(t, scorer.score(t)))
			.collect(Collect.sortedList());
	}

	public static <E extends Exception, T> Excepts.Predicate<E, T> filter(Scorer<? super T> scorer,
		Excepts.Predicate<? extends E, ? super Double> filter) {
		return Predicates.testing(scorer::score, filter);
	}

	public static <T> Excepts.Predicate<RuntimeException, T> filter(Scorer<? super T> scorer,
		Double min, Double max) {
		return filter(scorer, Predicates.range(min, max));
	}

	/**
	 * A score that returns a constant for all conditions.
	 */
	public static <T> Scorer<T> constant(double value) {
		if (value == 0.0) return zero();
		if (value == 1.0) return one();
		return (_ -> value);
	}

	/**
	 * A score that returns 1.0 for all conditions.
	 */
	public static <T> Scorer<T> one() {
		return Reflect.unchecked(ONE);
	}

	/**
	 * A score that returns 0.0 for all conditions.
	 */
	public static <T> Scorer<T> zero() {
		return Reflect.unchecked(ZERO);
	}

	/**
	 * Wraps a score, returning 0.0 for null values.
	 */
	public static <T> Scorer<T> nonNull(Scorer<? super T> scorer) {
		if (scorer == null) return zero();
		return (t -> t == null ? 0.0 : scorer.score(t));
	}

	/**
	 * A score that returns the double value of the number.
	 */
	public static <T extends Number> Scorer<T> value() {
		return nonNull(Number::doubleValue);
	}

	/**
	 * Creates a scorer by multiplying scorer values together.
	 */
	@SafeVarargs
	public static <T> Scorer<T> multiply(final Scorer<T>... scorers) {
		return multiply(Arrays.asList(scorers));
	}

	/**
	 * Creates a scorer by multiplying scorer values together.
	 */
	public static <T> Scorer<T> multiply(Collection<Scorer<T>> scorers) {
		return nonNull(t -> scorers.stream().mapToDouble(scorer -> scorer.score(t))
			.reduce((i, j) -> i * j).orElse(0.0));
	}

	/**
	 * Creates a scorer by averaging scorer values.
	 */
	@SafeVarargs
	public static <T> Scorer<T> average(Scorer<T>... scorers) {
		return average(Arrays.asList(scorers));
	}

	/**
	 * Creates a scorer by averaging scorer values.
	 */
	public static <T> Scorer<T> average(Collection<Scorer<? super T>> scorers) {
		return nonNull(
			t -> scorers.stream().mapToDouble(scorer -> scorer.score(t)).average().orElse(0));
	}

}
