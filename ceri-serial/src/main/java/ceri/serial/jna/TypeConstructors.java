package ceri.serial.jna;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Collects construction functions for a class. Creates no-arg instance, instance from pointer, and
 * array.
 */
public class TypeConstructors<T extends Structure> {
	private final Class<T> cls;
	private final Supplier<T> of;
	private final Function<Pointer, T> ofPtr;
	private final IntFunction<T[]> array;

	public static <T extends Structure> TypeConstructors<T> of(Class<T> cls, Supplier<T> of,
		Function<Pointer, T> ofPtr, IntFunction<T[]> array) {
		return new TypeConstructors<>(cls, of, ofPtr, array);
	}

	private TypeConstructors(Class<T> cls, Supplier<T> of, Function<Pointer, T> ofPtr,
		IntFunction<T[]> array) {
		this.cls = cls;
		this.of = of;
		this.ofPtr = ofPtr;
		this.array = array;
	}

	public T create() {
		requireOf();
		return of.get();
	}

	public T create(Pointer p) {
		requireOfPtr();
		return ofPtr.apply(p);
	}

	/**
	 * Creates an array filled
	 */
	public T[] array(int count) {
		requireArray();
		return array.apply(count);
	}

	public T[] array(Pointer... ptrs) {
		requireOfPtr();
		requireArray();
		return Stream.of(ptrs).map(ofPtr).toArray(array);
	}

	public T[] arrayByRef(Pointer p) {
		return array(p.getPointerArray(0));
	}

	public T[] arrayByRef(Pointer p, int count) {
		return array(p.getPointerArray(0, count));
	}

	public T[] arrayByVal(int count) {
		requireOf();
		requireArray();
		return Struct.arrayByVal(of, array, count);
	}

	public T[] arrayByVal(Pointer p, int count) {
		requireOfPtr();
		requireArray();
		return Struct.arrayByVal(p, ofPtr, array, count);
	}

	public T[] arrayByVal(T t, int count) {
		requireArray();
		return Struct.arrayByVal(t, array, count);
	}

	private void requireOf() {
		if (of == null)
			throw new UnsupportedOperationException("Default constructor not supported: " + cls);
	}

	private void requireOfPtr() {
		if (ofPtr == null) throw new UnsupportedOperationException(
			"Constructor from pointer not supported: " + cls);
	}

	private void requireArray() {
		if (array == null) throw new UnsupportedOperationException("Arrays not supported: " + cls);
	}
}
