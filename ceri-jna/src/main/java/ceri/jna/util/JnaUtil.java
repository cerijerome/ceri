package ceri.jna.util;

import java.lang.ref.Reference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.sun.jna.IntegerType;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.Lazy;
import ceri.common.except.Exceptions;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.util.Validate;
import ceri.jna.type.CLong;
import ceri.jna.type.CUlong;

public class JnaUtil {
	private static final Pattern LAST_ERROR_REGEX =
		Pattern.compile("^(?:\\[\\d+\\]|errno was \\d+)\\s*");
	private static final int MEMCPY_OPTIMAL_MIN_SIZE = 8 * 1024; // determined from test results
	public static final Charset DEFAULT_CHARSET = defaultCharset();

	private JnaUtil() {}

	/**
	 * Debug setting to protect against invalid native memory access. Not supported on all
	 * platforms. Only use when debugging as this interferes with signal handlers on non-windows
	 * systems. Returns true if successful.
	 */
	public static boolean setProtected() {
		Native.setProtected(true);
		return Native.isProtected();
	}

	/**
	 * Returns a closeable resource to prevent gc. Use for objects referenced in async callbacks.
	 */
	public static <T> Enclosure<T> callback(T callback) {
		return Enclosure.of(callback, Reference::reachabilityFence);
	}

	/**
	 * Extracts the message without error code from the exception. Returns empty string if the
	 * message is just the number.
	 */
	public static String message(LastErrorException e) {
		if (e == null) return "";
		var message = e.getMessage();
		if (message == null) return "";
		// if (message == null || LAST_ERROR_PLAIN_MSG_REGEX.matcher(message).matches()) return "";
		return LAST_ERROR_REGEX.matcher(e.getMessage()).replaceFirst("").trim();
	}

	/**
	 * Allocates new memory, or returns null if size is 0.
	 */
	public static Memory malloc(int size) {
		return size == 0 ? null : new Memory(size);
	}

