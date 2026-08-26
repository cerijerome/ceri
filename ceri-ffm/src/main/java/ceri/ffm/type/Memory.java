package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.nio.ByteOrder;
import java.util.Objects;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;

/**
 * Support for memory segments.
 */
public class Memory {
	public static final Arena GLOBAL = Arena.global();
	public static final Supporter $ = new Supporter(Layouts.POINTER);

	private Memory() {}

	/**
	 * Consumer for a memory segment.
	 */
	public interface Consumer {
		/** A no-op instance. */
		Consumer NULL = (_, _, _) -> {};
		
		/**
		 * Consumes a memory segment.
		 */
		default void accept(MemorySegment memory) {
			accept(memory, 0L);
		}

		/**
		 * Consumes a memory segment from given offset.
		 */
		default void accept(MemorySegment memory, long offset) {
			accept(memory, offset, Long.MAX_VALUE);
		}

		/**
		 * Consumes a memory segment from offset up to given length.
		 */
		void accept(MemorySegment memory, long offset, long length);
	}

	/**
	 * Synchronizes memory and value contents, returning the resulting value. Can be used for
	 * synchronizing immutable value instances.
	 */
	public interface Updater<T> {
		/** A no-op instance. */
		Updater<?> NULL = (_, _, _, t) -> t;
		
		/**
		 * Consumes a memory segment.
		 */
		default T apply(MemorySegment memory, T t) {
			return apply(memory, 0L, t);
		}

		/**
		 * Consumes a memory segment from given offset.
		 */
		default T apply(MemorySegment memory, long offset, T t) {
			return apply(memory, offset, Long.MAX_VALUE, t);
		}

		/**
		 * Applies a memory segment from offset up to given length to a value.
		 */
		T apply(MemorySegment memory, long offset, long length, T t);
		
		/**
		 * Returns a no-op instance.
		 */
		static <T> Updater<T> ofNull() {
			return Reflect.unchecked(NULL);
		}
	}

	/**
	 * Synchronizes memory and value contents. Can be used for reading value contents from memory,
	 * and writing value contents to memory.
	 */
	public interface Sync<T> {
		/** A no-op instance. */
		Sync<?> NULL = (_, _, _, _) -> {};
		
		/**
		 * Synchronizes a memory segment slice and value contents.
		 */
		default void accept(MemorySegment memory, T t) {
			accept(memory, 0L, t);
		}

		/**
		 * Synchronizes a memory segment slice and value contents.
		 */
		default void accept(MemorySegment memory, long offset, T t) {
			accept(memory, offset, Long.MAX_VALUE, t);
		}

		/**
		 * Synchronizes a memory segment slice and value contents.
		 */
		void accept(MemorySegment memory, long offset, long length, T t);
		
		/**
		 * Returns a no-op instance.
		 */
		static <T> Sync<T> ofNull() {
			return Reflect.unchecked(NULL);
		}
	}

	/**
	 * Operational support for segments.
	 */
	public static final class Supporter extends Support.Typed<MemorySegment, AddressLayout> {

		Supporter(AddressLayout layout) {
			super(layout);
		}

		@Override
		public Native.Kind kind() {
			return Native.Kind.MEMORY;
		}

		@Override
		public Class<MemorySegment> type() {
			return MemorySegment.class;
		}

		@Override
		public MemorySegment val() {
			return MemorySegment.NULL;
		}

		@Override
		public Supporter align(long align) {
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new Supporter(layout);
		}

		@Override
		public Supporter order(ByteOrder order) {
			var layout = Layouts.order(layout(), order);
			return layout == layout() ? this : new Supporter(layout);
		}

