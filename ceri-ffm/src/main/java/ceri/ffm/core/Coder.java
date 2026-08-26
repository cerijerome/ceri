package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.List;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.io.Direction;
import ceri.common.math.Maths;
import ceri.ffm.type.Memory;

/**
 * Encoding and decoding of types to memory with dynamic layout sizes.
 */
public class Coder {

	private Coder() {}

	/**
	 * Dynamically decodes types from memory.
	 */
	public static class In {
		private final MemorySegment memory;
		private final long end;
		private final long alignment;
		private long offset;

		private In(MemorySegment memory, long offset, long length, long alignment) {
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

	/**
	 * Dynamically encodes types to memory, with optional type updates after encoding.
	 */
	public static class Out {
		private final List<Functions.Consumer<MemorySegment>> encodings = Lists.of();
		private final List<Functions.Consumer<MemorySegment>> updates = Lists.of();
		private final Direction direction;
		private final long alignment;
		private long offset = 0;

		private Out(Direction direction, long alignment) {
			this.direction = direction;
			this.alignment = alignment;
		}

		/**
		 * Returns true if the encoder supports type encoding.
		 */
		public boolean in() {
			return Direction.in(direction);
		}

		/**
		 * Returns true if the encoder supports type updates after encoding.
		 */
		public boolean out() {
			return Direction.out(direction);
		}

		/**
		 * Accepts an encoding with known length, and an update to read back from the memory after
		 * processing.
		 */
		public Out accept(Memory.Consumer encoding, Memory.Consumer update, long length) {
			var offset = align();
			if (encoding != null && in()) encodings.add(m -> encoding.accept(m, offset, length));
			if (update != null && out()) updates.add(m -> update.accept(m, offset, length));
			inc(length);
			return this;
		}

		/**
		 * Adds padding/nul-termination of given size.
		 */
		public Out acceptNul(long length) {
			return accept((m, o, l) -> Memory.fill(m, o, l, 0), null, length);
		}

		/**
		 * Allocates memory and applies the encodings in sequence. The result also provides a method
		 * to update the source.
		 */
		public Native.Adapted<MemorySegment> alloc(SegmentAllocator allocator) {
			var memory = allocator.allocate(offset, alignment);
			for (var encoding : encodings)
				encoding.accept(memory);
			return Native.Adapted.of(memory, updates.isEmpty() ? null : this::resolve);
		}

		// support

		private long inc(long length) {
			offset += length;
			return offset;
		}

		private long align() {
			return inc(Layouts.padding(offset, alignment));
		}

		private void resolve(MemorySegment memory) {
			for (var update : updates)
				update.accept(memory);
		}
	}

	/**
	 * Creates a new decoder for bounded memory with given alignment.
	 */
	public static In decoder(MemorySegment memory, long offset, long length, long alignment) {
		if (Memory.isNull(memory)) return null;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		return new In(memory, offset, length, alignment);
	}

	/**
	 * Creates a new encoder with given alignment.
	 */
	public static Out encoder(Direction direction, long alignment) {
		return new Out(direction, alignment);
	}
}
