package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import ceri.common.array.Array;
import ceri.common.array.RawArray;
import ceri.common.math.Maths;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;

/**
 * Operational support for primitives.
 */
public abstract class Primitive<T, A, L extends ValueLayout> extends Support<T, A, L> {
	public static final OfBool BOOL = new OfBool(Layouts.BOOL);
	public static final OfChar CHAR = new OfChar(Layouts.CHAR);
	public static final OfByte BYTE = new OfByte(Layouts.BYTE);
	public static final OfShort SHORT = new OfShort(Layouts.SHORT);
	public static final OfInt INT = new OfInt(Layouts.INT);
	public static final OfLong LONG = new OfLong(Layouts.LONG);
	public static final OfFloat FLOAT = new OfFloat(Layouts.FLOAT);
	public static final OfDouble DOUBLE = new OfDouble(Layouts.DOUBLE);

	/**
	 * Operational support for boxed primitives.
	 */
	public static class Box<T, L extends ValueLayout> extends Support.Typed<T, L> {
		public static final Box<Boolean, ValueLayout.OfBoolean> BOOL = new Box<>(Primitive.BOOL);
		public static final Box<Character, ValueLayout.OfChar> CHAR = new Box<>(Primitive.CHAR);
		public static final Box<Byte, ValueLayout.OfByte> BYTE = new Box<>(Primitive.BYTE);
		public static final Box<Short, ValueLayout.OfShort> SHORT = new Box<>(Primitive.SHORT);
		public static final Box<Integer, ValueLayout.OfInt> INT = new Box<>(Primitive.INT);
		public static final Box<Long, ValueLayout.OfLong> LONG = new Box<>(Primitive.LONG);
		public static final Box<Float, ValueLayout.OfFloat> FLOAT = new Box<>(Primitive.FLOAT);
		public static final Box<Double, ValueLayout.OfDouble> DOUBLE = new Box<>(Primitive.DOUBLE);
		private final Primitive<T, ?, L> primitive;

		private Box(Primitive<T, ?, L> primitive) {
			super(primitive.layout());
			this.primitive = primitive;
		}

		@Override
		public Class<T> type() {
			return primitive.boxType();
		}

		@Override
		public Box<T, L> with(long align, ByteOrder order) {
			var primitive = this.primitive.with(align, order);
			return primitive == this.primitive ? this : new Box<>(primitive);
		}

		@Override
		public T val() {
			return primitive.val();
		}

		@Override
		T rawGet(MemorySegment memory, long offset, long length) {
			return primitive.rawGet(memory, offset, length);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, T value) {
			primitive.rawWrite(memory, offset, length, value);
		}
	}

	/**
	 * Added bool support.
	 */
	public static class OfBool extends Primitive<Boolean, boolean[], ValueLayout.OfBoolean> {

		private OfBool(ValueLayout.OfBoolean layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return boolean.class;
		}

		@Override
		public Class<Boolean> boxType() {
			return Boolean.class;
		}

		@Override
		public Class<boolean[]> arrayType() {
			return boolean[].class;
		}

		@Override
		public OfBool with(long align, ByteOrder order) {
			return Layouts.with(this, OfBool::new, layout(), null, align, order);
		}

		@Override
		public Boolean val() {
			return Boolean.FALSE;
		}