	/**
	 * Allocates and zeroes new memory, or returns null if size is 0.
	 */
	public static Memory calloc(int size) {
		var m = malloc(size);
		if (m != null) m.clear();
		return m;
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory mallocBytes(int... array) {
		return mallocBytes(ArrayUtil.bytes.of(array));
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory mallocBytes(byte[] array) {
		return mallocBytes(array, 0);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory mallocBytes(byte[] array, int offset) {
		return mallocBytes(array, offset, array.length - offset);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory mallocBytes(byte[] array, int offset, int length) {
		Validate.slice(array.length, offset, length);
		if (length == 0) return null;
		var m = new Memory(length);
		m.write(0, array, offset, length);
		return m;
	}

	/**
	 * Copies bytes from pointer offset to pointer offset. Calls memmove for smaller sizes (<8k), or
	 * if regions overlap. Returns the number of bytes copied.
	 */
	public static int memcpy(Pointer p, long toOffset, long fromOffset, int size) {
		if (toOffset == fromOffset) return size;
		return memcpy(p, toOffset, p, fromOffset, size);
	}

	/**
	 * Copies bytes from pointer offset to pointer offset. Calls memmove for smaller sizes (<8k), or
	 * if regions overlap. Returns the number of bytes copied.
	 */
	public static int memcpy(Pointer to, long toOffset, Pointer from, long fromOffset, int size) {
		if (size < MEMCPY_OPTIMAL_MIN_SIZE
			|| PointerUtil.overlap(to, toOffset, from, fromOffset, size))
			return memmove(to, toOffset, from, fromOffset, size);
		var toBuffer = to.getByteBuffer(toOffset, size);
		var fromBuffer = from.getByteBuffer(fromOffset, size);
		toBuffer.put(fromBuffer);
		return size;
	}

	/**
	 * Copies byte array from pointer offset to pointer offset using a buffer (if needed).
	 */
	public static int memmove(Pointer p, long toOffset, long fromOffset, int size) {
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
	 * A lazy buffer, that only allocates memory when needed.
	 */
	public static Lazy.Supplier<RuntimeException, Memory> lazyMem(long size) {
		return Lazy.unsafe(() -> new Memory(size));
	}

	/**
	 * Provide typed sub-view of given memory, or null.
	 */
	public static Memory share(Memory m, long offset) {
		return share(m, offset, size(m) - offset);
	}

	/**
	 * Provide typed sub-view of given memory, or null.
	 */
	public static Memory share(Memory m, long offset, long len) {
		long size = size(m);
		PointerUtil.validate(m, size, offset, len);
		if (m == null || (offset == 0 && len == size)) return m;
		return (Memory) m.share(offset, len);
	}

	/**
	 * Frees the memory if not null.
	 */
	public static void close(Memory m) {
		if (m != null) m.close();
	}

	/**
	 * Returns the memory size, 0 for null memory.
	 */
	public static long size(Memory m) {
		return m == null ? 0 : m.size();
	}

	/**
	 * Returns the memory size, 0 for null memory. Fails if size is larger than int.
	 */
	public static int intSize(Memory m) {
		return Math.toIntExact(size(m));
	}

	/**
	 * Perform a bitwise-and on an integer type value.
	 */
	public static <T extends IntegerType> T and(T t, long value) {
		t.setValue(t.longValue() & value);
		return t;
	}

	/**
	 * Perform a bitwise-or on an integer type value.
	 */
	public static <T extends IntegerType> T or(T t, long value) {
		t.setValue(t.longValue() | value);
		return t;
	}

	/**
	 * Perform a bitwise-and followed by bitwise-or on an integer type value.
	 */
	public static <T extends IntegerType> T andOr(T t, long and, long or) {
		t.setValue((t.longValue() & and) | or);
		return t;
	}

	/**
	 * Apply and set the native long value.
	 */
	public static <T extends IntegerType> T apply(T t, Functions.LongOperator operator) {
		t.setValue(operator.applyAsLong(t.longValue()));
		return t;
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static short ubyte(Pointer p, long offset) {
		return Maths.ubyte(p.getByte(offset));
	}

	/**
	 * Get unsigned value from reference pointer.
	 */
	public static short ubyte(ByteByReference ref) {
		return Maths.ubyte(ref.getValue());
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static int ushort(Pointer p, int offset) {
		return Maths.ushort(p.getShort(offset));
	}

	/**
	 * Get unsigned value from reference pointer.
	 */
	public static int ushort(ShortByReference ref) {
		return Maths.ushort(ref.getValue());
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static long uint(Pointer p, int offset) {
		return Maths.uint(p.getInt(offset));
	}

	/**
	 * Get unsigned value from reference pointer.
	 */
	public static long uint(IntByReference ref) {
		return Maths.uint(ref.getValue());
	}

	/**
	 * Get signed value from pointer.
	 */
	public static long clong(Pointer p, long offset) {
		return CLong.readFrom(p, offset).longValue();
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static long culong(Pointer p, long offset) {
		return CUlong.readFrom(p, offset).longValue();
	}

	/**
	 * Set signed value at pointer.
	 */
	public static void clong(Pointer p, long offset, long value) {
		new CLong(value).write(p, offset);
	}

	/**
	 * Set unsigned value at pointer.
	 */
	public static void culong(Pointer p, long offset, long value) {
		new CUlong(value).write(p, offset);
	}

	/**
	 * Creates a reference containing the value.
	 */
	public static ByteByReference byteRef(int value) {
		return new ByteByReference((byte) value);
	}

	/**
	 * Creates a reference containing the value.
	 */
	public static ShortByReference shortRef(int value) {
		return new ShortByReference((short) value);
	}

	/**
	 * Creates a reference containing the value.
	 */
	public static IntByReference intRef(int value) {
		return new IntByReference(value);
	}

	/**
	 * Creates a reference containing the value.
	 */
	public static LongByReference longRef(long value) {
		return new LongByReference(value);
	}

	/**
	 * Creates a reference pointer containing the value.
	 */
	public static Pointer byteRefPtr(int value) {
		return byteRef(value).getPointer();
	}

	/**
	 * Creates a reference pointer containing the value.
	 */
	public static Pointer shortRefPtr(int value) {
		return shortRef(value).getPointer();
	}

	/**
	 * Creates a reference pointer containing the value.
	 */
	public static Pointer intRefPtr(int value) {
		return intRef(value).getPointer();
	}

	/**
	 * Creates a reference pointer containing the value.
	 */
	public static Pointer longRefPtr(long value) {
		return longRef(value).getPointer();
	}

	/**
	 * Creates a fixed-length contiguous array of given type size. For {@code type*} array types.
	 */
	@SuppressWarnings("resource")
	public static <T> T[] mallocArray(Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count, int size) {
		return arrayByVal(malloc(size * count), constructor, arrayFn, count, size);
	}

	/**
	 * Creates a zeroed fixed-length contiguous array of given type size. For {@code type*} array
	 * types.
	 */
	@SuppressWarnings("resource")
	public static <T> T[] callocArray(Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count, int size) {
		return arrayByVal(calloc(size * count), constructor, arrayFn, count, size);
	}

	/**
	 * Creates a typed array from an indirect contiguous null-terminated pointer array. If the
	 * pointer is null, an empty array is returned. For {@code type**} array types.
	 */
	public static <T> T[] arrayByRef(Pointer p, Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn) {
		return Stream.of(PointerUtil.arrayByRef(p)).map(constructor).toArray(arrayFn);
	}

	/**
	 * Creates a typed array from an indirect contiguous fixed-length pointer array. If the pointer
	 * is null, and for any null indirect pointers, the array will contain null items. Make sure
	 * count is unsigned (use ubyte/ushort if needed). For {@code type**} array types.
	 */
	public static <T> T[] arrayByRef(Pointer p, Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count) {
		return Stream.of(PointerUtil.arrayByRef(p, count)).map(typeFn(constructor))
			.toArray(arrayFn);
	}

	/**
	 * Creates a type from index i of an indirect contiguous pointer array. If the pointer or
	 * indirect pointer is null, null is returned. For {@code type**} array types.
	 */
	public static <T> T byRef(Pointer p, int i, Functions.Function<Pointer, T> constructor) {
		return type(PointerUtil.byRef(p, i), constructor);
	}

	/**
	 * Creates a typed array from a fixed-length contiguous array of given type size. Returns an
	 * array of null pointers if the pointer is null. For {@code type*} array types.
	 */
	public static <T> T[] arrayByVal(Pointer p, Functions.Function<Pointer, T> constructor,
		Functions.IntFunction<T[]> arrayFn, int count, int size) {
		return Stream.of(PointerUtil.arrayByVal(p, count, size)).map(typeFn(constructor))
			.toArray(arrayFn);
	}

	/**
	 * Creates a type from index i of a contiguous pointer array. Returns null if the pointer is
	 * null. For {@code type*} array types.
	 */
	public static <T> T byVal(Pointer p, int i, Functions.Function<Pointer, T> constructor,
		int size) {
		return type(PointerUtil.byVal(p, i, size), constructor);
	}

	/**
	 * Creates a type from a pointer. Returns null if the pointer is null.
	 */
	public static <T> T type(Pointer p, Functions.Function<Pointer, T> constructor) {
		return p == null ? null : constructor.apply(p);
	}

	/**
	 * Converts a constructor into a null-aware constructor. Useful for streams.
	 */
	public static <T> Functions.Function<Pointer, T>
		typeFn(Functions.Function<Pointer, T> constructor) {
		return p -> type(p, constructor);
	}

	/**
	 * Creates an array of bytes from the given memory pointer.
	 */
	public static byte[] bytes(Memory m) {
		return bytes(m, 0);
	}

	/**
	 * Creates an array of bytes from the given memory pointer.
	 */
	public static byte[] bytes(Memory m, long offset) {
		return bytes(m, offset, Math.toIntExact(size(m) - offset));
	}

	/**
	 * Creates an array of bytes from the given memory pointer.
	 */
	public static byte[] bytes(Pointer p, long offset, long length) {
		if (PointerUtil.validate(p, offset, length) != null)
			return p.getByteArray(offset, Math.toIntExact(length));
		return ArrayUtil.bytes.empty;
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] bytes(ByteBuffer buffer) {
		return bytes(buffer, 0);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] bytes(ByteBuffer buffer, int position) {
		Objects.requireNonNull(buffer);
		return bytes(buffer, position, buffer.limit() - position);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] bytes(ByteBuffer buffer, int position, int length) {
		Objects.requireNonNull(buffer);
		if (length == 0) return ArrayUtil.bytes.empty;
		if (position == 0 && length == buffer.limit() && buffer.hasArray()) return buffer.array();
		byte[] bytes = new byte[length];
		buffer.get(position, bytes);
		return bytes;
	}

	/**
	 * Throws an exception if the memory slice is out of range.
	 */
	public static void validateSlice(long size, long offset, long length) {
		if (offset < 0 || offset > size)
			throw new IndexOutOfBoundsException("Offset must be 0.." + size + ": " + offset);
		if (length < 0 || offset + length > size) throw new IndexOutOfBoundsException(
			"Length must be 0.." + (size - offset) + ": " + length);
	}

	/**
	 * Throws an exception if the given buffer is not direct.
	 */
	public static <T extends Buffer> T validateDirect(T buffer) {
		if (buffer.isDirect()) return buffer;
		throw Exceptions.illegalArg("Buffer must be direct: " + buffer);
	}

	/**
	 * Convenience method to get a pointer for a direct buffer.
	 */
	public static Pointer pointer(Buffer buffer) {
		if (buffer == null) return null;
		validateDirect(buffer);
		return Native.getDirectBufferPointer(buffer);
	}

	/**
	 * Convenience method to get a byte buffer for the memory.
	 */
	public static ByteBuffer buffer(Memory m) {
		return buffer(m, 0);
	}

	/**
	 * Convenience method to get a byte buffer for the memory.
	 */
	public static ByteBuffer buffer(Memory m, long offset) {
		return buffer(m, offset, size(m) - offset);
	}

	/**
	 * Convenience method to get a byte buffer for the pointer.
	 */
	public static ByteBuffer buffer(Pointer p, long offset, long length) {
		if (PointerUtil.validate(p, offset, length) != null) return p.getByteBuffer(offset, length);
		return ByteBuffer.allocate(0);
	}

	/**
	 * Decodes a string from fixed-length bytes using default charset.
	 */
	public static String string(byte[] bytes) {
		return string(DEFAULT_CHARSET, bytes);
	}

	/**
	 * Decodes string from fixed-length bytes using charset.
	 */
	public static String string(Charset charset, byte[] bytes) {
		return new String(bytes, 0, bytes.length, charset);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer using default charset.
	 */
	public static String string(Memory m) {
		return string(m, 0);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer using default charset.
	 */
	public static String string(Memory m, long offset) {
		return string(m, offset, m.size() - offset);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer using default charset.
	 */
	public static String string(Pointer p, long offset, long length) {
		return string(DEFAULT_CHARSET, p, offset, length);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer.
	 */
	public static String string(Charset charset, Memory m) {
		return string(charset, m, 0);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer.
	 */
	public static String string(Charset charset, Memory m, long offset) {
		return string(charset, m, offset, m.size() - offset);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer.
	 */
	public static String string(Charset charset, Pointer p, long offset, long length) {
		byte[] bytes = bytes(p, offset, length);
		return new String(bytes, charset);
	}

	/**
	 * Decodes string from byte buffer using default charset.
	 */
	public static String string(ByteBuffer buffer) {
		return string(buffer, 0);
	}

	/**
	 * Decodes string from byte buffer using default charset.
	 */
	private static String string(ByteBuffer buffer, int position) {
		return string(buffer, position, buffer.limit() - position);
	}

	/**
	 * Decodes string from byte buffer using default charset.
	 */
	public static String string(ByteBuffer buffer, int position, int length) {
		return string(DEFAULT_CHARSET, buffer, position, length);
	}

	/**
	 * Decodes string from byte buffer using charset.
	 */
	public static String string(Charset charset, ByteBuffer buffer) {
		return string(charset, buffer, 0);
	}

	/**
	 * Decodes string from byte buffer using charset.
	 */
	private static String string(Charset charset, ByteBuffer buffer, int position) {
		return string(charset, buffer, position, buffer.limit() - position);
	}

	/**
	 * Decodes string from byte buffer using charset.
	 */
	public static String string(Charset charset, ByteBuffer buffer, int position, int len) {
		return charset.decode(buffer.limit(position + len).position(position)).toString();
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array index after reading.
	 */
	public static int read(Pointer p, byte[] buffer) {
		return read(p, buffer, 0);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array index after reading.
	 */
	public static int read(Pointer p, byte[] buffer, int index) {
		return read(p, buffer, index, buffer.length - index);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array index after reading.
	 */
	public static int read(Pointer p, byte[] buffer, int index, int length) {
		return read(p, 0, buffer, index, length);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array index after reading.
	 */
	public static int read(Pointer p, long offset, byte[] buffer) {
		return read(p, offset, buffer, 0);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array index after reading.
	 */
	public static int read(Pointer p, long offset, byte[] buffer, int index) {
		return read(p, offset, buffer, index, buffer.length - index);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array index after reading.
	 */
	public static int read(Pointer p, long offset, byte[] buffer, int index, int length) {
		Validate.slice(buffer.length, index, length);
		PointerUtil.validate(p, offset, length);
		if (length > 0) p.read(offset, buffer, index, length);
		return index + length;
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, byte[] buffer) {
		return write(p, buffer, 0);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, byte[] buffer, int index) {
		return write(p, buffer, index, buffer.length - index);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, byte[] buffer, int index, int length) {
		return write(p, 0, buffer, index, length);
	}

	/**
	 * Copies bytes to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, long offset, int... buffer) {
		return write(p, offset, ArrayUtil.bytes.of(buffer));
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, long offset, byte[] buffer) {
		return write(p, offset, buffer, 0);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, long offset, byte[] buffer, int index) {
		return write(p, offset, buffer, index, buffer.length - index);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static long write(Pointer p, long offset, byte[] buffer, int index, int length) {
		Validate.slice(buffer.length, index, length);
		if (PointerUtil.validate(p, offset, length) != null) p.write(offset, buffer, index, length);
		return offset + length;
	}

	/**
	 * Writes byte value to memory. Returns the pointer offset after writing.
	 */
	public static long fill(Memory m, int value) {
		return fill(m, 0, value);
	}

	/**
	 * Writes byte value to memory, starting at offset. Returns the pointer offset after writing.
	 */
	public static long fill(Memory m, long offset, int value) {
		return fill(m, offset, size(m) - offset, value);
	}

	/**
	 * Writes byte value to the pointer, starting at offset for given length. Returns the pointer
	 * offset after writing.
	 */
	public static long fill(Pointer p, long offset, long length, int value) {
		if (PointerUtil.validate(p, offset, length) != null)
			p.setMemory(offset, length, (byte) value);
		return offset + length;
	}

	/**
	 * Attempt to get a signed or unsigned long from a value. Returns null if incompatible.
	 */
	public static Long asLong(Object value, boolean signed) {
		return switch (value) {
			case Byte b -> (long) (signed ? b : Maths.ubyte(b));
			case Short s -> (long) (signed ? s : Maths.ushort(s));
			case Integer i -> (signed ? i : Maths.uint(i));
			case Number n -> n.longValue();
			default -> null;
		};
	}

	/* Support methods */

	private static Charset defaultCharset() {
		var encoding = Native.getDefaultStringEncoding();
		if (!Charset.isSupported(encoding)) return Charset.defaultCharset();
		return Charset.forName(encoding);
	}
}
