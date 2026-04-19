package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Map;
import ceri.common.array.Array;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
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
	private static final Map<Class<?>, Support<?, ?, ? extends ValueLayout>> MAP = map();

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
		public Box<T, L> with(String name, long align, ByteOrder order) {
			var primitive = this.primitive.with(name, align, order);
			return primitive == this.primitive ? this : new Box<>(primitive);
		}

		@Override
		public T val() {
			return primitive.val();
		}

		@Override
		T rawGet(MemorySegment memory, long offset) {
			return primitive.rawGet(memory, offset);
		}

		@Override
		void rawWrite(T value, MemorySegment memory, long offset) {
			primitive.rawWrite(value, memory, offset);
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
		public OfBool with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfBool::new, layout(), name, align, order);
		}

		@Override
		public Boolean val() {
			return Boolean.FALSE;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		@Override
		Boolean rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Boolean value, MemorySegment memory, long offset) {
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
		void rawWriteArray(boolean[] array, int index, MemorySegment memory, long offset,
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
		public OfChar with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfChar::new, layout(), name, align, order);
		}

		@Override
		public Character val() {
			return VAL;
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
		 * Wraps the values as a memory segment.
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		// overrides

		@Override
		Character rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Character value, MemorySegment memory, long offset) {
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
		public OfByte with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfByte::new, layout(), name, align, order);
		}

		@Override
		public Byte val() {
			return VAL;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
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
		Byte rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Byte value, MemorySegment memory, long offset) {
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
		public OfShort with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfShort::new, layout(), name, align, order);
		}

		@Override
		public Short val() {
			return VAL;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
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
		Short rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Short value, MemorySegment memory, long offset) {
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
		public OfInt with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfInt::new, layout(), name, align, order);
		}

		@Override
		public Integer val() {
			return VAL;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		// overrides

		@Override
		Integer rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Integer value, MemorySegment memory, long offset) {
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
		public OfLong with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfLong::new, layout(), name, align, order);
		}

		@Override
		public Long val() {
			return VAL;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		// overrides

		@Override
		Long rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Long value, MemorySegment memory, long offset) {
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
		public OfFloat with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfFloat::new, layout(), name, align, order);
		}

		@Override
		public Float val() {
			return VAL;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		// overrides

		@Override
		Float rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Float value, MemorySegment memory, long offset) {
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
		public OfDouble with(String name, long align, ByteOrder order) {
			return Layouts.with(this, OfDouble::new, layout(), name, align, order);
		}

		@Override
		public Double val() {
			return VAL;
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
			return writeArray(array, 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		// overrides

		@Override
		Double rawGet(MemorySegment memory, long offset) {
			return memory.get(layout(), offset);
		}

		@Override
		void rawWrite(Double value, MemorySegment memory, long offset) {
			memory.set(layout(), offset, value);
		}

		@Override
		MemorySegment rawWrapArray(double[] array) {
			return MemorySegment.ofArray(array);
		}
	}

	public static <T> Support<T, ?, ? extends ValueLayout> of(Class<?> cls) {
		return Reflect.unchecked(MAP.get(cls));
	}

	public static ValueLayout layout(Class<?> cls) {
		var support = of(cls);
		return support == null ? null : support.layout();
	}

	private Primitive(L layout) {
		super(layout);
	}

	public abstract Class<T> boxType();

	@Override
	public abstract Primitive<T, A, L> with(String name, long align, ByteOrder order);

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

	@Override
	public Object deepInit(Object t) {
		return t; // no nulls to replace
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
	void rawWriteArray(A array, int index, MemorySegment memory, long offset, int count) {
		MemorySegment.copy(array, index, memory, layout(), offset, count);
	}

	abstract MemorySegment rawWrapArray(A array);

	@Override
	int dimsOf(Object t) {
		if (boxType().isInstance(t)) return 0;
		return super.dimsOf(t);
	}

	// support

	private static byte[] bytes(boolean... bools) {
		if (bools == null) return null;
		var bytes = new byte[bools.length];
		for (int i = 0; i < bools.length; i++)
			if (bools[i]) bytes[i] = 1;
		return bytes;
	}

	private static Map<Class<?>, Support<?, ?, ? extends ValueLayout>> map() {
		return Immutable.convertMapOf(Support::type, t -> t, BOOL, CHAR, BYTE, SHORT, INT, LONG,
			FLOAT, DOUBLE, Box.BOOL, Box.CHAR, Box.BYTE, Box.SHORT, Box.INT, Box.LONG, Box.FLOAT,
			Box.DOUBLE);
	}
}
