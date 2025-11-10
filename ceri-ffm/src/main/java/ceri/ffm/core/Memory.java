package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public class Memory {
	private Memory() {}

	/**
	 * Returns the address of the segment; 0 if null or a null pointer.
	 */
	public static long address(MemorySegment m) {
		return m == null ? 0L : m.address();
	}

	/**
	 * Returns the size of the segment; 0 if null or a null pointer.
	 */
	public static long size(MemorySegment m) {
		if (address(m) == 0L) return 0L;
		return m.byteSize();
	}

	/**
	 * Returns true if the segment is null or a null pointer.
	 */
	public static boolean isNull(MemorySegment m) {
		return m == null || m.address() == 0L;
	}

	/**
	 * Returns true if the segment is not a non-null pointer.
	 */
	public static boolean nonNull(MemorySegment m) {
		return !isNull(m);
	}

	/**
	 * Fails if the segment is null or a null pointer.
	 */
	public static MemorySegment valid(MemorySegment m) {
		if (nonNull(m)) return m;
		throw new NullPointerException("Memory segment is null");
	}

	/**
	 * Resizes the segment unless null or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment m, long size) {
		if (isNull(m)) return m;
		return m.reinterpret(size);
	}

	//

	public static void copy(MemorySegment from, long fromOffset, MemorySegment to, long toOffset,
		long count) {
		MemorySegment.copy(from, fromOffset, to, toOffset, count);
	}

	public static void copy(MemorySegment from, ValueLayout fromLayout, long fromOffset,
		MemorySegment to, ValueLayout toLayout, long toOffset, long count) {
		MemorySegment.copy(from, fromLayout, fromOffset, to, toLayout, toOffset, count);
	}

	public static void copy(Object from, int fromIndex, MemorySegment to, ValueLayout toLayout,
		long toOffset, int count) {
		MemorySegment.copy(from, fromIndex, to, toLayout, toOffset, count);
	}

	public static void copy(MemorySegment from, ValueLayout fromLayout, long fromOffset, Object to,
		int toIndex, int count) {
		MemorySegment.copy(from, fromLayout, fromOffset, to, toIndex, count);
	}

	public static MemorySegment allocateFrom(SegmentAllocator allocator, ValueLayout elementLayout,
		MemorySegment from, ValueLayout fromElementLayout, long fromOffset, long count) {
		return allocator.allocateFrom(elementLayout, from, fromElementLayout, fromOffset, count);
	}
}
