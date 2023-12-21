package ceri.common.collection;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.math.MathUtil;

/**
 * Tracks ranges of indexes. Allows adding and removing ranges. Uses linear or binary search
 * depending on the number of index ranges.
 */
public class IndexRanges {
	private static final int SIZE_DEF = 32;
	private static final int LINEAR_MAX = 256; // roughly when binary search is more efficient
	private final int linearMax;
	private int[] starts; // sorted range starts
	private int[] ends; // sorted range ends
	private int n = 0;

	/**
	 * Create an instance with default initial index storage array size.
	 */
	public static IndexRanges of() {
		return of(LINEAR_MAX, SIZE_DEF);
	}

	/**
	 * Create an instance with linear algorithm threshold, and initial index storage array size.
	 */
	public static IndexRanges of(int linearMax, int size) {
		validateMin(linearMax, 0);
		validateMin(size, 1);
		return new IndexRanges(linearMax, size);
	}

	private IndexRanges(int linearMax, int size) {
		this.linearMax = linearMax;
		starts = new int[size];
		ends = new int[size];
	}

	/**
	 * Calculates the total number of indexes in the ranges. Returns Integer.MIN_VALUE if all
	 * indexes 0 to Integer.MAX_VALUE are present.
	 */
	public int count() {
		int total = 0;
		for (int i = 0; i < n; i++)
			total += (ends[i] + 1 - starts[i]);
		return total;
	}

	/**
	 * Returns the number of distinct ranges.
	 */
	public int ranges() {
		return n;
	}

	/**
	 * Returns the first index, or -1 if none.
	 */
	public int first() {
		return n > 0 ? starts[0] : -1;
	}

	/**
	 * Returns the last index, or -1 if none.
	 */
	public int last() {
		return n > 0 ? ends[n - 1] : -1;
	}

	/**
	 * Add a new range. Merges with existing ranges.
	 */
	public IndexRanges add(int start, int end) {
		validateMin(start, 0);
		validateMin(end, start);
		if (n == 0) return insert(0, start, end);
		int iS = startIndex(start); // -1..n-1
		if (iS >= 0 && start > ends[iS] + 1) iS++; // -1..n
		int iE = endIndex(end); // 0..n
		if (iE < n && end < starts[iE] - 1) iE--; // -1..n
		if (iS > iE || iS == n || iE == -1) return insert(Math.max(0, iS), start, end);
		return reduce(Math.max(0, iS), Math.min(n - 1, iE), start, end);
	}

	/**
	 * Removes a new range. Splits and crops existing ranges.
	 */
	public IndexRanges remove(int start, int end) {
		validateMin(start, 0);
		validateMin(end, start);
		if (n == 0) return this;
		int iS = startIndex(start); // -1..n-1
		if (iS >= 0 && start == starts[iS]) iS--; // -1..n-1
		int iE = endIndex(end); // 0..n
		if (iE < n && end == ends[iE]) iE++; // 0..n
		if (iS == iE) return split(iS, start, end);
		return crop(iS, iE, start, end);
	}

	/**
	 * Move all indexes by the given amount, dropping any outside the positive int range.
	 */
	public IndexRanges shift(int count) {
		if (count == 0) return this;
		return count < 0 ? shiftLeft(-count) : shiftRight(count);
	}

	/**
	 * Consumes each index within the ranges.
	 */
	public <E extends Exception> void forEach(ExceptionIntConsumer<E> consumer) throws E {
		var iterator = iterator();
		while (iterator.hasNext())
			consumer.accept(iterator.next());
	}

	/**
	 * Streams all indexes within the ranges. The indexes must not be modified while the stream is
	 * active.
	 */
	public IntStream stream() {
		return StreamUtil.intStream(iterator());
	}

	/**
	 * Returns a primitive iterator for the ranges. The indexes must not be modified while the
	 * iterator is active.
	 */
	public PrimitiveIterator.OfInt iterator() {
		return new IntIterator();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder().append('[');
		for (int i = 0; i < n; i++) {
			if (i > 0) b.append(',');
			int diff = ends[i] - starts[i];
			b.append(starts[i]);
			if (diff > 1) b.append('-').append(ends[i]);
			else if (diff > 0) b.append(',').append(ends[i]);
		}
		return b.append(']').toString();
	}

