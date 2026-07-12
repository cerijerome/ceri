package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import ceri.common.math.Maths;

/**
 * Dynamically decodes types from memory.
 */
public class Decoder {
	private final MemorySegment memory;
	private final long end;
	private final long alignment;
	private long offset;

	/**
	 * Create an instance for bounded memory with given alignment.
	 */
	public static Decoder of(MemorySegment memory, long offset, long length, long alignment) {
		if (Segments.isNull(memory)) return null;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		return new Decoder(memory, offset, length, alignment);
	}

	private Decoder(MemorySegment memory, long offset, long length, long alignment) {
		this.memory = memory;
		this.alignment = alignment;
		this.offset = offset;
		this.end = offset + length;
	}

	/**
	 * Provides the memory segment to decode.
	 */
	public MemorySegment memory() {
		return memory;
	}

	/**
	 * Provides the current offset within the memory segment.
	 */
	public long offset() {
		return offset;
	}

	/**
	 * Returns the remaining length.
	 */
	public long length() {
		return end - offset;
	}

	/**
	 * Checks if the current offset has nul-termination of given size, and increment the offset.
	 */
	public boolean nul(long size) {
		boolean nul = Terminator.is(memory, offset(), size);
		if (nul) inc(size);
		return nul;
	}

	/**
	 * Increments the offset by given length, with automatic alignment padding if needed.
	 */
	public long inc(long length) {
		offset += Maths.limit(length, 0, length());
		offset += Layouts.padding(offset, alignment);
		return offset;
	}
}
