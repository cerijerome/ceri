package ceri.common.score;

import static ceri.common.collection.StreamUtil.toList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import ceri.common.comparator.Comparators;
import ceri.common.filter.Filter;
import ceri.common.filter.Filters;
import ceri.common.util.BasicUtil;

/**
 * Basic scores and score utilities.
 */
public class Scorers {
	private static final Scorer<Object> ONE = (t -> 1.0);
	private static final Scorer<Object> ZERO = (t -> 0.0);

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
	public static <T> List<ScoreResult<T>> results(Scorer<? super T> scorer, T... ts) {
		return results(scorer, Arrays.asList(ts));
	}

	public static <T> List<ScoreResult<T>> results(Scorer<? super T> scorer,
		Collection<? extends T> ts) {
		return toList(ts.stream().map(t -> ScoreResult.<T>of(t, scorer.score(t))).sorted());
	}

	public static <T> Filter<T> filter(Scorer<T> scorer, Filter<Double> filter) {
		return (t -> filter.filter(scorer.score(t)));
	}

	public static <T> Filter<T> filter(Scorer<T> scorer, Double min, Double max) {
		return filter(scorer, Filters.all(Filters.gte(min), Filters.lte(max)));
	}

	/**
	 * A score that returns a constant for all conditions.
	 */
	public static <T> Scorer<T> constant(double value) {
		if (value == 0.0) return zero();
		if (value == 1.0) return one();
		return (t -> value);
	}

	/**
	 * A score that returns 1.0 for all conditions.
	 */
	public static <T> Scorer<T> one() {
		return BasicUtil.uncheckedCast(ONE);
	}

	/**
	 * A score that returns 0.0 for all conditions.
	 */
	public static <T> Scorer<T> zero() {
		return BasicUtil.uncheckedCast(ZERO);
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
