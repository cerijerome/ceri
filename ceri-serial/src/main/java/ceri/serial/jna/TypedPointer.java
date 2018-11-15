package ceri.serial.jna;

import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * Used for typing pointers that get set in native lib.
 */
public class TypedPointer extends PointerType {
	public static class ByReference<T extends TypedPointer> extends com.sun.jna.ptr.ByReference {
		private final Supplier<T> constructor;

		public ByReference(Supplier<T> constructor) {
			super(Pointer.SIZE);
			this.constructor = constructor;
		}

		public T[] toArray(int size, IntFunction<T[]> arrayConstructor) {
			return IntStream.range(0, size).mapToObj(this::getValue).toArray(arrayConstructor);
		}
		
		public T[] populate(T[] array) {
			for (int i = 0; i < array.length; i++) array[i] = getValue(i);
			return array;
		}
		
		public T getValue(int offset) {
			T t = constructor.get();
			t.setPointer(getPointer().getPointer(offset));
			return t;
		}
		
		public T getValue() {
			return getValue(0);
		}
	}

	public static class ArrayReference<T extends TypedPointer> extends com.sun.jna.ptr.ByReference {
		private final Supplier<T> constructor;
		private final IntFunction<T[]> arrayConstructor;

		public ArrayReference(Supplier<T> constructor, IntFunction<T[]> arrayConstructor) {
			super(Pointer.SIZE);
			this.constructor = constructor;
			this.arrayConstructor = arrayConstructor;
		}

		public T[] toArray(int size) {
			return ref().toArray(size, arrayConstructor);
		}
		
		protected ByReference<T> ref() {
			ByReference<T> ref = new ByReference<>(constructor);
			ref.setPointer(getPointer().getPointer(0));
			return ref;
		}
	}

	public TypedPointer() {
		super();
	}

}
