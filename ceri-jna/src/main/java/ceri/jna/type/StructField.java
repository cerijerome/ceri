package ceri.jna.type;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.function.Functions;
import ceri.common.util.Validate;
import ceri.jna.util.Jna;
import ceri.jna.util.Pointers;

/**
 * Provides access to struct field pointers as typed arrays. Can be set as
 * {@code public static final} fields on the struct:
 *
 * <pre>
 * class MyStruct extends Struct {
 * 	public static final StructField.Type<MyStruct, MyType> BY_VAL =
 * 		byVal(t -> t.byVal, MyType::new);
 * 	public static final StructField.Type<MyStruct, MyType> BY_REF =
 * 		byRef(t -> t.byRef, MyType::new);
 * 	public static final StructField.Array<MyStruct, MyType> ARRAY_BY_VAL =
 * 		arrayByVal(t -> t.byVal, t -> t.n, MyType::new, MyType[]::new);
 * 	public static final StructField.Array<MyStruct, MyType> ARRAY_BY_REF =
 * 		arrayByRef(t -> t.byRef, t -> t.n, MyType::new, MyType[]::new);
 * 	public static final StructField.Array<MyStruct, MyType> ARRAY_BY_NULL_TERM_REF =
 * 		arrayByRef(t -> t.byRef, MyType::new, MyType[]::new);
 *
 * 	public Pointer val; // MyType*
 * 	public Pointer ref; // MyType**
 * 	public int n;
 * 	public Pointer byVal; // MyType* n-length array
 * 	public Pointer byRef; // MyType** n-length array
 * 	public Pointer byNullTermRef; // MyType** null-terminated array
 * }
 * </pre>
 */
public class StructField {

	private StructField() {}

	public static interface Type<T, R> {
		/**
		 * Get type from given instance.
		 */
		R get(T t);
	}

	public static interface Array<T, R> {
		/**
		 * Get type at array index from given instance.
		 */
		R get(T t, int i);

		/**
		 * Get type array from given instance.
		 */
		R[] get(T t);
	}

	/**
	 * Create for a type pointer. For {@code type*} types.
	 */
	public static <T, R> Type<T, R> byVal(Functions.Function<T, Pointer> ptrFn,
		Functions.Function<Pointer, R> createFn) {
		return t -> Jna.type(ptrFn.apply(t), createFn);
	}

	/**
	 * Create for a type pointer. For {@code type**} types.
	 */
	public static <T, R> Type<T, R> byRef(Functions.Function<T, Pointer> ptrFn,
		Functions.Function<Pointer, R> createFn) {
		return t -> Jna.type(Pointers.byRef(ptrFn.apply(t)), createFn);
	}

	/**
	 * Create for a pointer to a contiguous type array. For {@code struct*} array types.
	 */
	public static <T, R extends Structure> Array<T, R> arrayByVal(
		Functions.Function<T, Pointer> ptrFn, Functions.ToIntFunction<T> countFn,
		Functions.Function<Pointer, R> createFn, Functions.IntFunction<R[]> arrayFn) {
		int size = createFn.apply(null).size();
		return arrayByVal(ptrFn, countFn, createFn, arrayFn, size);
	}

	/**
	 * Create for a pointer to a contiguous array of given type size. For {@code type*} array types.
	 */
	public static <T, R> Array<T, R> arrayByVal(Functions.Function<T, Pointer> ptrFn,
		Functions.ToIntFunction<T> countFn, Functions.Function<Pointer, R> createFn,
		Functions.IntFunction<R[]> arrayFn, int size) {
		return array((t, i) -> {
			var p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			Validate.index(n, i);
			return Jna.byVal(p, i, createFn, size);
		}, t -> {
			var p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			return Jna.arrayByVal(p, createFn, arrayFn, n, size);
		});
	}

	/**
	 * Create for a pointer to an indirect contiguous type pointer array. For {@code type**} array
	 * types.
	 */
	public static <T, R> Array<T, R> arrayByRef(Functions.Function<T, Pointer> ptrFn,
		Functions.ToIntFunction<T> countFn, Functions.Function<Pointer, R> createFn,
		Functions.IntFunction<R[]> arrayFn) {
		return array((t, i) -> {
			var p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			Validate.index(n, i);
			return Jna.byRef(p, i, createFn);
		}, t -> {
			var p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			return Jna.arrayByRef(p, createFn, arrayFn, n);
		});
	}

	/**
	 * Create for a pointer to an indirect null-terminated contiguous type pointer array. Does not
	 * validate when accessing by index.
	 */
	public static <T, R> Array<T, R> arrayByRef(Functions.Function<T, Pointer> ptrFn,
		Functions.Function<Pointer, R> createFn, Functions.IntFunction<R[]> arrayFn) {
		return array((t, i) -> {
			var p = ptrFn.apply(t);
			return Jna.byRef(p, i, createFn);
		}, t -> {
			var p = ptrFn.apply(t);
			return Jna.arrayByRef(p, createFn, arrayFn);
		});
	}

	private static <T, R> Array<T, R> array(Functions.ObjIntFunction<T, R> getIndexFn,
		Functions.Function<T, R[]> getAllFn) {
		return new Array<>() {
			@Override
			public R get(T t, int i) {
				return getIndexFn.apply(t, i);
			}

			@Override
			public R[] get(T t) {
				return getAllFn.apply(t);
			}
		};
	}
}
