package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.Objects;
import ceri.common.function.Functions;
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;

/**
 * An opaque base container for a memory segment.
 */
public abstract class PointerType {
	private final MemorySegment memory;

	public static class myptr extends PointerType {
		public static final Supporter<myptr> $ = PointerType.support(myptr.class);

		public myptr(MemorySegment memory) {
			super(memory);
		}
	}

	public static void main(String[] args) {
		var p = new myptr(Segments.auto().allocate(13));
		var pp = Pointer.of(p);
		var v = Pointer.ofVoid(p.memory());
		var pv = Pointer.of(v).asConst();
		var i = Primitive.INT.wrapAll(1, 2, 3);
		var pi = Pointer.ofInt(i).asConst();
		var i2 = i.asSlice(1, 4);
		var pi2 = Pointer.ofInt(i2);
		System.out.println(p);
		System.out.println(pp);
		System.out.println(v);
		System.out.println(pv);
		System.out.println(i);
		System.out.println(pi);
		System.out.println(i2);
		System.out.println(pi2);
	}

	/**
	 * Typed pointer constructor.
	 */
	interface Create<P extends PointerType, S extends Support<?, ?, ?>> {
		/**
		 * Creates a new pointer instance.
		 */
		P apply(MemorySegment memory, S support, boolean constant);
	}

	/**
	 * Operational support for pointers.
	 */
	public static class Supporter<P extends PointerType> extends Support.Typed<P, AddressLayout> {
		private final Config<P, ?> config;

		record Config<P extends PointerType, S extends Support<?, ?, ?>>(Class<P> type,
			Native.Kind kind, S support, Create<P, S> create, boolean constant) {

			private Config<P, S> asConst() {
				return new Config<>(type(), kind(), support(), create(), constant);
			}

			private P create(MemorySegment memory) {
				return create().apply(memory, support(), constant());
			}
		}

		static <P extends PointerType, S extends Support<?, ?, ?>> Supporter<P> of(Class<P> type,
			Native.Kind kind, S support, Create<P, S> create, boolean constant) {
			var config = new Config<>(type, kind, support, create, constant);
			return new Supporter<>(config, Layouts.POINTER);
		}

		Supporter(Config<P, ?> config, AddressLayout layout) {
			super(layout);
			this.config = config;
		}

		/**
		 * Creates an instance for the memory segment.
		 */
		public P of(MemorySegment memory) {
			if (memory == null) return null;
			return config.create(memory);
		}

		/**
		 * Returns true if pointer data is read only.
		 */
		public boolean isConst() {
			return config.constant();
		}

		/**
		 * Returns support for constant pointer data. No change if already constant.
		 */
		public Supporter<P> asConst() {
			return isConst() ? this : new Supporter<>(config.asConst(), layout());
		}

		@Override
		public Native.Kind kind() {
			return config.kind();
		}

		@Override
		public Class<P> type() {
			return config.type();
		}

		@Override
		public P val() {
			return of(MemorySegment.NULL);
		}

		@Override
		public Supporter<P> align(long align) {
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new Supporter<>(config, layout);
		}

		@Override
		public Supporter<P> order(ByteOrder order) {
			var layout = Layouts.order(layout(), order);
			return layout == layout() ? this : new Supporter<>(config, layout);
		}