		/**
		 * Gets the value at the offset.
		 */
		public boolean getBool(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setBool(MemorySegment memory, long offset, boolean value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocBool(boolean value) {
			return allocBool(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocBool(SegmentAllocator allocator, boolean value) {
			if (allocator == null) return null;
			var memory = allocator.allocate(layout());
			memory.set(layout(), 0, value);
			return memory;
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, boolean... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, boolean... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, boolean... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul,
			boolean... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			boolean... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		@Override
		Boolean rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Boolean value) {
			memory.set(layout(), offset, value);
		}

		@Override
		void rawReadArray(MemorySegment memory, long offset, boolean[] array, int index,
			int count) {
			for (int i = 0; i < count; i++) {
				array[index++] = memory.get(layout(), offset);
				offset += layout().byteSize();
			}
		}

		@Override
		void rawWriteArray(MemorySegment memory, long offset, boolean[] array, int index,
			int count) {
			for (int i = 0; i < count; i++) {
				memory.set(layout(), offset, array[i++]);
				offset += layout().byteSize();
			}
		}

		@Override
		MemorySegment rawWrapArray(boolean[] array) {
			// Converts to bytes, then wraps
			return MemorySegment.ofArray(bytes(array));
		}
	}

	/**
	 * Added char support.
	 */
	public static class OfChar extends Primitive<Character, char[], ValueLayout.OfChar> {
		private static final Character VAL = '\0';

		private OfChar(ValueLayout.OfChar layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return char.class;
		}

		@Override
		public Class<Character> boxType() {
			return Character.class;
		}

		@Override
		public Class<char[]> arrayType() {
			return char[].class;
		}

		@Override
		public OfChar with(long align, ByteOrder order) {
			return Layouts.with(this, OfChar::new, layout(), null, align, order);
		}

		@Override
		public Character val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public char getChar(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.charValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setChar(MemorySegment memory, long offset, char value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocChar(char value) {
			return allocChar(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocChar(SegmentAllocator allocator, char value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, char... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, char... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(char... array) {
			return wrapArray(array, 0);
		}

		/**
		 * Wraps the value char array as a memory segment.
		 */
		public MemorySegment wrap(CharSequence s) {
			if (s == null) return null;
			return wrapArray(s.toString().toCharArray());
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, char... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, char... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			char... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		// overrides

		@Override
		Character rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Character value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(char[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	/**
	 * Added byte support.
	 */
	public static class OfByte extends Primitive<Byte, byte[], ValueLayout.OfByte> {
		private static final Byte VAL = 0;

		private OfByte(ValueLayout.OfByte layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return byte.class;
		}

		@Override
		public Class<Byte> boxType() {
			return Byte.class;
		}

		@Override
		public Class<byte[]> arrayType() {
			return byte[].class;
		}

		@Override
		public OfByte with(long align, ByteOrder order) {
			return Layouts.with(this, OfByte::new, layout(), null, align, order);
		}

		/**
		 * Provides support for pointers of this type.
		 */
		public RawPointer.Supporter<Pointer.OfByte> asPointer(boolean constant) {
			return Pointer.OfByte.support(this, constant);
		}

		@Override
		public Byte val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public byte getByte(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.byteValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setByte(MemorySegment memory, long offset, byte value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocByte(byte value) {
			return allocByte(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocByte(SegmentAllocator allocator, byte value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, byte... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, int... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, byte... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, int... array) {
			return allocArray(allocator, Array.BYTE.of(array), nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(byte... array) {
			return wrapArray(array);
		}

		/**
		 * Wraps the converted values as a memory segment.
		 */
		public MemorySegment wrapAll(int... array) {
			return wrapArray(Array.BYTE.of(array));
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, byte... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, byte... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			byte... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, int... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, int... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			int... array) {
			return writeAll(memory, offset, length, nul, Array.BYTE.of(array));
		}

		// overrides

		@Override
		Byte rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Byte value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(byte[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	/**
	 * Added short support.
	 */
	public static class OfShort extends Primitive<Short, short[], ValueLayout.OfShort> {
		private static final Short VAL = 0;

		private OfShort(ValueLayout.OfShort layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return short.class;
		}

		@Override
		public Class<Short> boxType() {
			return Short.class;
		}

		@Override
		public Class<short[]> arrayType() {
			return short[].class;
		}

		@Override
		public OfShort with(long align, ByteOrder order) {
			return Layouts.with(this, OfShort::new, layout(), null, align, order);
		}

		@Override
		public Short val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public short getShort(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.shortValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setShort(MemorySegment memory, long offset, short value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocShort(short value) {
			return allocShort(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocShort(SegmentAllocator allocator, short value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, short... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, int... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, short... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, int... array) {
			return allocArray(allocator, Array.SHORT.of(array), nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(short... array) {
			return wrapArray(array);
		}

		/**
		 * Wraps the converted values as a memory segment.
		 */
		public MemorySegment wrapAll(int... array) {
			return wrapArray(Array.SHORT.of(array));
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, short... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, short... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			short... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, int... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, int... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			int... array) {
			return writeAll(memory, offset, length, nul, Array.SHORT.of(array));
		}

		// overrides

		@Override
		Short rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Short value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(short[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	/**
	 * Added int support.
	 */
	public static class OfInt extends Primitive<Integer, int[], ValueLayout.OfInt> {
		private static final Integer VAL = 0;

		private OfInt(ValueLayout.OfInt layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return int.class;
		}

		@Override
		public Class<Integer> boxType() {
			return Integer.class;
		}

		@Override
		public Class<int[]> arrayType() {
			return int[].class;
		}

		@Override
		public OfInt with(long align, ByteOrder order) {
			return Layouts.with(this, OfInt::new, layout(), null, align, order);
		}

		/**
		 * Provides support for pointers of this type.
		 */
		public RawPointer.Supporter<Pointer.OfInt> asPointer(boolean constant) {
			return Pointer.OfInt.support(this, constant);
		}

		@Override
		public Integer val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public int getInt(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.intValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setInt(MemorySegment memory, long offset, int value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocInt(int value) {
			return allocInt(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocInt(SegmentAllocator allocator, int value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, int... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, int... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(int... array) {
			return wrapArray(array, 0);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, int... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, int... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			int... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		// overrides

		@Override
		Integer rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Integer value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(int[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	/**
	 * Added long support.
	 */
	public static class OfLong extends Primitive<Long, long[], ValueLayout.OfLong> {
		private static final Long VAL = 0L;

		private OfLong(ValueLayout.OfLong layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return long.class;
		}

		@Override
		public Class<Long> boxType() {
			return Long.class;
		}

		@Override
		public Class<long[]> arrayType() {
			return long[].class;
		}

		@Override
		public OfLong with(long align, ByteOrder order) {
			return Layouts.with(this, OfLong::new, layout(), null, align, order);
		}

		@Override
		public Long val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public long getLong(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.longValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setLong(MemorySegment memory, long offset, long value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocLong(long value) {
			return allocLong(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocLong(SegmentAllocator allocator, long value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, long... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, long... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(long... array) {
			return wrapArray(array, 0);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, long... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, long... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			long... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		// overrides

		@Override
		Long rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Long value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(long[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	/**
	 * Added float support.
	 */
	public static class OfFloat extends Primitive<Float, float[], ValueLayout.OfFloat> {
		private static final Float VAL = 0f;

		private OfFloat(ValueLayout.OfFloat layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return float.class;
		}

		@Override
		public Class<Float> boxType() {
			return Float.class;
		}

		@Override
		public Class<float[]> arrayType() {
			return float[].class;
		}

		@Override
		public OfFloat with(long align, ByteOrder order) {
			return Layouts.with(this, OfFloat::new, layout(), null, align, order);
		}

		@Override
		public Float val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public float getFloat(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.floatValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setFloat(MemorySegment memory, long offset, float value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocFloat(float value) {
			return allocFloat(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocFloat(SegmentAllocator allocator, float value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, float... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, float... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(float... array) {
			return wrapArray(array, 0);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, float... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, float... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			float... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		// overrides

		@Override
		Float rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Float value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(float[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	/**
	 * Added double support.
	 */
	public static class OfDouble extends Primitive<Double, double[], ValueLayout.OfDouble> {
		private static final Double VAL = 0.0;

		private OfDouble(ValueLayout.OfDouble layout) {
			super(layout);
		}

		@Override
		public Class<?> type() {
			return double.class;
		}

		@Override
		public Class<Double> boxType() {
			return Double.class;
		}

		@Override
		public Class<double[]> arrayType() {
			return double[].class;
		}

		@Override
		public OfDouble with(long align, ByteOrder order) {
			return Layouts.with(this, OfDouble::new, layout(), null, align, order);
		}

		@Override
		public Double val() {
			return VAL;
		}

		/**
		 * Gets the value at the offset.
		 */
		public double getDouble(MemorySegment memory, long offset) {
			if (!Segments.within(memory, offset, layoutSize())) return VAL.doubleValue();
			return memory.get(layout(), offset);
		}

		/**
		 * Sets the value at the offset.
		 */
		public boolean setDouble(MemorySegment memory, long offset, double value) {
			if (!Segments.within(memory, offset, layoutSize())) return false;
			memory.set(layout(), offset, value);
			return true;
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocDouble(double value) {
			return allocDouble(Segments.auto(), value);
		}

		/**
		 * Allocates memory with the value.
		 */
		public MemorySegment allocDouble(SegmentAllocator allocator, double value) {
			if (allocator == null) return null;
			return allocator.allocateFrom(layout(), value);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(boolean nul, double... array) {
			return allocAll(Segments.auto(), nul, array);
		}

		/**
		 * Allocates memory and copies the values with optional nul-termination.
		 */
		public MemorySegment allocAll(SegmentAllocator allocator, boolean nul, double... array) {
			return allocArray(allocator, array, nul);
		}

		/**
		 * Wraps the values as a memory segment.
		 */
		public MemorySegment wrapAll(double... array) {
			return wrapArray(array, 0);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, boolean nul, double... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, boolean nul, double... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory with optional nul-termination, within bounds; returns the number
		 * of values copied, including nul-terminator.
		 */
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			double... array) {
			return writeArray(memory, offset, length, array, 0, Integer.MAX_VALUE, nul);
		}

		// overrides

		@Override
		Double rawGet(MemorySegment memory, long offset, long length) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, Double value) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(double[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	private Primitive(L layout) {
		super(layout);
	}

	/**
	 * Returns the boxed class.
	 */
	public abstract Class<T> boxType();

	@Override
	public abstract Primitive<T, A, L> with(long align, ByteOrder order);

	/**
	 * Wraps the bounded array as a memory segment.
	 */
	public MemorySegment wrapArray(A array) {
		return wrapArray(array, 0);
	}

	/**
	 * Wraps the bounded array as a memory segment.
	 */
	public MemorySegment wrapArray(A array, int index) {
		return wrapArray(array, index, Integer.MAX_VALUE);
	}

	/**
	 * Wraps the bounded array as a memory segment.
	 */
	public MemorySegment wrapArray(A array, int index, int length) {
		if (array == null) return null;
		index = Maths.limit(index, 0, RawArray.length(array));
		length = Maths.limit(length, 0, RawArray.length(array) - index);
		return Segments.slice(rawWrapArray(array), size(index), size(length));
	}

	// overrides

	@Override
	void rawReadArray(MemorySegment memory, long offset, A array, int index, int count) {
		MemorySegment.copy(memory, layout(), offset, array, index, count);
	}

	@Override
	int rawReadArrayNew(MemorySegment memory, long offset, A array) {
		return readArray(memory, offset, array, 0, false);
	}

	@Override
	void rawWriteArray(MemorySegment memory, long offset, A array, int index, int count) {
		MemorySegment.copy(array, index, memory, layout(), offset, count);
	}

	abstract MemorySegment rawWrapArray(A array);

	// support

	private static byte[] bytes(boolean... bools) {
		if (bools == null) return null;
		var bytes = new byte[bools.length];
		for (int i = 0; i < bools.length; i++)
			if (bools[i]) bytes[i] = 1;
		return bytes;
	}
}
