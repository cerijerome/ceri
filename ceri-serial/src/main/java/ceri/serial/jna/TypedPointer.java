package ceri.serial.jna;

import java.util.function.IntFunction;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 * Used for typing pointers that get set in native lib.
 */
public abstract class TypedPointer extends PointerType {

	/**
	 * Convenience constructor for typed pointers
	 */
	public static <T extends TypedPointer> T from(Supplier<T> constructor, Pointer p) {
		T t = constructor.get();
		t.setPointer(p);
		return t;
	}

	/**
	 * Convenience constructor for typed pointer references
	 */
	public static <T extends ByReference<?>> T from(Supplier<T> constructor, Pointer p, int count) {
		T t = constructor.get();
		t.setPointer(p);
		t.setCount(count);
		return t;
	}

	/**
	 * A reference to a typed pointer. Can be nested multiple times. Allows a count to be stored
	 * with the reference to generate arrays.
	 */
	public static class ByReference<T extends TypedPointer> extends TypedPointer {
		private final Supplier<T> constructor;
		private final IntFunction<T[]> arrayConstructor;
		private int count;

		public ByReference(Supplier<T> constructor) {
			this(constructor, null);
		}

		public ByReference(Supplier<T> constructor, IntFunction<T[]> arrayConstructor) {
			setPointer(new Memory(Pointer.SIZE));
			this.constructor = constructor;
			this.arrayConstructor = arrayConstructor;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public int getCount() {
			return count;
		}

		public T[] typedArray() {
			return typedArray(count);
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

	protected TypedPointer() {
		super();
	}

	protected TypedPointer(Pointer p) {
		super(p);
	}

}
