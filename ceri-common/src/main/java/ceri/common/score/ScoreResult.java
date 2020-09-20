package ceri.common.score;

import java.util.Comparator;
import java.util.Objects;

public class ScoreResult<T> implements Comparable<ScoreResult<T>> {
	private static final Comparator<ScoreResult<?>> COMPARATOR =
		Comparator.<ScoreResult<?>>comparingDouble(t -> t.score).reversed();
	public final T ref;
	public final double score;

	public static <T> ScoreResult<T> of(T ref, double score) {
		return new ScoreResult<>(ref, score);
	}

	private ScoreResult(T ref, double score) {
		this.ref = ref;
		this.score = score;
	}

	@Override
	public int compareTo(ScoreResult<T> other) {
		return COMPARATOR.compare(this, other);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ref, score);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ScoreResult)) return false;
		ScoreResult<?> other = (ScoreResult<?>) obj;
		if (!Objects.equals(ref, other.ref)) return false;
		if (!Objects.equals(score, other.score)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ref + "=" + score;
	}

}