		@Override
		public String typeDesc() {
			return PointerType.typeDesc(type(), support(), isConst());
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), isConst(), support());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			return (obj instanceof Supporter<?> s) && equalTo(s) && isConst() == s.isConst()
				&& Objects.equals(support(), s.support());
		}

		@Override
		P rawGet(MemorySegment memory, long offset, long length) {
			return of(memory.get(layout(), offset));
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, P value) {
			memory.set(layout(), offset, value.memory());
		}

		private Support<?, ?, ?> support() {
			return config.support();
		}
	}

	/**
	 * Extends pointer type to allow casting.
	 */
	public static abstract class Raw extends PointerType {
		/** Raw pointer operational support. */
		public static final Supporter<Raw> $ = Supporter.of(Raw.class,
			Native.Kind.PRIMITIVE_POINTER, Support.VOID, (m, _, _) -> Pointer.ofVoid(m), true);

		Raw(MemorySegment memory) {
			super(memory);
		}

		/**
		 * Returns true if this pointer has no associated type.
		 */
		public boolean isVoid() {
			return true;
		}

		/**
		 * Returns the memory segment size in bytes.
		 */
		public long size() {
			return memory().byteSize();
		}

		/**
		 * Returns the memory segment size in bytes.
		 */
		public int sizeInt() {
			return Math.toIntExact(size());
		}

		/**
		 * Casts this pointer to void.
		 */
		public Pointer.OfVoid asVoid() {
			return Pointer.ofVoid(memory());
		}

		/**
		 * Casts this pointer to byte.
		 */
		public Pointer.OfByte asByte() {
			return asByte(Primitive.BYTE);
		}

		/**
		 * Casts this pointer to byte.
		 */
		public Pointer.OfByte asByte(Primitive.OfByte type) {
			return new Pointer.OfByte(memory(), type, isConst());
		}

		/**
		 * Casts this pointer to int.
		 */
		public Pointer.OfInt asInt() {
			return asInt(Primitive.INT);
		}

		/**
		 * Casts this pointer to int.
		 */
		public Pointer.OfInt asInt(Primitive.OfInt type) {
			return new Pointer.OfInt(memory(), type, isConst());
		}

		/**
		 * Casts this pointer to another type.
		 */
		public <U> Pointer<U> as(Class<U> cls) {
			return as(Supports.DEF.typedFrom(cls));
		}

		/**
		 * Casts this pointer to another type.
		 */
		public <U> Pointer<U> as(Support.Typed<U, ?> type) {
			if (type == null) return null;
			return Pointer.of(memory(), type, isConst());
		}

		@Override
		public String toString() {
			return typeDesc() + '(' + Segments.string(memory()) + ')';
		}

		@Override
		Support<?, ?, ?> type() {
			return Support.VOID;
		}
	}

	/**
	 * Adds arithmetic, type array access, and const memory functionality.
	 */
	public static abstract class Indexable<P extends Indexable<P, T, A>, //
		T extends Support<?, A, ?>, A> extends Raw {
		private final T type;
		private final boolean constant;

		Indexable(MemorySegment memory, T type, boolean constant) {
			super(memory);
			this.type = type;
			this.constant = constant;
		}

		@Override
		public boolean isVoid() {
			return type.isVoid();
		}

		/**
		 * Returns the type support.
		 */
		@Override
		public T type() {
			return type;
		}

		/**
		 * Applies the alignment to the type.
		 */
		public P typeAlign(long align) {
			return copy(null, Reflect.unchecked(type().align(align)), isConst());
		}

		/**
		 * Applies the byte order to the type.
		 */
		public P typeOrder(ByteOrder order) {
			return copy(null, Reflect.unchecked(type().order(order)), isConst());
		}

		/**
		 * Returns operational support for the pointer type.
		 */
		@Override
		public abstract Supporter<P> support();

		/**
		 * Returns the type layout.
		 */
		public MemoryLayout layout() {
			return type().layout();
		}

		/**
		 * Returns the number of type instances within the memory segment.
		 */
		public long count() {
			return Layouts.count(layout(), size());
		}

		/**
		 * Returns the number of type instances within the memory segment.
		 */
		public int countInt() {
			return Math.toIntExact(count());
		}

		/**
		 * Returns a pointer with resized memory segment.
		 */
		public P resize() {
			return resize(1);
		}

		/**
		 * Returns a pointer with resized memory segment.
		 */
		public P resize(int count) {
			return resize(0, count);
		}

		/**
		 * Returns a pointer with resized memory segment.
		 */
		public P resize(int index, int count) {
			return copy(Segments.resize(memory(), size(index), size(count)), null, isConst());
		}

		/**
		 * Returns a pointer with sliced memory segment.
		 */
		public P slice(int index) {
			return slice(index, Integer.MAX_VALUE);
		}

		/**
		 * Returns a pointer with sliced memory segment.
		 */
		public P slice(int index, int count) {
			return copy(Segments.slice(memory(), size(index), size(count)), null, isConst());
		}

		/**
		 * Returns a pointer with resized memory segment if native, otherwise with sliced segment.
		 */
		public P reslice() {
			return reslice(1);
		}

		/**
		 * Returns a pointer with resized memory segment if native, otherwise with sliced segment.
		 */
		public P reslice(int count) {
			return reslice(0, count);
		}

		/**
		 * Returns a pointer with resized memory segment if native, otherwise with sliced segment.
		 */
		public P reslice(int index, int count) {
			return copy(Segments.reslice(memory(), size(index), size(count)), null, isConst());
		}

		/**
		 * Returns true if the memory segment cannot be modified through the pointer.
		 */
		@Override
		public boolean isConst() {
			return constant;
		}

		/**
		 * Returns a pointer that is unable to modify the memory segment.
		 */
		public P asConst() {
			return copy(null, null, true);
		}

		/**
		 * Returns a new array from memory, with up to optional nul-termination.
		 */
		public A getArray(boolean nul) {
			return getArray(Integer.MAX_VALUE, nul);
		}

		/**
		 * Returns a new array from memory, with up to optional nul-termination.
		 */
		public A getArray(int count, boolean nul) {
			return getArrayAt(0, count, nul);
		}

		/**
		 * Returns a new array from memory, with up to optional nul-termination.
		 */
		public A getArrayAt(int index, boolean nul) {
			return getArrayAt(index, Integer.MAX_VALUE, nul);
		}

		/**
		 * Returns a new array from memory, with up to optional nul-termination.
		 */
		public A getArrayAt(int index, int count, boolean nul) {
			return type().getArray(memory(), size(index), size(count), nul);
		}

		/**
		 * Writes the array to memory with optional nul-termination; returns the number of elements
		 * written.
		 */
		public int setArray(A array, boolean nul) {
			return setArray(array, 0, nul);
		}

		/**
		 * Writes the array to memory with optional nul-termination; returns the number of elements
		 * written.
		 */
		public int setArray(A array, int start, boolean nul) {
			return setArray(array, start, Integer.MAX_VALUE, nul);
		}

		/**
		 * Writes the array to memory with optional nul-termination; returns the number of elements
		 * written.
		 */
		public int setArray(A array, int start, int count, boolean nul) {
			return setArrayAt(0, array, start, count, nul);
		}

		/**
		 * Writes the array to memory with optional nul-termination; returns the number of elements
		 * written.
		 */
		public int setArrayAt(int index, A array, boolean nul) {
			return setArrayAt(index, array, 0, nul);
		}

		/**
		 * Writes the array to memory with optional nul-termination; returns the number of elements
		 * written.
		 */
		public int setArrayAt(int index, A array, int start, boolean nul) {
			return setArrayAt(index, array, start, Integer.MAX_VALUE, nul);
		}

		/**
		 * Writes the array to memory with optional nul-termination; returns the number of elements
		 * written.
		 */
		public int setArrayAt(int index, A array, int start, int count, boolean nul) {
			if (isConst()) return 0;
			return type().writeArray(memory(), size(index), Long.MAX_VALUE, array, start, count,
				nul);
		}

		/**
		 * Returns the byte size for the given the pointer type count.
		 */
		long size(int count) {
			return type().size(count);
		}

		/**
		 * Creates a new pointer instance.
		 */
		abstract P instance(MemorySegment memory, T type, boolean constant);

		/**
		 * Creates a new pointer instance, or returns the current instance if unchanged.
		 */
		P copy(MemorySegment memory, T type, boolean constant) {
			if (memory == null) memory = memory();
			if (type == null) type = type();
			if (memory() == memory && type() == type && isConst() == constant)
				return Reflect.unchecked(this);
			return instance(memory, type, constant);
		}
	}

	/**
	 * Creates a support instance for the custom type.
	 */
	static <P extends PointerType> Supporter<P> support(Class<P> cls) {
		return Reflect.unchecked(Supports.DEF.from(cls));
	}

	/**
	 * Creates a support instance for the custom type.
	 */
	static <P extends PointerType> Supporter<P> supportFor(Class<P> cls) {
		var create = constructorFor(cls);
		var config = new Supporter.Config<>(cls, Native.Kind.POINTER_TYPE, null,
			(m, _, _) -> create.apply(m), true);
		return new Supporter<>(config, Layouts.POINTER);
	}

	protected PointerType(MemorySegment memory) {
		this.memory = Basics.def(memory, MemorySegment.NULL);
	}

	/**
	 * Returns true if the contained memory segment has native address 0.
	 */
	public boolean isNull() {
		return Segments.isNull(memory());
	}

	/**
	 * Returns the memory segment of the pointer.
	 */
	public MemorySegment memory() {
		return memory;
	}

	/**
	 * Returns the pointer address.
	 */
	public long address() {
		return Segments.address(memory());
	}

	/**
	 * Returns true if the pointer is read-only.
	 */
	public boolean isConst() {
		return true;
	}

	/**
	 * Provides a simple descriptor for the pointer type.
	 */
	public String typeDesc() {
		return typeDesc(getClass(), type(), isConst());
	}

	@Override
	public int hashCode() {
		return Objects.hash(memory(), isConst(), type());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof PointerType p) && Objects.equals(memory(), p.memory())
			&& isConst() == p.isConst() && Objects.equals(type(), p.type());
	}

	@Override
	public String toString() {
		return typeDesc() + '(' + Segments.addressString(memory()) + ')';
	}

	// shared

	/**
	 * Returns operational support for the pointer type.
	 */
	Supporter<?> support() {
		return support(Reflect.getClass(this));
	}

	Support<?, ?, ?> type() {
		return null;
	}

	// support

	private static String typeDesc(Class<?> type, Support<?, ?, ?> support, boolean constant) {
		if (support == null) return type.getSimpleName() + '*';
		var desc = Support.wrapDesc(support.typeDesc()) + '*';
		if (!constant || support.isVoid()) return desc;
		return "const " + desc;
	}

	private static <P extends PointerType> Functions.Function<MemorySegment, P>
		constructorFor(Class<P> cls) {
		return Handles.asFunction(Handles.constructor(cls, MemorySegment.class));
	}
}
