package ceri.serial.jna;

import java.util.function.Function;
import java.util.function.IntFunction;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Extends Structure to read fields when constructed from a pointer. Also makes array handling more
 * robust/typed.
 */
public abstract class Struct extends Structure {

	protected Struct() {}

	protected Struct(Pointer p) {
		super(p);
		read();
	}

	/**
	 * Overrides default behavior, allowing zero-length arrays with no instances created.
	 */
	@Override
	public Structure[] toArray(Structure[] array) {
		if (array.length == 0) return array;
		return super.toArray(array);
	}

	/**
	 * Returns byte array for field, from remaining bytes in structure. Make sure length field is
	 * unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	protected byte[] fieldByteArrayRem(String name, int structLen) {
		int offset = fieldOffset(name);
		if (offset >= structLen) return new byte[0];
		return getPointer().getByteArray(offset, structLen - offset);
	}

	/**
	 * Returns the pointer offset to the field.
	 */
	protected Pointer fieldPointer(String name) {
		int offset = fieldOffset(name);
		return getPointer().share(offset);
	}

	/**
	 * Creates a typed array of structures at given field. Used for marker field[0] with length
	 * given in another field value. Make sure count field is unsigned (call JnaUtil.ubyte/ushort if
	 * needed).
	 */
	protected <T extends Struct> T[] fieldArray(String name, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		return JnaUtil.array(fieldPointer(name), count, constructor, arrayConstructor);
	}

	/**
	 * Creates a typed array of structures referenced by given field pointer array. Used for marker
	 * *field[0] with length given in another field value. Make sure count field is unsigned (call
	 * JnaUtil.ubyte/ushort if needed).
	 */
	protected <T extends Struct> T[] fieldArrayByRef(String name, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		return JnaUtil.arrayByRef(fieldPointer(name), count, constructor, arrayConstructor);
	}

}