		@Override
		MemorySegment rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, MemorySegment value) {
			memory.set(layout(), offset, value);
		}
	}

	/**
	 * Returns a new auto arena.
	 */
	public static SegmentAllocator auto() {
		return Arena.ofAuto();
	}

	/**
	 * Returns a hash for the segment and its size.
	 */
	public static int hash(MemorySegment memory) {
		if (memory == null) return Objects.hash(memory);
		return Objects.hash(memory, memory.byteSize());
	}

	/**
	 * Returns true if the segments have the same location and size.
	 */
	public static boolean equals(MemorySegment m1, MemorySegment m2) {
		if (m1 == m2) return true;
		return Objects.equals(m1, m2) && m1.byteSize() == m2.byteSize();
	}

	/**
	 * Provides an alternative string descriptor.
	 */
	public static String string(MemorySegment memory) {
		if (memory == null) return Strings.NULL;
		return String.format("%s+%02x", addressString(memory), size(memory));
	}

	/**
	 * Provides an alternative string descriptor for the address.
	 */
	public static String addressString(MemorySegment memory) {
		return addressString(memory, 0L);
	}

	/**
	 * Provides an alternative string descriptor for the address with offset.
	 */
	public static String addressString(MemorySegment memory, long offset) {
		if (memory == null) return Strings.NULL;
		offset = Math.max(0L, offset);
		var heapBase = memory.heapBase().orElse(null);
		if (heapBase == null) return "@" + Long.toHexString(memory.address() + offset);
		return String.format("#%x:%02x", System.identityHashCode(heapBase),
			memory.address() + offset);
	}

	/**
	 * Returns the address of the segment; 0 if null.
	 */
	public static long address(MemorySegment memory) {
		return memory == null ? 0L : memory.address();
	}

	/**
	 * Returns true if the segment is null or a native null pointer.
	 */
	public static boolean isNull(MemorySegment memory) {
		return memory == null || (memory.isNative() && memory.address() == 0L);
	}

	/**
	 * Returns true if the segment is non-null and native.
	 */
	public static boolean isNative(MemorySegment memory) {
		return memory != null && memory.isNative();
	}

	/**
	 * Returns true if the segment scope is alive.
	 */
	public static boolean isAlive(MemorySegment memory) {
		return memory != null && memory.scope().isAlive();
	}

	/**
	 * Returns the byte size of the segment, or 0 if null.
	 */
	public static long size(MemorySegment memory) {
		return memory == null ? 0L : memory.byteSize();
	}

	/**
	 * Returns the byte size of the segment, or 0 if null. Fails if larger than int.
	 */
	public static int sizeInt(MemorySegment memory) {
		return Math.toIntExact(size(memory));
	}

	/**
	 * Returns the offset bounded by segment size, or 0 if null.
	 */
	public static long limit(MemorySegment memory, long offset) {
		return memory == null ? 0L : Maths.limit(offset, 0L, memory.byteSize());
	}

	/**
	 * Returns the offset bounded by segment size, or 0 if null. Fails if larger than int.
	 */
	public static int limitInt(MemorySegment memory, long offset) {
		return Math.toIntExact(limit(memory, offset));
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
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, long offset) {
		return slice(memory, offset, Long.MAX_VALUE);
	}

	/**
	 * Returns a view of the memory segment.
	 */
	public static MemorySegment slice(MemorySegment memory, long offset, long length) {
		return slice(memory, offset, length, 1L);
	}

	/**
	 * Returns a view of the memory segment, failing if the alignment constraint is not met.
	 */
	public static MemorySegment slice(MemorySegment memory, long offset, long length, long align) {
		if (isNull(memory)) return memory;
		offset = Maths.limit(offset, 0L, memory.byteSize());
		length = Maths.limit(length, 0L, memory.byteSize() - offset);
		Layouts.validateAlignment(offset, align);
		if (offset == 0L && length == memory.byteSize()) return memory;
		return memory.asSlice(offset, length);
	}

	/**
	 * Returns a view of the memory segment based on layout length, failing if the alignment
	 * constraint is not met.
	 */
	public static MemorySegment slice(MemorySegment memory, MemoryLayout layout) {
		return slice(memory, 0L, layout, 1L);
	}

	/**
	 * Returns a view of the memory segment based on layout index offset and length, failing if the
	 * alignment constraint is not met.
	 */
	public static MemorySegment slice(MemorySegment memory, long index, MemoryLayout layout,
		long count) {
		return slice(memory, Layouts.size(layout, index), Layouts.size(layout, count),
			Layouts.align(layout));
	}

	/**
	 * Resizes the segment if native, otherwise slices the segment within bounds.
	 */
	public static MemorySegment resize(MemorySegment memory, long offset, long length) {
		return resize(memory, offset, length, 1L);
	}

	/**
	 * Resizes the segment if native, otherwise slices the segment within bounds. Fails if the
	 * alignment constraint is not met.
	 */
	public static MemorySegment resize(MemorySegment memory, long offset, long length, long align) {
		if (isNull(memory)) return memory;
		if (!isNative(memory)) return slice(memory, offset, length, align);
		offset = Math.max(0L, offset);
		length = Math.max(0L, length);
		if (offset + length > memory.byteSize()) memory = memory.reinterpret(offset + length);
		return slice(memory, offset, length, align);
	}

	/**
	 * Resizes the segment if native, otherwise slices the segment within bounds. Based on layout
	 * index offset and length. Fails if the alignment constraint is not met.
	 */
	public static MemorySegment resize(MemorySegment memory, MemoryLayout layout) {
		return resize(memory, 0L, layout, 1L);
	}

	/**
	 * Resizes the segment if native, otherwise slices the segment within bounds. Based on layout
	 * index offset and length. Fails if the alignment constraint is not met.
	 */
	public static MemorySegment resize(MemorySegment memory, long index, MemoryLayout layout,
		long count) {
		return resize(memory, Layouts.size(layout, index), Layouts.size(layout, count),
			Layouts.align(layout));
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
}
