package ceri.serial.jna;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
	public static final Charset DEFAULT_CHARSET = defaultCharset();
	private static final int MEMMOVE_OPTIMAL_MAX_SIZE = 8 * 1024;

	private JnaUtil() {}

	/**
	 * Checks function result, and throws exception if negative
	 */
	public static int verify(int result) throws CException {
		return verify(result, "call");
	}

	/**
	 * Checks function result, and throws exception if negative
	 */
	public static int verify(int result, String name) throws CException {
		if (result >= 0) return result;
		throw new CException(String.format("JNA %s failed: %d", name, result), result);
	}

	/**
	 * Checks a file descriptor validity
	 */
	public static boolean isValidFileDescriptor(int fileDescriptor) {
		return fileDescriptor != INVALID_FILE_DESCRIPTOR;
	}

	/**
	 * Validates a file descriptor
	 */
	public static int validateFileDescriptor(int fileDescriptor) throws IOException {
		if (isValidFileDescriptor(fileDescriptor)) return fileDescriptor;
		throw new IOException("Invalid file descriptor: " + fileDescriptor);
	}

	/**
	 * Load typed native library
	 */
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
	 * Creates a typed array of structures from reference pointer for a null-terminated array.
	 */
	public static <T extends Struct> T[] arrayByRef(Pointer p, 
		Function<Pointer, T> constructor, IntFunction<T[]> arrayConstructor) {
		if (p == null) return arrayConstructor.apply(0);
		Pointer[] refs = p.getPointerArray(0);
		return Stream.of(refs).map(constructor).toArray(arrayConstructor);
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
		return byteArray(p, 0, len);
	}

	/**
	 * Creates an array bytes from of given structure. If count is 0, returns empty array. Make sure
	 * count field is unsigned (call JnaUtil.ubyte/ushort if needed).
	 */
	public static byte[] byteArray(Pointer p, int offset, int len) {
		if (len == 0) return ArrayUtil.EMPTY_BYTE;
		if (p != null) return p.getByteArray(offset, len);
		throw new IllegalArgumentException("Null pointer but non-zero length: " + len);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] byteArray(ByteBuffer buffer, int len) {
		return byteArray(buffer, 0, len);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] byteArray(ByteBuffer buffer, int position, int len) {
		if (position == 0 && len == buffer.limit()) return buffer.array();
		byte[] b = new byte[len];
		buffer.position(position).get(b);
		return b;
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory malloc(byte... array) {
		return malloc(array, 0);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory malloc(byte[] array, int offset) {
		return malloc(array, offset, array.length - offset);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory malloc(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		Memory m = new Memory(length);
		m.write(0, array, offset, length);
		return m;
	}

	/**
	 * Copies byte array from pointer offset to pointer offset. Faster than memmove only
	 * for large sizes (>8k depending on system).
	 */
	public static int memcpy(Pointer p, long toOffset, long fromOffset, int size) {
		return memcpy(p, toOffset, p, fromOffset, size);
	}
	
	/**
	 * Copies byte array from pointer offset to pointer offset. Faster than memmove only
	 * for large sizes (>8k depending on system).
	 */
	public static int memcpy(Pointer to, long toOffset, Pointer from, long fromOffset, int size) {
		if (size < MEMMOVE_OPTIMAL_MAX_SIZE) return memmove(to, toOffset, from, fromOffset, size);
		ByteBuffer toBuffer = to.getByteBuffer(toOffset, size);
		ByteBuffer fromBuffer = from.getByteBuffer(fromOffset, size);
		toBuffer.put(fromBuffer);
		return size;
	}
	
	/**
	 * Copies byte array from pointer offset to pointer offset using a buffer (if needed).
	 */
	public static int memmove(Pointer p, long toOffset, long fromOffset, int size) {
		if (toOffset >= fromOffset + size || toOffset + size <= fromOffset)
			return memcpy(p, toOffset, fromOffset, size);
		return memmove(p, toOffset, p, fromOffset, size);
	}
	
	/**
	 * Copies byte array from pointer offset to pointer offset using a buffer.
	 */
	public static int memmove(Pointer to, long toOffset, Pointer from, long fromOffset, int size) {
		byte[] buffer = from.getByteArray(fromOffset, size);
		to.write(toOffset, buffer, 0, buffer.length);
		return buffer.length;
	}
	
	/**
	 * Decodes string bytes from pointer and length. Pointer.getString() does not 
	 * allow length to be specified, and can be return inconsistent values.
	 */
	public static String string(Memory m) {
		return string(DEFAULT_CHARSET, m);
	}

	/**
	 * Decodes string bytes from pointer and length. Pointer.getString() does not 
	 * allow length to be specified, and can be return inconsistent values.
	 */
	public static String string(Charset charset, Memory m) {
		return string(charset, m, (int) m.size());
	}

	/**
	 * Decodes string bytes from pointer and length. Pointer.getString() does not 
	 * allow length to be specified, and can be return inconsistent values.
	 */
	public static String string(Pointer p, int len) {
		return string(p, 0, len);
	}

	/**
	 * Decodes string bytes from pointer and length. Pointer.getString() does not 
	 * allow length to be specified, and can be return inconsistent values.
	 */
	public static String string(Charset charset, Pointer p, int len) {
		return string(charset, p, 0, len);
	}

	/**
	 * Decodes string bytes from pointer and length. Pointer.getString() does not 
	 * allow length to be specified, and can be return inconsistent values.
	 */
	public static String string(Pointer p, long offset, int len) {
		byte[] b = p.getByteArray(offset, len);
		return new String(b, DEFAULT_CHARSET);
	}

	/**
	 * Decodes string bytes from pointer and length. Pointer.getString() does not 
	 * allow length to be specified, and can be return inconsistent values.
	 */
	public static String string(Charset charset, Pointer p, long offset, int len) {
		byte[] b = p.getByteArray(offset, len);
		return new String(b, charset);
	}

	/**
	 * Decodes string from byte buffer.
	 */
	public static String string(ByteBuffer buffer, int len) {
		return string(buffer, 0, len);
	}

	/**
	 * Decodes string from byte buffer.
	 */
	public static String string(ByteBuffer buffer, int offset, int len) {
		return string(DEFAULT_CHARSET, buffer, offset, len);
	}

	/**
	 * Decodes string from byte buffer.
	 */
	public static String string(Charset charset, ByteBuffer buffer, int len) {
		return string(charset, buffer, 0, len);
	}

	/**
	 * Decodes string from byte buffer.
	 */
	public static String string(Charset charset, ByteBuffer buffer, int offset, int len) {
		return charset.decode(buffer.limit(offset + len).position(offset)).toString();
	}

	/**
	 * Convert byte to unsigned int value
	 */
	public static int ubyte(int value) {
		return value & 0xff;
	}

	/**
	 * Convert short to unsigned int value
	 */
	public static int ushort(int value) {
		return value & 0xffff;
	}

	/**
	 * Verify int is not negative
	 */
	public static int uint(int value) {
		if (value >= 0) return value;
		throw new ArithmeticException("unsigned int overflow: 0x" + StringUtil.toHex(value));
	}

	private static Charset defaultCharset() {
		String encoding = Native.getDefaultStringEncoding();
		if (encoding == null || !Charset.isSupported(encoding)) return Charset.defaultCharset();
		return Charset.forName(encoding);
	}

}
