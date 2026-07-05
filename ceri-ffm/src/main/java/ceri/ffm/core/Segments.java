package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySegment.Scope;
import java.lang.foreign.SegmentAllocator;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.List;
import ceri.common.collect.Lists;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.io.Direction;
import ceri.common.math.Maths;
import ceri.common.text.Strings;

/**
 * Support for memory segments.
 */
public class Segments {
	private static final Cleaner CLEANER = Cleaner.create();
	public static final SegmentAllocator GLOBAL = Arena.global();

	private Segments() {}

	/**
	 * Consumer for a memory slice.
	 */
	public interface Consumer {
		void accept(MemorySegment memory, long offset, long length);
	}

	/**
	 * Function that accepts a memory slice and returns a value.
	 */
	public interface Function<T> {
		T apply(MemorySegment memory, long offset, long length);
	}

	/**
	 * Gets a new type from memory.
	 */
	public interface Get<T> {
		T apply(MemorySegment memory, long offset);
	}

	/**
	 * Updates a type from memory, or returns a new type from memory if immutable.
	 */
	public interface Update<T> {
		T apply(MemorySegment memory, long offset, T t);
	}

	/**
	 * Writes type instance to memory.
	 */
	public interface Write<T> {
		void accept(MemorySegment memory, long offset, T t);
	}

	/**
	 * Accepts bounded offsets and lengths.
	 */
	public interface BiSliceConsumer<E extends Exception> {
		void accept(long lOffset, long lLength, long rOffset, long rLength) throws E;
	}

	/**
	 * Applies bounded offsets and lengths.
	 */
	public interface BiSliceFunction<E extends Exception, T> {
		T apply(long lOffset, long lLength, long rOffset, long rLength) throws E;
	}

	/**
	 * Dynamically encodes types to memory.
	 */
	public static class Encoder {
		private final List<Functions.Consumer<MemorySegment>> encodings = Lists.of();
		private final List<Functions.Consumer<MemorySegment>> updates = Lists.of();
		private final Direction direction;
		private final long alignment;
		private long offset = 0;

		/**
		 * An encoding result with option to update sources after processing the allocated memory.
		 */
		public class Result {
			private final MemorySegment memory;

			private Result(MemorySegment memory) {
				this.memory = memory;
			}

			/**
			 * Provides the encoded memory.
			 */
			public MemorySegment memory() {
				return memory;
			}

			/**
			 * Updates sources after processing the encoded memory.
			 */
			public void update() {
				for (var update : updates)
					update.accept(memory);
			}
		}

		public class Builder {
			
		}
		
		private Encoder(Direction direction, long alignment) {
			this.direction = direction;
			this.alignment = alignment;
		}

		/**
		 * Returns true if the encoder is expecting type encoding.
		 */
		public boolean in() {
			return Direction.in(direction);
		}
		
		/**
		 * Returns true if the encoder is expecting type update.
		 */
		public boolean out() {
			return Direction.in(direction);
		}
		
		/**
		 * Accepts an encoding with known length, and an update to read back from the memory after
		 * processing.
		 */
		public void accept(Segments.Consumer encoding, Segments.Consumer update, long length) {
			var offset = this.offset;
			if (encoding != null && in()) encodings.add(m -> encoding.accept(m, offset, length));
			if (update != null && out()) updates.add(m -> update.accept(m, offset, length));
			this.offset += length;
		}

		/**
		 * Allocates memory and applies the encodings in sequence. The result also provides a method
		 * to update the source
		 */
		public Result alloc(SegmentAllocator allocator) {
			var memory = allocator.allocate(offset, alignment);
			for (var encoding : encodings)
				encoding.accept(memory);
			return new Result(memory);
		}
	}

	/**
	 * Utility that tracks the offset and length of a memory segment for sequential operations.
	 */
	public static class Decoder {
		private final MemorySegment memory;
		private final long end;
		private long offset;

		private Decoder(MemorySegment memory, long offset, long length) {
			this.memory = memory;
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

		public void inc(long length) {
			offset += Maths.limit(length, 0, length());
		}
	}

	/**
	 * An arena wrapper that can be closed or garbage collected.
	 */
	public static class Auto implements Arena, Functions.Closeable {
		private final java.lang.foreign.Arena arena;
		private final Cleanable cleanable;

		private Auto(Arena arena) {
			this.arena = arena;
			cleanable = CLEANER.register(this, arena::close); // must not reference fields
		}

		@Override
		public MemorySegment allocate(long byteSize, long byteAlignment) {
			return arena.allocate(byteSize, byteAlignment);
		}

		@Override
		public Scope scope() {
			return arena.scope();
		}

