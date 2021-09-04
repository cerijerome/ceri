package ceri.serial.jna;

import java.util.function.Function;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Used to access a struct field pointer as a typed object. Can be set as a static final field on
 * the struct:
 * 
 * <pre>
 * class MyStruct extends Struct {
 * 	public static final TypeRef.Struct<MyStruct, MyStruct> NEXT =
 * 		TypeRef.struct(t -> t.next, MyStruct::new);
 * 	public Pointer next; // MyStruct*
 * 
 * 	public MyStruct(Pointer p) {
 * 		super(p);
 * 	}
 * }
 * 
 * MyStruct m = new MyStruct(p);
 * var next = MyStruct.NEXT.read(m);
 * </pre>
 */
public class TypeRef<T, R> {
	private final Function<T, Pointer> ptrFn;
	private final Function<Pointer, R> createFn;

	/**
	 * Create an instance for struct types.
	 */
	public static <T, R extends Structure> TypeRef.Struct<T, R> struct(Function<T, Pointer> ptrFn,
		Function<Pointer, R> createFn) {
		return new TypeRef.Struct<>(ptrFn, createFn);
	}

	/**
	 * Create an instance for non-struct types.
	 */
	public static <T, R> TypeRef<T, R> of(Function<T, Pointer> ptrFn,
		Function<Pointer, R> createFn) {
		return new TypeRef<>(ptrFn, createFn);
	}

	/**
	 * Extends TypeRef to read returned struct fields.
	 */
	public static class Struct<T, R extends Structure> extends TypeRef<T, R> {
		private Struct(Function<T, Pointer> ptrFn, Function<Pointer, R> createFn) {
			super(ptrFn, createFn);
		}

		/**
		 * Get struct type and read fields.
		 */
		public R read(T t) {
			return ceri.serial.jna.Struct.read(get(t));
		}
	}

	private TypeRef(Function<T, Pointer> ptrFn, Function<Pointer, R> createFn) {
		this.ptrFn = ptrFn;
		this.createFn = createFn;
	}

	/**
	 * Get type from given instance.
	 */
	public R get(T t) {
		Pointer p = ptrFn.apply(t);
		return p == null ? null : createFn.apply(p);
	}

}
