package ceri.serial.jna;

import java.util.function.IntFunction;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * Used for typing pointers that get set in native lib.
 */
public class TypedPointer extends PointerType {
	
	/**
	 * A reference to a typed pointer. Can be nested multiple times.
	 */
	public static class ByReference<T extends TypedPointer> extends TypedPointer {
		private final Supplier<T> constructor;
		private final IntFunction<T[]> arrayConstructor;

		public ByReference(Supplier<T> constructor) {
			this(constructor, null);
		}

		public ByReference(Supplier<T> constructor, IntFunction<T[]> arrayConstructor) {
			setPointer(new Memory(Pointer.SIZE));
			this.constructor = constructor;
			this.arrayConstructor = arrayConstructor;
		}

		public T[] typedArray(int size) {
			if (arrayConstructor == null)
				throw new UnsupportedOperationException("Typed arrays are not supported");
			Pointer[] pointerArray = getPointer().getPointerArray(0, size);
			T[] array = arrayConstructor.apply(pointerArray.length);
			for (int i = 0; i < pointerArray.length; i++)
				array[i] = typedValue(pointerArray[i]);
			return array;
		}

		private T typedValue(Pointer pointer) {
			T t = constructor.get();
			t.setPointer(pointer);
			return t;
		}
		
		public T typedValue(int offset) {
			return typedValue(getPointer().getPointer(offset));
		}

		public T typedValue() {
			return typedValue(0);
		}
	}

	public TypedPointer() {
		super();
	}

}
