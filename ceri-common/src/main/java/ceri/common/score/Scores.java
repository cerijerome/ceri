package ceri.common.score;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import ceri.common.comparator.Comparators;
import ceri.common.filter.Filter;
import ceri.common.util.BasicUtil;

/**
 * Basic scores and score utilities.
 */
public class Scores {
	private static final Score<Object> ONE = (t -> 1.0);
	private static final Score<Object> ZERO = (t -> 0.0);

	private Scores() {}

	public static <T> Comparator<T> comparator(Score<T> score) {
		return ((lhs, rhs) -> -Comparators.DOUBLE.compare(score.score(lhs), score.score(rhs)));
	}

	public static <T> void sort(List<T> ts, Score<? super T> score) {
		Comparator<? super T> comparator = comparator(score);
		Collections.sort(ts, comparator);
	}

	public static <T> Filter<T> filter(Score<T> score, Double min, Double max) {
		return (t -> {
			double value = score.score(t);
			if (min != null && value < min.doubleValue()) return false;
			if (max != null && value > max.doubleValue()) return false;
			return true;
		});			
	}

	/**
	 * A score that returns a constant for all conditions.
	 */
	public static <T> Score<T> constant(double value) {
		if (value == 0.0) return zero();
		if (value == 1.0) return one();
		return (t -> value);
	}

	/**
	 * A score that returns 1.0 for all conditions.
	 */
	public static <T> Score<T> one() {
		return BasicUtil.uncheckedCast(ONE);
	}

	/**
	 * A score that returns 0.0 for all conditions.
	 */
	public static <T> Score<T> zero() {
		return BasicUtil.uncheckedCast(ZERO);
	}

	/**
	 * Wraps a score, returning 0.0 for null values.
	 */
	public static <T> Score<T> nonNull(Score<? super T> score) {
		if (score == null) return zero();
		return (t -> t == null ? 0.0 : score.score(t));
	}

	/**
	 * A score that returns the double value of the number.
	 */
	public static <T extends Number> Score<T> value() {
		return nonNull(t -> t.doubleValue());
	}

	/**
	 * Score that multiplies the score of the key by its map value.
	 */
	public static <T, N extends Number> Score<Map<T, N>> multiplier(Score<? super T> score) {
		if (score == null) return zero();
		return (map -> {
			double total = 0.0;
			for (Map.Entry<T, N> entry : map.entrySet()) {
				if (entry.getValue() == null) continue;
				total += score.score(entry.getKey()) * entry.getValue().doubleValue();
			}
			return total;
		});
	}

}
