package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import ceri.common.math.Maths;

public class Decoder {
	private final MemorySegment memory;
	private final long end;
	private final long alignment;
	private long offset;

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

	public MemorySegment memory() {
		return memory;
	}

	public long offset() {
		return offset;
	}

	public long length() {
		return end - offset;
	}

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
