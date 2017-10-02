package ceri.common.score;

/**
 * Gives a score value for given object.
 */
public interface Scorer<T> {

	double score(T t);

}