		@Override
		public void close() {
			cleanable.clean();
		}
	}

	/**
	 * Returns a new shared arena that can be closed or garbage collected.
	 */
	@SuppressWarnings("resource")
	public static Arena arena() {
		return new Auto(Arena.ofShared());
	}

	/**
	 * Returns a new auto arena.
	 */
	public static SegmentAllocator auto() {
		return Arena.ofAuto();
	}

	/**
	 * Returns a new decoder to provide a memory slice with moving offset.
	 */
	public static Decoder decoder(MemorySegment memory, long offset, long length) {
		if (memory == null) return null;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		return new Decoder(memory, offset, length);
	}

	/**
	 * Returns the address of the segment; 0 if null or a null pointer.
	 */
	public static long address(MemorySegment memory) {
		return memory == null ? 0L : memory.address();
	}

	/**
	 * Returns true if the segment is non-null and native.
	 */
	public static boolean isNative(MemorySegment memory) {
		return memory != null && memory.isNative();
	}

	/**
	 * Returns true if the segment is null or a native null pointer.
	 */
	public static boolean isNull(MemorySegment memory) {
		return memory == null || (memory.isNative() && memory.address() == 0L);
	}

	/**
	 * Returns true if the segment is not a non-null pointer.
	 */
	public static boolean nonNull(MemorySegment memory) {
		return !isNull(memory);
	}

	/**
	 * Returns the size of the segment; 0 if null or a null pointer.
	 */
	public static long size(MemorySegment memory) {
		return isNull(memory) ? 0L : memory.byteSize();
	}

	/**
	 * Returns the size of the segment from the offset; 0 if null, null pointer or out of range.
	 */
	public static long size(MemorySegment memory, long offset) {
		return Math.max(0L, size(memory) - Math.max(0L, offset));
	}

	/**
	 * Returns the size of the segment; 0 if null or a null pointer.
	 */
	public static int sizeInt(MemorySegment memory) {
		return sizeInt(memory, 0L);
	}

	/**
	 * Returns the size of the segment from the offset; 0 if null, null pointer or out of range.
	 */
	public static int sizeInt(MemorySegment memory, long offset) {
		return Math.toIntExact(size(memory, offset));
	}

	/**
	 * Returns the number of layout units available within bounds, or 0 if null.
	 */
	public static long count(MemorySegment memory, MemoryLayout layout) {
		return count(memory, layout, 0L);
	}

	/**
	 * Returns the number of layout units available within bounds, or 0 if null.
	 */
	public static long count(MemorySegment memory, MemoryLayout layout, long offset) {
		return Layouts.count(layout, size(memory, offset));
	}

	/**
	 * Returns the number of layout units available within bounds, or 0 if null.
	 */
	public static int countInt(MemorySegment memory, MemoryLayout layout) {
		return countInt(memory, layout, 0L);
	}

	/**
	 * Returns the number of layout units available within bounds, or 0 if null.
	 */
	public static int countInt(MemorySegment memory, MemoryLayout layout, long offset) {
		return Math.toIntExact(count(memory, layout, offset));
	}

	/**
	 * Returns true if the segment is null, a null pointer, or zero size.
	 */
	public static boolean isEmpty(MemorySegment memory) {
		return size(memory) == 0L;
	}

	/**
	 * Returns true if the segment not null, a null pointer, or zero size.
	 */
	public static boolean nonEmpty(MemorySegment memory) {
		return !isEmpty(memory);
	}

	/**
	 * Returns true if the segment scope is alive.
	 */
	public static boolean isAlive(MemorySegment memory) {
		return memory != null && memory.scope().isAlive();
	}

	/**
	 * Returns true if the segment is non-null, native, and has a byte size of 0.
	 */
	public static boolean isNativePointer(MemorySegment memory) {
		return isNative(memory) && memory.byteSize() == 0L;
	}

	/**
	 * Provides an alternative string descriptor.
	 */
	public static String string(MemorySegment memory) {
		if (memory == null) return Strings.NULL;
		return String.format("@%04x+%02x(%c)", address(memory), size(memory),
			isNative(memory) ? 'N' : 'H');
	}

	/**
	 * Returns the offset within bounds.
	 */
	public static long offset(MemorySegment memory, long offset) {
		return Maths.limit(offset, 0L, size(memory));
	}

	/**
	 * Fails if the segment is null or a null pointer.
	 */
	public static MemorySegment valid(MemorySegment memory) {
		if (nonNull(memory)) return memory;
		throw new NullPointerException("Memory segment is null");
	}

