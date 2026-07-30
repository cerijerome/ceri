package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import com.google.common.base.Objects;
import ceri.common.array.Array;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;

public class Pointer<T> extends PointerType.Indexable<Pointer<T>, Support.Typed<T, ?>, T[]> {
	/** Wildcard pointer support. */
	public static final Supporter<Pointer<?>> $ = Reflect.unchecked(support(Support.VOID, true));

	/**
	 * Constant void pointer.
	 */
	public static class OfVoid extends PointerType.Raw {
		public static final Supporter<OfVoid> $ = Supporter.of(OfVoid.class,
			Native.Kind.PRIMITIVE_POINTER, Support.VOID, (m, _, _) -> new OfVoid(m), true);

		private OfVoid(MemorySegment memory) {
			super(memory);
		}

		/**
		 * Casts to a typed pointer.
		 */
		public Pointer<?> typed() {
			return as(Support.VOID);
		}

		@Override
		public Pointer.OfVoid asVoid() {
			return this;
		}

		@Override
		Supporter<OfVoid> support() {
			return $;
		}
	}

	/**
	 * Primitive boolean pointer.
	 */
	public static class OfBool extends PointerType.Indexable<OfBool, Primitive.OfBool, boolean[]> {
		public static final Supporter<OfBool> $ = support(Primitive.BOOL, false);

		static Supporter<OfBool> support(Primitive.OfBool type, boolean constant) {
			return Supporter.of(OfBool.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfBool(m, t, c), constant);
		}

