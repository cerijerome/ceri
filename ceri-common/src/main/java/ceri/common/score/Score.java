package ceri.common.score;

/**
 * Gives a score value for given object.
 */
public interface Score<T> {

	double score(T t);
	
}
