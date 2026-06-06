package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.Map;
import com.google.common.base.Objects;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.test.FfmTesting;

public class Pointer<T> extends RawPointer.Indexable<Pointer<T>, Support.Typed<T, ?>, T[]> {
	public static final Supporter<Pointer<?>> $ = Reflect.unchecked(new Supporter<>(
		new Supporter.Config<>(Pointer.class, Support.VOID, (m, s, _) -> of(m, s), true)));

	public static void main0(String[] args) {
		var pv = of(IntType.CLong.$.allocAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		var ps = of(IntType.size_t.$.allocAll(true, 1, 2, 3, 4, 5, 6, 7, 8, 9));
		var pb = ofBytes(true, 1, -1, 2, -2, 3, -3, 4);
		var pi = ofInts(true, 1, -1, 2, -2, 3, -3, 4);
		var m = RawPointer.$.allocAll(true, pv, pb, pi);
		var m0 = Pointer.$.allocAll(true, pv, ps);
		FfmTesting.bin(pv);
		FfmTesting.bin(m);
		var pa = Pointer.$.getArray(m, false);
		FfmTesting.bin(pa);
		var cl = pa[0].as(IntType.CLong.$).resize(16).getArray(true);
		FfmTesting.arg(cl);
	}

	/**
	 * A constant void pointer.
	 */
	public static class OfVoid extends RawPointer {
		public static final Supporter<OfVoid> $ = new Supporter<>(
			new Supporter.Config<>(OfVoid.class, Support.VOID, (m, _, _) -> new OfVoid(m), true));

		OfVoid(MemorySegment memory) {
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
	}

	public static class OfByte extends RawPointer.Indexable<OfByte, Primitive.OfByte, byte[]> {
		public static final Supporter<OfByte> $ = support(Primitive.BYTE, false);

		public static Supporter<OfByte> support(Primitive.OfByte type, boolean constant) {
			return new Supporter<>(new Supporter.Config<>(OfByte.class, type,
				(m, t, c) -> new OfByte(m, t, c), constant));
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

		public byte get() {
			return getAt(0);
		}

		public byte getAt(int index) {
			return type().getByte(memory(), size(index));
		}

		public boolean set(byte value) {
			return setAt(0, value);
		}

		public boolean setAt(int index, byte value) {
			if (isConst()) return false;
			return type().setByte(memory(), size(index), value);
		}

		public final int setAll(boolean nul, byte... array) {
			return setAllAt(0, nul, array);
		}

		public final int setAll(boolean nul, int... array) {
			return setAllAt(0, nul, array);
		}

		public final int setAllAt(int index, boolean nul, byte... array) {
			return setArrayAt(index, array, nul);
		}

		public final int setAllAt(int index, boolean nul, int... array) {
			return setArrayAt(index, Array.BYTE.of(array), nul);
		}

		@Override
		OfByte instance(MemorySegment memory, Primitive.OfByte type, boolean constant) {
			return new OfByte(memory, type, constant);
		}
	}

	public static class OfInt extends RawPointer.Indexable<OfInt, Primitive.OfInt, int[]> {
		public static final Supporter<OfInt> $ = support(Primitive.INT, false);

		public static Supporter<OfInt> support(Primitive.OfInt type, boolean constant) {
			return new Supporter<>(new Supporter.Config<>(OfInt.class, type,
				(m, t, c) -> new OfInt(m, t, c), constant));
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

		public int get() {
			return getAt(0);
		}

		public int getAt(int index) {
			return type().getInt(memory(), size(index));
		}

		public boolean set(int value) {
			return setAt(0, value);
		}

		public boolean setAt(int index, int value) {
			if (isConst()) return false;
			return type().setInt(memory(), size(index), value);
		}

		public final int setAll(boolean nul, int... array) {
			return setAllAt(0, nul, array);
		}

		public final int setAllAt(int index, boolean nul, int... array) {
			return setArrayAt(index, array, nul);
		}

		@Override
		OfInt instance(MemorySegment memory, Primitive.OfInt type, boolean constant) {
			return new OfInt(memory, type, constant);
		}
	}

	public static void main(String[] args) {
		var s = Primitive.INT.asPointer(true).asArray(3, true).asPointer(true).asArray(2, false);
		System.out.println(s.typeDesc());
	}

	public static <T> Supporter<Pointer<T>> support(Support.Typed<T, ?> type) {
		return support(type, false);
	}

	public static <T> Supporter<Pointer<T>> support(Support.Typed<T, ?> type, boolean constant) {
		return new Supporter<>(new Supporter.Config<>(Reflect.unchecked(Pointer.class), type,
			(m, s, _) -> of(m, s), constant));
	}

	static <T> Supporter<Pointer<T>> supportFor(TypeNode node) {
		Support.Typed<T, ?> type = Reflect.unchecked(Supports.from(node));
		var constant = node.context().constant();
		return support(type, constant);
	}

	public static OfByte ofByte(MemorySegment memory) {
		return new OfByte(memory, Primitive.BYTE, false);
	}

	public static OfByte ofByte(int value) {
		return ofByte(Primitive.BYTE.allocByte(Segments.auto(), (byte) value));
	}

	public static OfByte ofBytes(boolean nul, byte... values) {
		return ofByte(Primitive.BYTE.allocAll(Segments.auto(), nul, values));
	}

	public static OfByte ofBytes(boolean nul, int... values) {
		return ofByte(Primitive.BYTE.allocAll(Segments.auto(), nul, values));
	}

	public static OfInt ofInt(MemorySegment memory) {
		return new OfInt(memory, Primitive.INT, false);
	}

	public static OfInt ofInt(int value) {
		return ofInt(Primitive.INT.allocInt(Segments.auto(), value));
	}

	public static OfInt ofInts(boolean nul, int... values) {
		return ofInt(Primitive.INT.allocAll(Segments.auto(), nul, values));
	}

	public static OfVoid ofVoid(MemorySegment memory) {
		return new OfVoid(memory);
	}

	public static Pointer<?> of(MemorySegment memory) {
		return of(memory, Support.VOID);
	}

	public static <P extends RawPointer> Pointer<P> of(P pointer) {
		return of(Segments.auto(), pointer);
	}

	public static <P extends RawPointer> Pointer<P> of(SegmentAllocator alloc, P pointer) {
		var memory = alloc.allocateFrom(Layouts.POINTER, pointer.memory());
		var type = switch (pointer) {
			case RawPointer.Indexable<?, ?, ?> i -> i.support();
			default -> OfVoid.$;
		};
		return Pointer.of(memory, Reflect.unchecked(type), pointer.isConst());
	}

	public static <T> Pointer<T> of(MemorySegment memory, Support.Typed<T, ?> type) {
		return of(memory, type, true);
	}

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

	public T get() {
		return get(0);
	}

	public T get(int index) {
		return type().get(memory(), size(index));
	}

	public boolean set(T value) {
		return setAt(0, value);
	}

	public boolean setAt(int index, T value) {
		if (isConst()) return false;
		return type().write(memory(), size(index), value);
	}

	@SafeVarargs
	public final int setAll(boolean nul, T... array) {
		return setAllAt(0, nul, array);
	}

	@SafeVarargs
	public final int setAllAt(int index, boolean nul, T... array) {
		return setArrayAt(index, array, nul);
	}

	@Override
	Pointer<T> instance(MemorySegment memory, Support.Typed<T, ?> type, boolean constant) {
		return new Pointer<>(memory, type, constant);
	}

	private static Map<Class<?>, Supporter<?>> map() {
		return Immutable.convertMapOf(Support::type, t -> t, RawPointer.$, OfVoid.$, OfByte.$,
			OfInt.$);
	}
}
