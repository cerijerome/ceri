package ceri.common.score;

import java.util.Collection;
import java.util.Map;
import java.util.stream.DoubleStream;

/**
 * Basic scores and score utilities.
 */
public class CollectionScorers {

	private CollectionScorers() {}

	/**
	 * Creates a scorer that multiplies scores over a collection.
	 */
	public static <T> Scorer<Collection<T>> multiply(Scorer<? super T> scorer) {
		if (scorer == null) return Scorers.zero();
		return (ts -> ts.stream().mapToDouble(scorer::score).reduce((i, j) -> i * j).orElse(0.0));
	}

	/**
	 * Creates a scorer that sums scores over a collection.
	 */
	public static <T> Scorer<Collection<T>> sum(Scorer<? super T> scorer) {
		if (scorer == null) return Scorers.zero();
		return (ts -> ts.stream().mapToDouble(scorer::score).sum());
	}

	/**
	 * Creates a scorer that averages scores over a collection.
	 */
	public static <T> Scorer<Collection<T>> average(Scorer<? super T> scorer) {
		if (scorer == null) return Scorers.zero();
		return (ts -> ts.stream().mapToDouble(scorer::score).average().orElse(0.0));
	}

	/**
	 * Creates a scorer that adds the multiplied scores of each key/value in the map.
	 */
	public static <T, N extends Number> Scorer<Map<T, N>> mapMultiplySum(Scorer<? super T> scorer) {
		return mapMultiplySum(scorer, Scorers.value());
	}

	/**
	 * Creates a scorer that adds the multiplied scores of each key/value in the map.
	 */
	public static <K, V> Scorer<Map<K, V>> mapMultiplySum(Scorer<? super K> keyScorer,
		Scorer<? super V> valueScorer) {
		if (keyScorer == null || valueScorer == null) return Scorers.zero();
		return (map -> mapMultiply(map, keyScorer, valueScorer).sum());
	}

	private static <K, V> DoubleStream mapMultiply(Map<K, V> map, Scorer<? super K> keyScorer,
		Scorer<? super V> valueScorer) {
		return map.entrySet().stream().mapToDouble(e -> multiplyEntry(e, keyScorer, valueScorer));
	}

	private static <K, V> double multiplyEntry(Map.Entry<K, V> entry, Scorer<? super K> keyScorer,
		Scorer<? super V> valueScorer) {
		return keyScorer.score(entry.getKey()) * valueScorer.score(entry.getValue());
	}

}
