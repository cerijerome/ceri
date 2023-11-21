package ceri.common.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.text.ToString;

/**
 * Combines ranges into a single length. Looks up range index by value. Uses linear or binary search
 * depending on the number of indexes.
 */
public class Indexer {
	public static final int INVALID_INDEX = -1;
	public static final Indexer NULL = of();
	private static final int LINEAR_MAX = 256; // roughly when search is more efficient
	private final int[] indexes;
	private final IntProvider indexProvider;
	private final IntUnaryOperator indexFn;
	public final int length;

	/**
	 * Interface for processing the index, offset, and length for a given value.
	 */
	public static interface Consumer {
		void accept(int index, int offset, int length);
	}

	/**
	 * Interface for processing the index, offset, and length for a given value.
	 */
	public static interface Function<T> {
		T apply(int index, int offset, int length);
	}

	/**
	 * Creates from section lengths.
	 */
	public static Indexer from(int... lengths) {
		var enc = IntArray.Encoder.fixed(lengths.length);
		for (int i = 0, n = 0; i < lengths.length; i++) {
			n += lengths[i];
			enc.writeInt(n);
		}
		return new Indexer(enc.ints());
	}

	/**
	 * Creates from types and a length function.
	 */
	@SafeVarargs
	public static <T> Indexer from(ToIntFunction<T> lengthFn, T... ts) {
		return from(lengthFn, Arrays.asList(ts));
	}

	/**
	 * Creates from types and a length function.
	 */
	public static <T> Indexer from(ToIntFunction<T> lengthFn, Collection<T> ts) {
		return from(ts.stream().mapToInt(lengthFn).toArray());
	}

	/**
	 * Creates from section indexes. The indexes must be increasing, or behavior is undefined. The
	 * last index is the total length.
	 */
	public static Indexer of(int... indexes) {
		return new Indexer(indexes);
	}

	private Indexer(int[] indexes) {
		this.indexes = indexes;
		indexProvider = IntArray.Immutable.wrap(indexes);
		indexFn = indexes.length <= LINEAR_MAX ? this::linearIndex : this::searchIndex;
		length = indexes.length == 0 ? 0 : indexes[indexes.length - 1];
	}

	/**
	 * Provides the sequence of increasing lengths, ending in total length.
	 */
	public IntProvider indexes() {
		return indexProvider;
	}

	/**
	 * Calls the consumer with the corresponding index, offset, and length for the value, if within
	 * the range.
	 */
	public void accept(int value, Consumer consumer) {
		int index = index(value);
		if (index != INVALID_INDEX) consumer.accept(index, value - start(index), length(index));
	}

	/**
	 * Calls the function with the corresponding index, offset, and length for the value, if within
	 * the range and returns the result. Returns null if out of range.
	 */
	public <T> T apply(int value, Function<T> function) {
		int index = index(value);
		return index == INVALID_INDEX ? null :
			function.apply(index, value - start(index), length(index));
	}

	/**
	 * Returns the index in which the value appears, or -1 if outside the range.
	 */
	public int index(int value) {
		if (value < 0 || value >= length) return INVALID_INDEX;
		return indexFn.applyAsInt(value);
	}

	/**
	 * Returns the start of given index section, or 0 if outside the range.
	 */
	public int start(int index) {
		if (index < 0 || index >= indexes.length) return 0;
		return index == 0 ? 0 : indexes[index - 1];
	}

	/**
	 * Returns the length of given index section, or 0 if outside the range.
	 */
	public int length(int index) {
		if (index < 0 || index >= indexes.length) return 0;
		return index == 0 ? indexes[index] : indexes[index] - indexes[index - 1];
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(indexes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Indexer other)) return false;
		return Arrays.equals(indexes, other.indexes);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, indexes());
	}

	private int searchIndex(int value) {
		int i = Arrays.binarySearch(indexes, value);
		return i >= 0 ? i + 1 : -1 - i;
	}

	private int linearIndex(int value) {
		for (int i = 0; i < indexes.length - 1; i++)
			if (value < indexes[i]) return i;
		return indexes.length - 1;
	}

}
