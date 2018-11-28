package ceri.serial.jna;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

public class JnaUtil {
	public static final int INVALID_FILE_DESCRIPTOR = -1;

	private JnaUtil() {}

	public static int verify(int result) throws IOException {
		return verify(result, "call");
	}

	public static int verify(int result, String name) throws IOException {
		if (result >= 0) return result;
		throw BasicUtil.exceptionf(IOException::new, "JNA %s failed: %d", name, result);
	}

	public static boolean isValidFileDescriptor(int fileDescriptor) {
		return fileDescriptor != INVALID_FILE_DESCRIPTOR;
	}

	public static int validateFileDescriptor(int fileDescriptor) throws IOException {
		if (isValidFileDescriptor(fileDescriptor)) return fileDescriptor;
		throw new IOException("Invalid file descriptor: " + fileDescriptor);
	}

	public static <T> T loadLibrary(String name, Class<T> cls) {
		return BasicUtil.uncheckedCast(Native.loadLibrary(name, cls));
	}

	/**
	 * Debug setting to protect against invalid native memory access. Not supported on all
	 * platforms. Returns whether successful.
	 */
	public static boolean setProtected() {
		Native.setProtected(true);
		return Native.isProtected();
	}

	/**
	 * Creates a typed array of structures from reference pointer. If count is 0, returns empty
	 * array. Make sure count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] arrayByRef(Pointer p, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p == null)
			throw new IllegalArgumentException("Null pointer but non-zero count: " + count);
		Pointer[] refs = p.getPointerArray(0, count);
		return Stream.of(refs).map(constructor).toArray(arrayConstructor);
	}

	/**
	 * Creates a typed array of structures from pointer. If count is 0, returns empty array. Make
	 * sure count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] array(Pointer p, int count,
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p != null) return array(constructor.apply(p), count, arrayConstructor);
		throw new IllegalArgumentException("Null pointer but non-zero count: " + count);
	}

	/**
	 * Creates a typed array of given structure. If count is 0, returns empty array. Make sure count
	 * field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T extends Struct> T[] array(Struct p, int count,
		IntFunction<T[]> arrayConstructor) {
		if (count == 0) return arrayConstructor.apply(0);
		return BasicUtil.uncheckedCast(array(p, count));
	}

	/**
	 * Creates an array of given structure. If count is 0, returns null rather than empty array.
	 * Make sure count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static Structure[] array(Struct p, int count) {
		if (p != null) return p.toArray(count);
		if (count == 0) return null;
		throw new IllegalArgumentException("Null pointer but non-zero count: " + count);
	}

	/**
	 * Creates an array bytes from of given structure. If count is 0, returns empty array. Make sure
	 * count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static byte[] byteArray(Pointer p, int len) {
		if (len == 0) return ArrayUtil.EMPTY_BYTE;
		if (p != null) return p.getByteArray(0, len);
		throw new IllegalArgumentException("Null pointer but non-zero length: " + len);
	}

	public static Memory malloc(byte[] array) {
		return malloc(array, 0);
	}

	public static Memory malloc(byte[] array, int offset) {
		return malloc(array, offset, array.length - offset);
	}

	public static Memory malloc(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		Memory m = new Memory(length);
		m.write(0, array, offset, length);
		return m;
	}

	public static int ubyte(byte value) {
		return value & 0xff;
	}

	public static int ushort(short value) {
		return value & 0xffff;
	}

	public static int uint(int value) {
		if (value >= 0) return value;
		throw new ArithmeticException("unsigned int overflow: 0x" + StringUtil.toHex(value));
	}

}
