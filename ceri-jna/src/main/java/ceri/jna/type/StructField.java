package ceri.jna.type;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.function.Funcs.ObjIntFunction;
import ceri.common.validation.ValidationUtil;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;

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
	public static <T, R> Type<T, R> byVal(Function<T, Pointer> ptrFn,
		Function<Pointer, R> createFn) {
		return t -> JnaUtil.type(ptrFn.apply(t), createFn);
	}

	/**
	 * Create for a type pointer. For {@code type**} types.
	 */
	public static <T, R> Type<T, R> byRef(Function<T, Pointer> ptrFn,
		Function<Pointer, R> createFn) {
		return t -> JnaUtil.type(PointerUtil.byRef(ptrFn.apply(t)), createFn);
	}

	/**
	 * Create for a pointer to a contiguous type array. For {@code struct*} array types.
	 */
	public static <T, R extends Structure> Array<T, R> arrayByVal(Function<T, Pointer> ptrFn,
		ToIntFunction<T> countFn, Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
		int size = createFn.apply(null).size();
		return arrayByVal(ptrFn, countFn, createFn, arrayFn, size);
	}

	/**
	 * Create for a pointer to a contiguous array of given type size. For {@code type*} array types.
	 */
	public static <T, R> Array<T, R> arrayByVal(Function<T, Pointer> ptrFn,
		ToIntFunction<T> countFn, Function<Pointer, R> createFn, IntFunction<R[]> arrayFn,
		int size) {
		return array((t, i) -> {
			Pointer p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			ValidationUtil.validateIndex(n, i);
			return JnaUtil.byVal(p, i, createFn, size);
		}, t -> {
			Pointer p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			return JnaUtil.arrayByVal(p, createFn, arrayFn, n, size);
		});
	}

	/**
	 * Create for a pointer to an indirect contiguous type pointer array. For {@code type**} array
	 * types.
	 */
	public static <T, R> Array<T, R> arrayByRef(Function<T, Pointer> ptrFn,
		ToIntFunction<T> countFn, Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
		return array((t, i) -> {
			Pointer p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			ValidationUtil.validateIndex(n, i);
			return JnaUtil.byRef(p, i, createFn);
		}, t -> {
			Pointer p = ptrFn.apply(t);
			int n = countFn.applyAsInt(t);
			return JnaUtil.arrayByRef(p, createFn, arrayFn, n);
		});
	}

	/**
	 * Create for a pointer to an indirect null-terminated contiguous type pointer array. Does not
	 * validate when accessing by index.
	 */
	public static <T, R> Array<T, R> arrayByRef(Function<T, Pointer> ptrFn,
		Function<Pointer, R> createFn, IntFunction<R[]> arrayFn) {
		return array((t, i) -> {
			Pointer p = ptrFn.apply(t);
			return JnaUtil.byRef(p, i, createFn);
		}, t -> {
			Pointer p = ptrFn.apply(t);
			return JnaUtil.arrayByRef(p, createFn, arrayFn);
		});
	}

	private static <T, R> Array<T, R> array(ObjIntFunction<T, R> getIndexFn,
		Function<T, R[]> getAllFn) {
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