		OfBool(MemorySegment memory, Primitive.OfBool type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfBool> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfBool asBool() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public boolean get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public boolean getAt(int index) {
			return type().getBool(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(boolean value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, boolean value) {
			if (isConst()) return false;
			return type().setBool(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, boolean... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, boolean... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfBool instance(MemorySegment memory, Primitive.OfBool type, boolean constant) {
			return new OfBool(memory, type, constant);
		}
	}

	/**
	 * Primitive char pointer.
	 */
	public static class OfChar extends PointerType.Indexable<OfChar, Primitive.OfChar, char[]> {
		public static final Supporter<OfChar> $ = support(Primitive.CHAR, false);

		static Supporter<OfChar> support(Primitive.OfChar type, boolean constant) {
			return Supporter.of(OfChar.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfChar(m, t, c), constant);
		}

		OfChar(MemorySegment memory, Primitive.OfChar type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfChar> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfChar asChar() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public char get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public char getAt(int index) {
			return type().getChar(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(char value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, char value) {
			if (isConst()) return false;
			return type().setChar(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, char... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, char... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfChar instance(MemorySegment memory, Primitive.OfChar type, boolean constant) {
			return new OfChar(memory, type, constant);
		}
	}

	/**
	 * Primitive byte pointer.
	 */
	public static class OfByte extends PointerType.Indexable<OfByte, Primitive.OfByte, byte[]> {
		public static final Supporter<OfByte> $ = support(Primitive.BYTE, false);

		static Supporter<OfByte> support(Primitive.OfByte type, boolean constant) {
			return Supporter.of(OfByte.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfByte(m, t, c), constant);
		}

		OfByte(MemorySegment memory, Primitive.OfByte type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfByte> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfByte asByte() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public byte get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public byte getAt(int index) {
			return type().getByte(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(int value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, int value) {
			if (isConst()) return false;
			return type().setByte(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, byte... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, int... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, byte... array) {
			return setArrayAt(index, array, nul);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, int... array) {
			return setArrayAt(index, Array.BYTE.of(array), nul);
		}

		@Override
		OfByte instance(MemorySegment memory, Primitive.OfByte type, boolean constant) {
			return new OfByte(memory, type, constant);
		}
	}

	/**
	 * Primitive short pointer.
	 */
	public static class OfShort extends PointerType.Indexable<OfShort, Primitive.OfShort, short[]> {
		public static final Supporter<OfShort> $ = support(Primitive.SHORT, false);

		static Supporter<OfShort> support(Primitive.OfShort type, boolean constant) {
			return Supporter.of(OfShort.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfShort(m, t, c), constant);
		}

		OfShort(MemorySegment memory, Primitive.OfShort type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfShort> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfShort asShort() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public short get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public short getAt(int index) {
			return type().getShort(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(int value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, int value) {
			if (isConst()) return false;
			return type().setShort(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, short... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, int... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, short... array) {
			return setArrayAt(index, array, nul);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, int... array) {
			return setArrayAt(index, Array.SHORT.of(array), nul);
		}

		@Override
		OfShort instance(MemorySegment memory, Primitive.OfShort type, boolean constant) {
			return new OfShort(memory, type, constant);
		}
	}

	/**
	 * Primitive int pointer.
	 */
	public static class OfInt extends PointerType.Indexable<OfInt, Primitive.OfInt, int[]> {
		public static final Supporter<OfInt> $ = support(Primitive.INT, false);

		static Supporter<OfInt> support(Primitive.OfInt type, boolean constant) {
			return Supporter.of(OfInt.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfInt(m, t, c), constant);
		}

		public static OfInt of(MemorySegment memory, Primitive.OfInt type, boolean constant) {
			if (memory == null || type == null) return null;
			return new OfInt(memory, type, constant);
		}

		OfInt(MemorySegment memory, Primitive.OfInt type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfInt> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfInt asInt() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public int get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public int getAt(int index) {
			return type().getInt(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(int value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, int value) {
			if (isConst()) return false;
			return type().setInt(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, int... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, int... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfInt instance(MemorySegment memory, Primitive.OfInt type, boolean constant) {
			return new OfInt(memory, type, constant);
		}
	}

	/**
	 * Primitive long pointer.
	 */
	public static class OfLong extends PointerType.Indexable<OfLong, Primitive.OfLong, long[]> {
		public static final Supporter<OfLong> $ = support(Primitive.LONG, false);

		static Supporter<OfLong> support(Primitive.OfLong type, boolean constant) {
			return Supporter.of(OfLong.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfLong(m, t, c), constant);
		}

		public static OfLong of(MemorySegment memory, Primitive.OfLong type, boolean constant) {
			if (memory == null || type == null) return null;
			return new OfLong(memory, type, constant);
		}

		OfLong(MemorySegment memory, Primitive.OfLong type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfLong> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfLong asLong() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public long get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public long getAt(int index) {
			return type().getLong(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(long value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, long value) {
			if (isConst()) return false;
			return type().setLong(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, long... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, long... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfLong instance(MemorySegment memory, Primitive.OfLong type, boolean constant) {
			return new OfLong(memory, type, constant);
		}
	}

	/**
	 * Primitive float pointer.
	 */
	public static class OfFloat
		extends PointerType.Indexable<OfFloat, Primitive.OfFloat, float[]> {
		public static final Supporter<OfFloat> $ = support(Primitive.FLOAT, false);

		static Supporter<OfFloat> support(Primitive.OfFloat type, boolean constant) {
			return Supporter.of(OfFloat.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfFloat(m, t, c), constant);
		}

		public static OfFloat of(MemorySegment memory, Primitive.OfFloat type, boolean constant) {
			if (memory == null || type == null) return null;
			return new OfFloat(memory, type, constant);
		}

		OfFloat(MemorySegment memory, Primitive.OfFloat type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfFloat> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfFloat asFloat() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public float get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public float getAt(int index) {
			return type().getFloat(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(float value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, float value) {
			if (isConst()) return false;
			return type().setFloat(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, float... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, float... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfFloat instance(MemorySegment memory, Primitive.OfFloat type, boolean constant) {
			return new OfFloat(memory, type, constant);
		}
	}

	/**
	 * Primitive double pointer.
	 */
	public static class OfDouble
		extends PointerType.Indexable<OfDouble, Primitive.OfDouble, double[]> {
		public static final Supporter<OfDouble> $ = support(Primitive.DOUBLE, false);

		static Supporter<OfDouble> support(Primitive.OfDouble type, boolean constant) {
			return Supporter.of(OfDouble.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfDouble(m, t, c), constant);
		}

		public static OfDouble of(MemorySegment memory, Primitive.OfDouble type, boolean constant) {
			if (memory == null || type == null) return null;
			return new OfDouble(memory, type, constant);
		}

		OfDouble(MemorySegment memory, Primitive.OfDouble type, boolean constant) {
			super(memory, type, constant);
		}

		@Override
		public Supporter<OfDouble> support() {
			return support(type(), isConst());
		}

		@Override
		public Pointer.OfDouble asDouble() {
			return this;
		}

		/**
		 * Gets the primitive value at the pointer.
		 */
		public double get() {
			return getAt(0);
		}

		/**
		 * Gets the primitive value at the pointer type index.
		 */
		public double getAt(int index) {
			return type().getDouble(memory(), size(index));
		}

		/**
		 * Sets the primitive value at the pointer. Returns false if constant or out of range.
		 */
		public boolean set(double value) {
			return setAt(0, value);
		}

		/**
		 * Sets the primitive value at the pointer type index. Returns false if constant or out of
		 * range.
		 */
		public boolean setAt(int index, double value) {
			if (isConst()) return false;
			return type().setDouble(memory(), size(index), value);
		}

		/**
		 * Sets primitive values at the pointer with optional nul-termination. Returns the number of
		 * values set.
		 */
		public final int setAll(boolean nul, double... array) {
			return setAllAt(0, nul, array);
		}

		/**
		 * Sets primitive values at the pointer type index with optional nul-termination. Returns
		 * the number of values set.
		 */
		public final int setAllAt(int index, boolean nul, double... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfDouble instance(MemorySegment memory, Primitive.OfDouble type, boolean constant) {
			return new OfDouble(memory, type, constant);
		}
	}

	static <T> Supporter<Pointer<T>> support(Support.Typed<T, ?> type, boolean constant) {
		return Supporter.of(Reflect.unchecked(Pointer.class), Native.Kind.POINTER, type,
			(m, s, _) -> of(m, s), constant);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfBool ofBool(MemorySegment memory) {
		return Primitive.BOOL.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfBool ofBool(boolean value) {
		return ofBool(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfBool ofBool(SegmentAllocator allocator, boolean value) {
		return Primitive.BOOL.pointerOfBool(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfChar ofChar(MemorySegment memory) {
		return Primitive.CHAR.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfChar ofChar(char value) {
		return ofChar(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfChar ofChar(SegmentAllocator allocator, char value) {
		return Primitive.CHAR.pointerOfChar(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfByte ofByte(MemorySegment memory) {
		return Primitive.BYTE.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfByte ofByte(int value) {
		return ofByte(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfByte ofByte(SegmentAllocator allocator, int value) {
		return Primitive.BYTE.pointerOfByte(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfShort ofShort(MemorySegment memory) {
		return Primitive.SHORT.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfShort ofShort(int value) {
		return ofShort(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfShort ofShort(SegmentAllocator allocator, int value) {
		return Primitive.SHORT.pointerOfShort(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfInt ofInt(MemorySegment memory) {
		return Primitive.INT.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfInt ofInt(int value) {
		return ofInt(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfInt ofInt(SegmentAllocator allocator, int value) {
		return Primitive.INT.pointerOfInt(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfLong ofLong(MemorySegment memory) {
		return Primitive.LONG.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfLong ofLong(long value) {
		return ofLong(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfLong ofLong(SegmentAllocator allocator, long value) {
		return Primitive.LONG.pointerOfLong(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfFloat ofFloat(MemorySegment memory) {
		return Primitive.FLOAT.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfFloat ofFloat(int value) {
		return ofFloat(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfFloat ofFloat(SegmentAllocator allocator, int value) {
		return Primitive.FLOAT.pointerOfFloat(allocator, value);
	}

	/**
	 * Returns a primitive pointer for the memory segment.
	 */
	public static OfDouble ofDouble(MemorySegment memory) {
		return Primitive.DOUBLE.pointer(memory);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfDouble ofDouble(int value) {
		return ofDouble(Segments.auto(), value);
	}

	/**
	 * Returns a primitive pointer for the allocated value.
	 */
	public static OfDouble ofDouble(SegmentAllocator allocator, int value) {
		return Primitive.DOUBLE.pointerOfDouble(allocator, value);
	}

	/**
	 * Returns a void pointer for the memory segment.
	 */
	public static OfVoid ofVoid(MemorySegment memory) {
		return memory == null ? null : new OfVoid(memory);
	}

	/**
	 * Returns an untyped pointer for the memory segment.
	 */
	public static Pointer<?> of(MemorySegment memory) {
		return of(memory, Support.VOID);
	}

	/**
	 * Returns an allocated typed pointer to the pointer.
	 */
	public static <P extends PointerType> Pointer<P> of(P pointer) {
		return of(Segments.auto(), pointer);
	}

	/**
	 * Returns an allocated typed pointer to the pointer.
	 */
	public static <P extends PointerType> Pointer<P> of(SegmentAllocator allocator, P pointer) {
		if (pointer == null) return null;
		var type = Reflect.<Supporter<P>>unchecked(pointer.support());
		return type.pointerOf(allocator, pointer);
	}

	/**
	 * Returns a typed pointer for the memory segment.
	 */
	public static <T> Pointer<T> of(MemorySegment memory, Support.Typed<T, ?> type) {
		if (memory == null || type == null) return null;
		return new Pointer<>(memory, type, false);
	}

	Pointer(MemorySegment memory, Support.Typed<T, ?> type, boolean constant) {
		super(memory, type, constant);
	}

	@Override
	public Supporter<Pointer<T>> support() {
		return support(type(), isConst());
	}

	@Override
	public <U> Pointer<U> as(Support.Typed<U, ?> type) {
		if (Objects.equal(type(), type)) return Reflect.unchecked(this);
		return super.as(type);
	}

	/**
	 * Returns the type value at the pointer.
	 */
	public T get() {
		return get(0);
	}

	/**
	 * Returns the type value at the pointer type index.
	 */
	public T get(int index) {
		return type().get(memory(), size(index));
	}

	/**
	 * Writes the type value at the pointer, if not constant.
	 */
	public boolean set(T value) {
		return setAt(0, value);
	}

	/**
	 * Writes the type value at the pointer type index, if not constant.
	 */
	public boolean setAt(int index, T value) {
		if (isConst()) return false;
		return type().write(memory(), size(index), value);
	}

	/**
	 * Writes the type values at the pointer type index with optional nul-termination, if not
	 * constant.
	 */
	@SafeVarargs
	public final int setAll(boolean nul, T... array) {
		return setAllAt(0, nul, array);
	}

	/**
	 * Writes the type values at the pointer type index with optional nul-termination, if not
	 * constant.
	 */
	@SafeVarargs
	public final int setAllAt(int index, boolean nul, T... array) {
		return setArrayAt(index, array, nul);
	}

	// shared

	@Override
	Pointer<T> instance(MemorySegment memory, Support.Typed<T, ?> type, boolean constant) {
		return new Pointer<>(memory, type, constant);
	}
}
