package ceri.common.score;

import java.util.Comparator;

/**
 * Gives a score value for given object.
 */
public interface Scorer<T> {

	record Result<T>(T ref, double score) implements Comparable<Result<T>> {
		private static final Comparator<Result<?>> COMPARATOR =
			Comparator.<Result<?>>comparingDouble(t -> t.score).reversed();

		@Override
		public int compareTo(Result<T> other) {
			return COMPARATOR.compare(this, other);
		}

		@Override
		public String toString() {
			return ref + "=" + score;
		}
	}

	double score(T t);

	default Result<T> result(T t) {
		return new Result<>(t, score(t));
	}

}
