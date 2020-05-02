package ceri.serial.clib.jna;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import ceri.common.reflect.ReflectUtil;

/**
 * Provides sizeof sizes for known types. Keeps a registry for registered java types. Constant names
 * refer to C types.
 */
public class SizeOf {
	private static final Map<Class<?>, Integer> sizes = new ConcurrentHashMap<>();
	public static final int _INVALID = -1;
	// Size by c type names
	public static final int POINTER = Native.POINTER_SIZE;
	public static final int CHAR = Byte.BYTES;
	public static final int WCHAR = Native.WCHAR_SIZE;
	public static final int SHORT = Short.BYTES;
	public static final int USHORT = SHORT;
	public static final int INT = Integer.BYTES;
	public static final int UINT = INT;
	public static final int LONG = NativeLong.SIZE;
	public static final int ULONG = LONG;
	public static final int LONG_LONG = Long.BYTES;
	public static final int UINT32 = INT;
	public static final int UINT64 = Long.BYTES;
	public static final int SIZE_T = Native.SIZE_T_SIZE;

	static {
		sizes.putAll(Map.of( //
			Byte.class, Byte.BYTES, //
			Character.class, Character.BYTES, //
			Short.class, Short.BYTES, //
			Integer.class, Integer.BYTES, //
			NativeLong.class, NativeLong.SIZE, //
			Long.class, Long.BYTES, //
			Float.class, Float.BYTES, //
			Double.class, Double.BYTES));
	}

	private SizeOf() {}

	/**
	 * Returns the size of the java class type. If the class is not registered, and is a structure
	 * type, an instance is created to calculate the size. Returns -1 if the size cannot be
	 * determined.
	 */
	public static int size(Class<?> cls) {
		return safeSize(sizes.computeIfAbsent(cls, SizeOf::computeSize));
	}

	/**
	 * Returns the size of the java class type. If the class is not registered, and is a structure
	 * type, an instance is created to calculate the size. Throws IllegalArgumentException if the
	 * size cannot be determined.
	 */
	public static int validSize(Class<?> cls) {
		int size = size(cls);
		if (size == _INVALID)
			throw new IllegalArgumentException("Size of " + cls + " cannot be determined");
		return size;
	}

	/**
	 * Register a java class type with its known size.
	 */
	public static int register(Class<?> cls, int size) {
		return safeSize(sizes.put(cls, size));
	}

	private static Integer computeSize(Class<?> cls) {
		if (!Structure.class.isAssignableFrom(cls)) return null;
		try {
			Structure struct = (Structure) ReflectUtil.create(cls);
			return struct == null ? null : struct.size();
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static int safeSize(Integer i) {
		return i == null ? _INVALID : i;
	}

}
