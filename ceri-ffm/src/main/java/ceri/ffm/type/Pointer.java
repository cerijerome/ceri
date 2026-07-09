package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import com.google.common.base.Objects;
import ceri.common.array.Array;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.test.FfmTesting;

public class Pointer<T> extends PointerType.Indexable<Pointer<T>, Support.Typed<T, ?>, T[]> {
	public static final Supporter<Pointer<?>> $ = Reflect.unchecked(support(Support.VOID, true));

	public static void main(String[] args) {
		var pv = of(IntType.CLong.$.allocAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		var ps = of(IntType.size_t.$.allocAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		var pb = ofBytes(true, 1, -1, 2, -2, 3, -3, 4);
		var pi = ofInts(true, 1, -1, 2, -2, 3, -3, 4);
		var m = PointerType.Raw.$.allocAll(true, pv, pb, pi);
		var m0 = Pointer.$.allocAll(true, pv, ps);
		FfmTesting.bin(pv);
		FfmTesting.bin(m);
		FfmTesting.bin(m0);
		var pa = Pointer.$.getArray(m, false);
		FfmTesting.bin(pa);
		var cl = pa[0].as(IntType.CLong.$).resize(16).getArray(true);
		FfmTesting.arg(cl);
	}

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
		public boolean isVoid() {
			return true;
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
	 * Primitive int pointer.
	 */
	public static class OfInt extends PointerType.Indexable<OfInt, Primitive.OfInt, int[]> {
		public static final Supporter<OfInt> $ = support(Primitive.INT, false);

		static Supporter<OfInt> support(Primitive.OfInt type, boolean constant) {
			return Supporter.of(OfInt.class, Native.Kind.PRIMITIVE_POINTER, type,
				(m, t, c) -> new OfInt(m, t, c), constant);
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

	static <T> Supporter<Pointer<T>> support(Support.Typed<T, ?> type, boolean constant) {
		return Supporter.of(Reflect.unchecked(Pointer.class), Native.Kind.POINTER, type,
			(m, s, _) -> of(m, s), constant);
	}

	static <T> Supporter<Pointer<T>> supportFor(TypeNode node, boolean constant) {
		Support.Typed<T, ?> type = Reflect.unchecked(Supports.DEF.from(node));
		return support(type, constant);
	}

	/**
	 * Returns a primitive byte pointer for the memory segment.
	 */
	public static OfByte ofByte(MemorySegment memory) {
		return new OfByte(memory, Primitive.BYTE, false);
	}

	/**
	 * Returns a primitive byte pointer for the allocated value.
	 */
	public static OfByte ofByte(int value) {
		return ofByte(Segments.auto(), value);
	}

	/**
	 * Returns a primitive byte pointer for the allocated value.
	 */
	public static OfByte ofByte(SegmentAllocator allocator, int value) {
		return ofByte(Primitive.BYTE.allocByte(allocator, value));
	}

	/**
	 * Returns a primitive byte pointer for the allocated values with optional nul-termination.
	 */
	public static OfByte ofBytes(boolean nul, byte... values) {
		return ofBytes(Segments.auto(), nul, values);
	}

	/**
	 * Returns a primitive byte pointer for the allocated values with optional nul-termination.
	 */
	public static OfByte ofBytes(SegmentAllocator allocator, boolean nul, byte... values) {
		return ofByte(Primitive.BYTE.allocAll(allocator, nul, values));
	}

	/**
	 * Returns a primitive byte pointer for the allocated values with optional nul-termination.
	 */
	public static OfByte ofBytes(boolean nul, int... values) {
		return ofBytes(Segments.auto(), nul, values);
	}

	/**
	 * Returns a primitive byte pointer for the allocated values with optional nul-termination.
	 */
	public static OfByte ofBytes(SegmentAllocator allocator, boolean nul, int... values) {
		return ofByte(Primitive.BYTE.allocAll(allocator, nul, values));
	}

	/**
	 * Returns a primitive int pointer for the memory segment.
	 */
	public static OfInt ofInt(MemorySegment memory) {
		return new OfInt(memory, Primitive.INT, false);
	}

	/**
	 * Returns a primitive int pointer for the allocated value.
	 */
	public static OfInt ofInt(int value) {
		return ofInt(Segments.auto(), value);
	}

	/**
	 * Returns a primitive int pointer for the allocated value.
	 */
	public static OfInt ofInt(SegmentAllocator allocator, int value) {
		return ofInt(Primitive.INT.allocInt(allocator, value));
	}

	/**
	 * Returns a primitive byte pointer for the allocated values with optional nul-termination.
	 */
	public static OfInt ofInts(boolean nul, int... values) {
		return ofInts(Segments.auto(), nul, values);
	}

	/**
	 * Returns a primitive byte pointer for the allocated values with optional nul-termination.
	 */
	public static OfInt ofInts(SegmentAllocator allocator, boolean nul, int... values) {
		return ofInt(Primitive.INT.allocAll(allocator, nul, values));
	}

	/**
	 * Returns a void pointer for the memory segment.
	 */
	public static OfVoid ofVoid(MemorySegment memory) {
		return new OfVoid(memory);
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
		return of(pointer, false);
	}

	/**
	 * Returns an allocated typed pointer to the pointer.
	 */
	public static <P extends PointerType> Pointer<P> of(P pointer, boolean constant) {
		return of(Segments.auto(), pointer, constant);
	}

	/**
	 * Returns an allocated typed pointer to the pointer.
	 */
	public static <P extends PointerType> Pointer<P> of(SegmentAllocator allocator, P pointer) {
		return of(allocator, pointer, false);
	}

	/**
	 * Returns an allocated typed pointer to the pointer.
	 */
	public static <P extends PointerType> Pointer<P> of(SegmentAllocator allocator, P pointer,
		boolean constant) {
		if (allocator == null || pointer == null) return null;
		var type = Reflect.<Supporter<P>>unchecked(pointer.support());
		var memory = type.alloc(allocator, pointer);
		return of(memory, type, constant);
	}

	/**
	 * Returns a typed pointer for the memory segment.
	 */
	public static <T> Pointer<T> of(MemorySegment memory, Support.Typed<T, ?> type) {
		return of(memory, type, false);
	}

	/**
	 * Returns a typed pointer for the memory segment.
	 */
	public static <T> Pointer<T> of(MemorySegment memory, Support.Typed<T, ?> type,
		boolean constant) {
		return new Pointer<>(memory, type, constant);
	}

	private Pointer(MemorySegment memory, Support.Typed<T, ?> type, boolean constant) {
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

	@Override
	Pointer<T> instance(MemorySegment memory, Support.Typed<T, ?> type, boolean constant) {
		return new Pointer<>(memory, type, constant);
	}
}
