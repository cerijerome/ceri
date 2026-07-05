package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.core.Support;
import ceri.ffm.core.Supports;

/**
 * An abstract container for a memory segment.
 */
public abstract class RawPointer {
	private static final String CONST = "const ";
	public static final Supporter<RawPointer> $ =
		Supporter.of(RawPointer.class, Support.VOID, (m, _, _) -> Pointer.ofVoid(m), true);
	private final MemorySegment memory;

	// - fixed layout (no flexible multi-dim nul-term)
	// - cache by type + dims + nul + ... (not node type)

	public interface Create<P extends RawPointer, S extends Support<?, ?, ?>> {
		P apply(MemorySegment memory, S support, boolean constant);
	}

	public static class Supporter<P extends RawPointer> extends Support.Typed<P, AddressLayout> {
		private final Config<P, ?> config;

		public record Config<P extends RawPointer, S extends Support<?, ?, ?>>(Class<P> type,
			S support, Create<P, S> create, boolean constant) {

			public Config<P, S> asConst() {
				return constant() ? this : new Config<>(type(), support(), create(), constant);
			}

			public Native.Kind kind() {
				if (support.isVoid() || support.kind() == Native.Kind.primitive)
					return Native.Kind.primitivePointer;
				return Native.Kind.pointer;
			}

			public P create(MemorySegment memory) {
				return create().apply(memory, support(), constant());
			}

			public String desc() {
				return (constant() && !support.isVoid() ? CONST : "")
					+ Native.wrap(support().typeDesc()) + '*';
			}
		}

		static <P extends RawPointer, S extends Support<?, ?, ?>> Supporter<P> of(Class<P> type,
			S support, Create<P, S> create) {
			return of(type, support, create, false);
		}

		static <P extends RawPointer, S extends Support<?, ?, ?>> Supporter<P> of(Class<P> type,
			S support, Create<P, S> create, boolean constant) {
			var config = new Config<>(type, support, create, constant);
			return new Supporter<>(config, Layouts.POINTER);
		}

		Supporter(Config<P, ?> config) {
			this(config, Layouts.POINTER);
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
		 * Returns support for constant pointer data. No change if already constant.
		 */
		public Supporter<P> asConst() {
			var config = this.config.asConst();
			return config == this.config ? this : new Supporter<>(config, layout());
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
		public String typeDesc() {
			return config.desc();
		}

		@Override
		public P val() {
			return config.create(MemorySegment.NULL);
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
		protected P rawGet(MemorySegment memory, long offset, long length) {
			return config.create(memory.get(layout(), offset));
		}

		@Override
		protected void rawWrite(MemorySegment memory, long offset, long length, P value) {
			memory.set(layout(), offset, value.memory());
		}
	}

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

		long size(int count) {
			return type().size(count);
		}

		abstract P instance(MemorySegment memory, T type, boolean constant);

		P copy(MemorySegment memory, T type, boolean constant) {
			if (memory == null) memory = memory();
			if (type == null) type = type();
			if (memory() == memory && type() == type && isConst() == constant)
				return Reflect.unchecked(this);
			return instance(memory, type, constant);
		}
	}

	RawPointer(MemorySegment memory) {
		this.memory = Basics.def(memory, MemorySegment.NULL);
	}

	/**
	 * Returns the contained memory segment.
	 */
	public MemorySegment memory() {
		return memory;
	}

	/**
	 * Returns the memory segment address.
	 */
	public long address() {
		return Segments.address(memory);
	}

	/**
	 * Returns true if the contained memory segment has native address 0.
	 */
	public boolean isNull() {
		return Segments.isNull(memory);
	}

	/**
	 * Returns true if this pointer has no associated type.
	 */
	public abstract boolean isVoid();

	/**
	 * Returns the memory segment size in bytes.
	 */
	public long size() {
		return memory.byteSize();
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

	public static void main(String[] args) {
		var p = Pointer.of(Segments.auto().allocate(13));
		System.out.println(p);
		var pi = p.as(int.class);
		System.out.println(pi);
	}

	/**
	 * Casts this pointer to another type.
	 */
	public <U> Pointer<U> as(Support.Typed<U, ?> type) {
		if (type == null) return null;
		return Pointer.of(memory(), type, isConst());
	}

	/**
	 * Provides a simple descriptor for the pointer type.
	 */
	public String desc() {
		return (isConst() ? "const " : "") + typeString() + "*";
	}

	@Override
	public String toString() {
		return desc() + " " + Segments.string(memory);
	}

	String typeString() {
		return "void";
	}

	boolean isConst() {
		return true;
	}
}
