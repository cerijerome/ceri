package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.math.Maths;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Memory;

/**
 * A nul-terminator of specified size in bytes.
 */
public record Terminator(int size) {
	private static final int BLOCK_SIZE = 64; // Size of block used for searching
	private static final MemorySegment BLOCK = MemorySegment.ofArray(new byte[BLOCK_SIZE]);
	public static final Terminator BYTE = new Terminator(Byte.BYTES);
	public static final Terminator SHORT = new Terminator(Short.BYTES);
	public static final Terminator INT = new Terminator(Integer.BYTES);
	public static final Terminator LONG = new Terminator(Long.BYTES);

	/**
	 * Returns an instance for given layout size in bytes.
	 */
	public static Terminator from(MemoryLayout layout) {
		return of(Layouts.sizeInt(layout));
	}

	/**
	 * Returns an instance for given size in bytes.
	 */
	public static Terminator of(int size) {
		return switch (Math.max(1, size)) {
			case Byte.BYTES -> BYTE;
			case Short.BYTES -> SHORT;
			case Integer.BYTES -> INT;
			case Long.BYTES -> LONG;
			default -> new Terminator(size);
		};
	}

	/**
	 * Returns the first index of this terminator within the segment range, or -1 if not found.
	 */
	public long find(MemorySegment memory) {
		return find(memory, 0L);
	}

	/**
	 * Returns the first index of this terminator within the segment range, or -1 if not found.
	 */
	public long find(MemorySegment memory, long offset) {
		return find(memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Returns the first index of this terminator within the segment range, or -1 if not found.
	 */
	public long find(MemorySegment memory, long offset, long length) {
		if (Memory.isNull(memory)) return -1;
		offset = Maths.limit(offset, 0, memory.byteSize());
		length = Maths.limit(length, 0, memory.byteSize() - offset);
		return pos(memory, offset, length);
	}

	/**
	 * Returns the memory slice up to start of terminator, or null if not found.
	 */
	public MemorySegment slice(MemorySegment memory) {
		return slice(memory, 0L);
	}

	/**
	 * Returns the memory slice from offset to start of terminator, or null if not found.
	 */
	public MemorySegment slice(MemorySegment memory, long offset) {
		return slice(memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Returns the memory slice from offset to start of terminator, or null if not found.
	 */
	public MemorySegment slice(MemorySegment memory, long offset, long length) {
		if (Memory.isNull(memory)) return null;
		offset = Maths.limit(offset, 0, memory.byteSize());
		length = Maths.limit(length, 0, memory.byteSize() - offset);
		long pos = pos(memory, offset, length);
		if (pos < 0) return null;
		return memory.asSlice(offset, pos - offset);
	}

	/**
	 * Returns a sequential list of nul-terminated memory segments up to max count.
	 */
	public List<MemorySegment> slices(int count, int max, MemorySegment memory) {
		return slices(count, max, memory, 0L);
	}

	/**
	 * Returns a sequential list of nul-terminated memory segments up to max count.
	 */
	public List<MemorySegment> slices(int count, int max, MemorySegment memory, long offset) {
		return slices(count, max, memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Returns a sequential list of nul-terminated memory segments up to max length and count.
	 */
	public List<MemorySegment> slices(int count, int max, MemorySegment memory, long offset,
		long length) {
		if (max <= 0 || Memory.isNull(memory)) return List.of();
		offset = Maths.limit(offset, 0, memory.byteSize());
		length = Maths.limit(length, 0, memory.byteSize() - offset);
		long end = offset + length;
		var list = Lists.<MemorySegment>of();
		while (offset < end && list.size() < count) {
			long pos = pos(memory, offset, Math.min(length - offset, max));
			if (pos < 0) break;
			list.add(memory.asSlice(offset, pos - offset));
			offset = pos + size();
		}
		return Immutable.wrap(list);
	}

	/**
	 * Sets the terminator at given offset, within bounds. Returns the number of bytes set.
	 */
	public int set(MemorySegment memory, long offset) {
		return set(memory, offset, size());
	}

	/**
	 * Sets the terminator at given offset, within bounds. Returns the number of bytes set.
	 */
	public int set(MemorySegment memory, long offset, int max) {
		if (Memory.isNull(memory)) return 0;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		max = (int) Maths.limit(max, 0L, memory.byteSize() - offset);
		int size = (int) Maths.limit(max, 0L, size());
		switch (size) {
			case 0 -> {}
			case Byte.BYTES -> memory.set(Layouts.BYTE, offset, (byte) 0);
			case Short.BYTES -> memory.set(Layouts.SHORT, offset, (short) 0);
			case Integer.BYTES -> memory.set(Layouts.INT, offset, 0);
			case Long.BYTES -> memory.set(Layouts.LONG, offset, 0L);
			default -> memory.asSlice(offset, size).fill((byte) 0);
		}
		return size;
	}

	@Override
	public String toString() {
		return "nul[" + size() + "]";
	}

	// support

	private long pos(MemorySegment memory, long offset, long length) {
		boolean large = size() > BLOCK_SIZE;
		long end = offset + length - size();
		while (offset <= end) {
			if (large ? matchLarge(memory, offset) : matchSmall(memory, offset)) return offset;
			offset += size();
		}
		return -1L;
	}

	private boolean matchSmall(MemorySegment memory, long offset) {
		return MemorySegment.mismatch(memory, offset, offset + size(), BLOCK, 0, size()) == -1L;
	}

	private boolean matchLarge(MemorySegment memory, long offset) {
		long end = offset + size();
		while (offset < end) {
			long size = Math.min(BLOCK_SIZE, end - offset);
			if (MemorySegment.mismatch(memory, offset, offset + size, BLOCK, 0, size) >= 0)
				return false;
			offset += size;
		}
		return true;
	}
}
