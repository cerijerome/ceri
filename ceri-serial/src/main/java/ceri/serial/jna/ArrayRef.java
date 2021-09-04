package ceri.serial.jna;

import static ceri.common.collection.ArrayUtil.validateIndex;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Used to access a struct field pointer as a typed array. Can be set as a static final field on 
 * the struct:
 * <pre>
 * class MyStruct extends Struct {
 *	public static final ArrayRef.Struct<MyStruct, MyStruct> NEXT =
 *		ArrayRef.struct(t -> t.next, t -> t.count, MyStruct::new, MyStruct[]::new);
 *	public int count;
 *	public Pointer next; // MyStruct**
 *	
 *	public MyStruct(Pointer p) {
 *		super(p);
 *	}
 * }
 *	MyStruct m = new MyStruct(p);
 *	var next = MyStruct.NEXT.read(m, 0);
 *	var allNext = MyStruct.NEXT.read(m);
 * </pre>
 */
public class ArrayRef<T, R> {
	private final Function<T, Pointer> ptrFn;
	private final ToIntFunction<T> countFn;
	private final Function<Pointer, R> createFn;
	private final IntFunction<R[]> arrayFn;

	/**
	 * Create an instance for struct types.
	 */
	public static <T, R extends Structure> ArrayRef.Struct<T, R> struct(Function<T, Pointer> ptrFn,
		ToIntFunction<T> countFn, Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
		return new ArrayRef.Struct<>(ptrFn, countFn, createFn, arrayFn);
	}

	/**
	 * Create an instance for non-struct types.
	 */
	public static <T, R> ArrayRef<T, R> of(Function<T, Pointer> ptrFn, ToIntFunction<T> countFn,
		Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
		return new ArrayRef<>(ptrFn, countFn, createFn, arrayFn);
	}

	/**
	 * Extends TypeRef to read returned struct fields.
	 */
	public static class Struct<T, R extends Structure> extends ArrayRef<T, R> {
		private Struct(Function<T, Pointer> ptrFn, ToIntFunction<T> countFn,
			Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
			super(ptrFn, countFn, createFn, arrayFn);
		}

		/**
		 * Get struct type and read fields.
		 */
		public R read(T t, int i) {
			return ceri.serial.jna.Struct.read(get(t, i));
		}

		/**
		 * Get struct array and read fields.
		 */
		public R[] read(T t) {
			return ceri.serial.jna.Struct.read(get(t));
		}
	}

	private ArrayRef(Function<T, Pointer> ptrFn, ToIntFunction<T> countFn,
		Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
		this.countFn = countFn;
		this.ptrFn = ptrFn;
		this.createFn = createFn;
		this.arrayFn = arrayFn;
	}

	/**
	 * Get type at array index from given instance.
	 */
	public R get(T t, int i) {
		Pointer p = ptrFn.apply(t);
		int n = countFn.applyAsInt(t);
		validateIndex(n, i);
		return JnaUtil.byRef(p, i, createFn);
	}

	/**
	 * Get type array from given instance.
	 */
	public R[] get(T t) {
		Pointer p = ptrFn.apply(t);
		int n = countFn.applyAsInt(t);
		return JnaUtil.arrayByRef(p, createFn, arrayFn, n);
	}

}
