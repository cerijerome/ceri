package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;

/**
 * A base container for a memory segment.
 */
public abstract class RawPointer extends PointerType {
	/** Raw pointer operational support. */
	public static final PointerType.Supporter<RawPointer> $ =
		PointerType.Supporter.of(RawPointer.class, Native.Kind.primitivePointer, Support.VOID,
			(m, _, _) -> Pointer.ofVoid(m), true);

	/**
	 * Adds arithmetic, type array access, and const memory functionality.
	 */
	static abstract class Indexable<P extends Indexable<P, T, A>, T extends Support<?, A, ?>, A>
		extends RawPointer {
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

		@Override
		String typeString() {
			return type.typeDesc();
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

	RawPointer(MemorySegment memory) {
		super(memory);
	}

	@Override
	public MemorySegment memory() {
		return super.memory();
	}

	@Override
	public long address() {
		return super.address();
	}

	/**
	 * Returns true if this pointer has no associated type.
	 */
	public abstract boolean isVoid();

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
	public String desc() {
		return (isConst() ? "const " : "") + super.desc();
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", desc(), Segments.string(memory()));
	}

	@Override
	// shared

	String typeString() {
		return "void";
	}

	/**
	 * Returns true if the pointer is read-only.
	 */
	boolean isConst() {
		return true;
	}

	/**
	 * Returns operational support for the pointer type.
	 */
	abstract Supporter<?> support();
}
