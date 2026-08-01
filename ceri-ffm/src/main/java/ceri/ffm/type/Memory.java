package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import ceri.common.math.Maths;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;

/**
 * Support for memory segments.
 */
public class Memory {
	public static final Supporter $ = new Supporter(Layouts.POINTER);

	private Memory() {}
	
	/**
	 * Operational support for segments.
	 */
	public static class Supporter extends Support.Typed<MemorySegment, AddressLayout> {

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
	 * Returns true if the segment is null or a native null pointer.
	 */
	public static boolean isNull(MemorySegment memory) {
		return memory == null || (memory.isNative() && memory.address() == 0L);
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
	
}