	/**
	 * Returns true if non-null and the slice is within bounds.
	 */
	public static boolean within(MemorySegment memory, long offset, long length) {
		if (isNull(memory)) return false;
		return offset >= 0L && offset <= memory.byteSize() && length >= 0L
			&& length <= memory.byteSize() - offset;
	}

	/**
	 * Returns true if non-null and the slice is within bounds.
	 */
	public static boolean within(MemorySegment memory, long offset, MemoryLayout layout,
		long count) {
		return within(memory, offset, Layouts.size(layout, count));
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment memory, long length) {
		return resize(memory, 0L, length);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment memory, MemoryLayout layout) {
		return resize(memory, layout, 1);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment memory, MemoryLayout layout, long count) {
		return resize(memory, 0L, layout, count);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment memory, long offset, long length) {
		if (isNull(memory)) return memory;
		offset = Math.max(0L, offset);
		length = Math.max(0L, length);
		if (offset + length > memory.byteSize()) memory = memory.reinterpret(offset + length);
		if (offset == 0L && length == memory.byteSize()) return memory;
		return memory.asSlice(offset, length);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment memory, long offset, MemoryLayout layout) {
		return resize(memory, offset, layout, 1);
	}

	/**
	 * Resizes the segment unless unchanged, null, or a null pointer.
	 */
	public static MemorySegment resize(MemorySegment memory, long offset, MemoryLayout layout,
		long count) {
		return resize(memory, offset, Layouts.size(layout, count));
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, long length) {
		return slice(memory, 0L, length);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, MemoryLayout layout) {
		return slice(memory, layout, 1L);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, MemoryLayout layout, long count) {
		return slice(memory, 0L, layout, count);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment sliceAll(MemorySegment memory, long offset) {
		return slice(memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, long offset, long length) {
		if (isNull(memory)) return memory;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		if (offset == 0L && length == memory.byteSize()) return memory;
		return memory.asSlice(offset, length);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, long offset, MemoryLayout layout) {
		return slice(memory, offset, layout, 1);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, long offset, MemoryLayout layout,
		long count) {
		return slice(memory, offset, Layouts.size(layout, count));
	}

	/**
	 * Returns an index view of a segment array that starts at the given offset.
	 */
	public static MemorySegment sliceAt(MemorySegment memory, long index, MemoryLayout layout) {
		return sliceAt(memory, index, layout, 1L);
	}

	/**
	 * Returns a view of a segment array that starts at the given layout index offset.
	 */
	public static MemorySegment sliceAt(MemorySegment memory, long index, MemoryLayout layout,
		long count) {
		return slice(memory, Layouts.size(layout, index), layout, count);
	}

	/**
	 * Resizes the segment if a native pointer, otherwise slices the segment within bounds.
	 */
	public static MemorySegment reslice(MemorySegment memory, long length) {
		return reslice(memory, 0L, length);
	}

	/**
	 * Resizes the segment if a native pointer, otherwise slices the segment within bounds.
	 */
	public static MemorySegment reslice(MemorySegment memory, MemoryLayout layout) {
		return reslice(memory, layout, 1L);
	}

	/**
	 * Resizes the segment if a native pointer, otherwise slices the segment within bounds.
	 */
	public static MemorySegment reslice(MemorySegment memory, MemoryLayout layout, long count) {
		return reslice(memory, 0L, layout, count);
	}

	/**
	 * Resizes the segment if a native pointer, otherwise slices the segment within bounds.
	 */
	public static MemorySegment reslice(MemorySegment memory, long offset, long length) {
		return isNativePointer(memory) ? resize(memory, offset, length) :
			slice(memory, offset, length);
	}

	/**
	 * Resizes the segment if a native pointer, otherwise slices the segment within bounds.
	 */
	public static MemorySegment reslice(MemorySegment memory, long offset, MemoryLayout layout) {
		return reslice(memory, offset, layout, 1L);
	}

	/**
	 * Resizes the segment if a native pointer, otherwise slices the segment within bounds.
	 */
	public static MemorySegment reslice(MemorySegment memory, long offset, MemoryLayout layout,
		long count) {
		return reslice(memory, offset, Layouts.size(layout, count));
	}

	/**
	 * Fills the memory with given byte value. Returns the number of bytes filled.
	 */
	public static long fill(MemorySegment m, int value) {
		return fill(m, 0L, value);
	}

	/**
	 * Fills the memory slice with given byte value. Returns the number of bytes filled.
	 */
	public static long fill(MemorySegment m, long offset, int value) {
		return fill(m, offset, Long.MAX_VALUE, value);
	}

	/**
	 * Fills the memory slice with given byte value. Returns the number of bytes filled.
	 */
	public static long fill(MemorySegment m, long offset, long length, int value) {
		var slice = slice(m, offset, length);
		if (size(slice) > 0) slice.fill((byte) value);
		return slice.byteSize();
	}

	/**
	 * Fills the memory slice with given byte value. Returns the number of bytes filled.
	 */
	public static long fill(MemorySegment m, long offset, MemoryLayout layout, long count,
		int value) {
		return fill(m, offset, Layouts.size(layout, count), value);
	}

	/**
	 * Finds the first index of the right memory segment within the left memory segment.
	 */
	public static long indexOf(MemorySegment lm, MemorySegment rm, long step) {
		return indexOf(lm, 0L, rm, 0L, step);
	}

	/**
	 * Finds the first index of the right memory segment within the left memory segment.
	 */
	public static long indexOf(MemorySegment lm, long loff, MemorySegment rm, long roff,
		long step) {
		return indexOf(lm, loff, Long.MAX_VALUE, rm, roff, Long.MAX_VALUE, step);
	}

	/**
	 * Finds the first index of the right memory segment within the left memory segment.
	 */
	public static long indexOf(MemorySegment lm, long loff, long llen, MemorySegment rm, long roff,
		long rlen, long step) {
		return applyBiSlice(lm, loff, llen, rm, roff, rlen, (lo, ll, ro, rl) -> {
			for (long i = 0; i <= ll - rl; i += step)
				if (MemorySegment.mismatch(lm, lo + i, lo + rl + i, rm, ro, ro + rl) == -1)
					return lo + i;
			return -1L;
		});
	}

	/**
	 * Allocates a copy of the memory segment.
	 */
	public static MemorySegment copyOf(SegmentAllocator allocator, MemorySegment memory) {
		return copyOf(allocator, memory, 0L);
	}

	/**
	 * Allocates a copy of the memory segment slice.
	 */
	public static MemorySegment copyOf(SegmentAllocator allocator, MemorySegment memory,
		long offset) {
		return copyOf(allocator, memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Allocates a copy of the memory segment slice.
	 */
	public static MemorySegment copyOf(SegmentAllocator allocator, MemorySegment memory,
		long offset, long length) {
		if (allocator == null || isNull(memory)) return null;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		var copy = allocator.allocate(length, memory.maxByteAlignment());
		MemorySegment.copy(memory, offset, copy, 0L, length);
		return copy;
	}

	/**
	 * Passes a bounded range to the consumer.
	 */
	public static <E extends Exception> void acceptSlice(MemorySegment m, long offset, long length,
		Excepts.LongBiConsumer<E> consumer) throws E {
		if (consumer == null || isNull(m)) return;
		offset = Maths.limit(offset, 0L, m.byteSize());
		length = Maths.limit(length, 0L, m.byteSize() - offset);
		consumer.accept(offset, length);
	}

	/**
	 * Passes a bounded range to the function.
	 */
	public static <E extends Exception, R> R applySlice(MemorySegment m, long offset, long length,
		Excepts.LongBiFunction<E, R> function) throws E {
		if (function == null || isNull(m)) return null;
		offset = Maths.limit(offset, 0L, m.byteSize());
		length = Maths.limit(length, 0L, m.byteSize() - offset);
		return function.apply(offset, length);
	}

	/**
	 * Passes bounded ranges to the consumer.
	 */
	public static <E extends Exception> void acceptBiSlice(MemorySegment lm, long loff, long llen,
		MemorySegment rm, long roff, long rlen, BiSliceConsumer<E> consumer) throws E {
		if (consumer == null || isNull(lm) || isNull(rm)) return;
		loff = Maths.limit(loff, 0L, lm.byteSize());
		llen = Maths.limit(llen, 0L, lm.byteSize() - loff);
		roff = Maths.limit(roff, 0L, rm.byteSize());
		rlen = Maths.limit(rlen, 0L, rm.byteSize() - roff);
		consumer.accept(loff, llen, roff, rlen);
	}

	/**
	 * Passes bounded ranges to the function.
	 */
	public static <E extends Exception, R> R applyBiSlice(MemorySegment lm, long loff, long llen,
		MemorySegment rm, long roff, long rlen, BiSliceFunction<E, R> function) throws E {
		if (function == null || isNull(lm) || isNull(rm)) return null;
		loff = Maths.limit(loff, 0L, lm.byteSize());
		llen = Maths.limit(llen, 0L, lm.byteSize() - loff);
		roff = Maths.limit(roff, 0L, rm.byteSize());
		rlen = Maths.limit(rlen, 0L, rm.byteSize() - roff);
		return function.apply(loff, llen, roff, rlen);
	}
}
