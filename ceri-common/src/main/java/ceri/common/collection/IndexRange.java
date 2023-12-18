package ceri.common.collection;

import static ceri.common.validation.ValidationUtil.validateMax;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import ceri.common.function.ExceptionIntConsumer;

/**
 * Tracks ranges of indexes. Allows adding and removing ranges. Uses linear or binary search
 * depending on the number of index ranges.
 */
public class IndexRange {
	private static final int SIZE_DEF = 32;
	private static final int LINEAR_MAX = 256; // roughly when binary search is more efficient
	private int[] starts;
	private int[] ends;
	private int n = 0;

	/**
	 * Create an instance with default initial index storage array size.
	 */
	public static IndexRange of() {
		return of(SIZE_DEF);
	}

	/**
	 * Create an instance with initial index storage array size.
	 */
	public static IndexRange of(int size) {
		return new IndexRange(size);
	}

	private IndexRange(int size) {
		starts = new int[size];
		ends = new int[size];
	}

	/**
	 * Calculates the total numbers of indexes in the ranges.
	 */
	public int count() {
		int total = 0;
		for (int i = 0; i < n; i++)
			total += (ends[i] + 1 - starts[i]);
		return total;
	}

	/**
	 * Add a new range. Merges with existing ranges.
	 */
	public IndexRange add(int start, int end) {
		validateMax(start, end);
		if (n == 0) return insert(0, start, end);
		int iS = startIndex(start); // -1..n-1
		if (iS >= 0 && start > ends[iS] + 1) iS++; // -1..n
		int iE = endIndex(end); // 0..n
		if (iE < n && end < starts[iE] - 1) iE--; // -1..n
		System.out.printf("%s +(%d,%d) iS=%d iE=%d%n", this, start, end, iS, iE);
		if (iS > iE || iS == n || iE == -1) return insert(Math.max(0, iS), start, end);
		return reduce(Math.max(0, iS), Math.min(n - 1, iE), start, end);
	}

	/**
	 * Removes a new range. Splits and crops existing ranges.
	 */
	public IndexRange remove(int start, int end) {
		validateMax(start, end);
		if (n == 0) return this;
		int iS = startIndex(start); // -1..n-1
		if (iS >= 0 && start == starts[iS]) iS--; // -1..n-1
		int iE = endIndex(end); // 0..n
		if (iE < n && end == ends[iE]) iE++; // 0..n
		System.out.printf("%s -(%d,%d) iS=%d iE=%d%n", this, start, end, iS, iE);
		if (iS == iE) return split(iS, start, end);
		return crop(iS, iE, start, end);
	}

	/**
	 * Consumes each index within the ranges.
	 */
	public <E extends Exception> void forEach(ExceptionIntConsumer<E> consumer) throws E {
		var iterator = new IntIterator();
		while (iterator.hasNext())
			consumer.accept(iterator.next());
	}

	/**
	 * Streams all indexes within the ranges. The indexes must not be modified while the stream is
	 * active.
	 */
	public IntStream stream() {
		var iterator = new IntIterator();
		return StreamUtil.intStream(iterator::hasNext, iterator::next);
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

	private class IntIterator {
		int index = 0;
		int i = -1;

		public int next() {
			if (index >= n) throw new IllegalStateException();
			if (i == -1) i = starts[index];
			else if (i < starts[index] || i > ends[index]) throw new IllegalStateException();
			else if (i < ends[index]) i++;
			else if (index < n - 1) i = starts[++index];
			else throw new NoSuchElementException();
			return i;
		}

		public boolean hasNext() {
			if (index >= n) return false;
			return index < n - 1 || i < ends[index];
		}
	}

	private IndexRange split(int i, int start, int end) {
		move(i, i + 1);
		ends[i] = start - 1;
		starts[i + 1] = end + 1;
		return this;
	}

	private IndexRange crop(int iS, int iE, int start, int end) {
		if (iS >= 0) ends[iS] = Math.min(ends[iS], start - 1);
		if (iE < n) starts[iE] = Math.max(starts[iE], end + 1);
		move(iE, iS + 1);
		return this;
	}

	private IndexRange insert(int i, int start, int end) {
		move(i, i + 1);
		starts[i] = start;
		ends[i] = end;
		return this;
	}

	private IndexRange reduce(int iS, int iE, int start, int end) {
		starts[iS] = Math.min(starts[iS], start);
		ends[iS] = Math.max(ends[iE], end);
		move(iE + 1, iS + 1);
		return this;
	}

	private void move(int from, int to) {
		int diff = to - from;
		ensureSize(n + diff);
		ArrayUtil.copy(starts, from, starts, to, n - from);
		ArrayUtil.copy(ends, from, ends, to, n - from);
		n += diff;
	}

	private int startIndex(int start) {
		return n <= LINEAR_MAX ? linearStartIndex(start) : searchStartIndex(start); // -1..n-1
	}

	private int endIndex(int end) {
		return n <= LINEAR_MAX ? linearEndIndex(end) : searchEndIndex(end); // 0..n
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
		int n = (starts.length >= Integer.MAX_VALUE >> 1) ? Integer.MAX_VALUE : starts.length << 1;
		starts = Arrays.copyOf(starts, n);
		ends = Arrays.copyOf(starts, n);
	}
}