	private class IntIterator implements PrimitiveIterator.OfInt {
		int index = 0;
		int i = -1;

		@Override
		public int nextInt() {
			if (index >= n) throw new IllegalStateException();
			if (i == -1) i = starts[index];
			else if (i < starts[index] || i > ends[index]) throw new IllegalStateException();
			else if (i < ends[index]) i++;
			else if (index < n - 1) i = starts[++index];
			else throw new NoSuchElementException();
			return i;
		}

		@Override
		public boolean hasNext() {
			if (index >= n) return false;
			return index < n - 1 || i < ends[index];
		}
	}

	private IndexRanges split(int i, int start, int end) {
		move(i, i + 1);
		ends[i] = start - 1;
		starts[i + 1] = end + 1;
		return this;
	}

	private IndexRanges crop(int iS, int iE, int start, int end) {
		if (iS >= 0) ends[iS] = Math.min(ends[iS], start - 1);
		if (iE < n) starts[iE] = Math.max(starts[iE], end + 1);
		return move(iE, iS + 1);
	}

	private IndexRanges insert(int i, int start, int end) {
		move(i, i + 1);
		starts[i] = start;
		ends[i] = end;
		return this;
	}

	private IndexRanges reduce(int iS, int iE, int start, int end) {
		starts[iS] = Math.min(starts[iS], start);
		ends[iS] = Math.max(ends[iE], end);
		return move(iE + 1, iS + 1);
	}

	private IndexRanges shiftLeft(int count) {
		if (count > last()) return clear();
		if (count <= first()) return shiftValues(-count);
		int iS = startIndex(count); // 0..n-1
		if (count > ends[iS]) iS++; // 0..n-1
		starts[iS] = Math.max(starts[iS], count);
		move(iS, 0);
		return shiftValues(-count);
	}

	private IndexRanges shiftRight(int count) {
		int max = Integer.MAX_VALUE - count;
		if (max < first()) return clear();
		if (max >= last()) return shiftValues(count);
		int iE = endIndex(max); // 0..n-1
		if (max < starts[iE]) iE--; // 0..n-1
		ends[iE] = Math.min(ends[iE], max);
		n = iE + 1;
		return shiftValues(count);
	}

	private IndexRanges shiftValues(int count) {
		for (int i = 0; i < n; i++) {
			starts[i] += count;
			ends[i] += count;
		}
		return this;
	}

	private IndexRanges move(int from, int to) {
		if (from == to) return this;
		int diff = to - from;
		ensureSize(n + diff);
		ArrayUtil.copy(starts, from, starts, to, n - from);
		ArrayUtil.copy(ends, from, ends, to, n - from);
		n += diff;
		return this;
	}

	private IndexRanges clear() {
		n = 0;
		return this;
	}

	private int startIndex(int start) {
		return n <= linearMax ? linearStartIndex(start) : searchStartIndex(start); // -1..n-1
	}

	private int endIndex(int end) {
		return n <= linearMax ? linearEndIndex(end) : searchEndIndex(end); // 0..n
	}

	private int searchStartIndex(int start) {
		int i = Arrays.binarySearch(starts, 0, n, start);
		return i >= -1 ? i : -2 - i;
	}

	private int searchEndIndex(int end) {
		int i = Arrays.binarySearch(ends, 0, n, end);
		return i >= 0 ? i : -1 - i;
	}

	private int linearStartIndex(int value) {
		for (int i = 0; i < n; i++)
			if (value < starts[i]) return i - 1;
		return n - 1;
	}

	private int linearEndIndex(int value) {
		for (int i = 0; i < n; i++)
			if (value <= ends[i]) return i;
		return n;
	}

	private void ensureSize(int size) {
		if (starts.length >= size) return;
		int newSize = MathUtil.multiplyLimit(size, 2);
		starts = Arrays.copyOf(starts, newSize);
		ends = Arrays.copyOf(starts, newSize);
	}
}
