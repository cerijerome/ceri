package ceri.common.score;

/**
 * Gives a score value for given object.
 */
public interface Scorer<T> {

	double score(T t);

	default ScoreResult<T> result(T t) {
		return ScoreResult.of(t, score(t));
	}

}
