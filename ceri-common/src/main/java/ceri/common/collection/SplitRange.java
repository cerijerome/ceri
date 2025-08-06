package ceri.common.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;
import ceri.common.exception.Exceptions;
import ceri.common.function.Functions;
import ceri.common.text.ToString;
import ceri.common.util.BasicUtil;

/**
 * Splits a range into indexed sections such as {@code 0001222233}. Index lookup may Use linear or
 * binary search depending on the number of indexes.
 */
public class SplitRange {
	public static final int INVALID_INDEX = -1;
	public static final SplitRange NULL = of();
	private static final int LINEAR_MAX = 256; // roughly when search is more efficient
	private final int[] indexes; // the exclusive end index of each section
	private final IntProvider indexProvider;
	private final Functions.IntOperator indexFn;
	public final int length;
	public final int count;

	/**
	 * Interface for processing the index, offset, and length for a given position.
	 */
	public static interface Consumer {
		void accept(int index, int offset, int length);
	}

	/**
	 * Interface for processing the index, offset, and length for a given position.
	 */
	public static interface Function<T> {
		T apply(int index, int offset, int length);
	}

	/**
	 * Creates an indexed list of types using a length function.
	 */
	@SafeVarargs
	public static <T> SplitRange.Typed<T> typed(Functions.ToIntFunction<? super T> lengthFn,
		T... ts) {
		return typed(lengthFn, Arrays.asList(ts));
	}

	/**
	 * Creates an indexed list of types using a length function.
	 */
	public static <T> SplitRange.Typed<T> typed(Functions.ToIntFunction<? super T> lengthFn,
		Collection<T> ts) {
		return new Typed<>(ImmutableUtil.copyAsList(ts), from(lengthFn, ts));
	}

	/**
	 * Splits a range into indexed sections, with typed object lookup per section.
	 */
	public static class Typed<T> {
		private static final Typed<?> NULL = new Typed<>(List.of(), SplitRange.NULL);
		public final List<T> list;
		public final SplitRange range;

		public static <T> Typed<T> ofNull() {
			return BasicUtil.unchecked(NULL);
		}

		private Typed(List<T> list, SplitRange range) {
			this.list = list;
			this.range = range;
		}

		/**
		 * Returns the total length.
		 */
		public int length() {
			return range.length;
		}

		/**
		 * Returns the index in which the position appears, or -1 if outside the range.
		 */
		public int index(int position) {
			return range.index(position);
		}

		/**
		 * Returns the item at the index of the given position, or null if outside range.
		 */
		public T at(int position) {
			return CollectionUtil.getOrDefault(list, index(position), null);
		}

		/**
		 * Calls the consumer with the corresponding type and offset for the position, if within the
		 * range.
		 */
		public void accept(int position, Functions.ObjIntConsumer<? super T> consumer) {
			int index = index(position);
			if (index != INVALID_INDEX)
				consumer.accept(list.get(index), position - range.start(index));
		}

		/**
		 * Calls the function with the corresponding type and offset for the position, if within the
		 * range, and returns the result. Returns null if out of range.
		 */
		public <R> R apply(int position, Functions.ObjIntFunction<? super T, R> function) {
			int index = index(position);
			return index == INVALID_INDEX ? null :
				function.apply(list.get(index), position - range.start(index));
		}

		@Override
		public int hashCode() {
			return Objects.hash(range, list);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof SplitRange.Typed<?> other)) return false;
			return Objects.equals(range, other.range) && Objects.equals(list, other.list);
		}

		@Override
		public String toString() {
			return ToString.ofClass(this, range.indexes()).childrens(list).toString();
		}
	}

	/**
	 * Creates from section lengths.
	 */
	public static SplitRange from(int... lengths) {
		var enc = IntArray.Encoder.fixed(lengths.length);
		for (int i = 0, n = 0; i < lengths.length; i++) {
			n += lengths[i];
			enc.writeInt(n);
		}
		return new SplitRange(enc.ints());
	}

	/**
	 * Creates from types and a length function.
	 */
	@SafeVarargs
	public static <T> SplitRange from(Functions.ToIntFunction<? super T> lengthFn, T... ts) {
		return from(lengthFn, Arrays.asList(ts));
	}

	/**
	 * Creates from types and a length function.
	 */
	public static <T> SplitRange from(Functions.ToIntFunction<? super T> lengthFn,
		Collection<T> ts) {
		return from(ts.stream().mapToInt(lengthFn).toArray());
	}

	/**
	 * Creates from section indexes, which must be a non-decreasing sequence. The last index is the
	 * total length.
	 */
	public static SplitRange of(int... indexes) {
		for (int i = 1; i < indexes.length; i++)
			if (indexes[i - 1] > indexes[i]) throw Exceptions
				.illegalArg("Index cannot decrease: %d > %d [%d]", indexes[i - 1], indexes[i], i);
		return new SplitRange(indexes.clone());
	}

	private SplitRange(int[] indexes) {
		this.indexes = indexes;
		indexProvider = IntArray.Immutable.wrap(indexes);
		indexFn = indexes.length <= LINEAR_MAX ? this::linearIndex : this::searchIndex;
		length = indexes.length == 0 ? 0 : indexes[indexes.length - 1];
		count = indexes.length;
	}

	/**
	 * Provides the sequence of increasing lengths, ending in total length.
	 */
	public IntProvider indexes() {
		return indexProvider;
	}

	/**
	 * Calls the consumer with the corresponding index, offset, and length for the position, if
	 * within the range.
	 */
	public void accept(int position, Consumer consumer) {
		int index = index(position);
		if (index != INVALID_INDEX) consumer.accept(index, position - start(index), length(index));
	}

	/**
	 * Calls the function with the corresponding index, offset, and length for the position, if
	 * within the range, and returns the result. Returns null if out of range.
	 */
	public <T> T apply(int position, Function<T> function) {
		int index = index(position);
		return index == INVALID_INDEX ? null :
			function.apply(index, position - start(index), length(index));
	}

	/**
	 * Returns the index in which the position appears, or -1 if outside the range.
	 */
	public int index(int position) {
		if (position < 0 || position >= length) return INVALID_INDEX;
		return indexFn.applyAsInt(position);
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
		if (!(obj instanceof SplitRange other)) return false;
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
