package ceri.jna.util;

import static ceri.common.collection.ArrayUtil.validateSlice;
import static ceri.common.exception.ExceptionUtil.exceptionf;
import java.lang.ref.Reference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;
import ceri.common.math.MathUtil;
import ceri.common.util.ValueCache;

public class JnaUtil {
	private static final int MEMCPY_OPTIMAL_MIN_SIZE = 8 * 1024; // determined from test results
	public static final Charset DEFAULT_CHARSET = defaultCharset();
	public static final boolean NLONG_LONG = NativeLong.SIZE == Long.BYTES;

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
	public static RuntimeCloseable closeable(Object callback) {
		return () -> Reference.reachabilityFence(callback);
	}

	/**
	 * Allocates long-term new memory, or returns null if size is 0.
	 */
	@SuppressWarnings("resource")
	public static GcMemory gcMalloc(int size) {
		return GcMemory.of(malloc(size));
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
		Memory m = malloc(size);
		if (m != null) m.clear();
		return m;
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory mallocBytes(int... array) {
		return mallocBytes(ArrayUtil.bytes(array));
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
		ArrayUtil.validateSlice(array.length, offset, length);
		if (length == 0) return null;
		Memory m = new Memory(length);
		m.write(0, array, offset, length);
		return m;
	}

	/**
	 * Copies byte array from pointer offset to pointer offset. Faster than memmove only for large
	 * sizes (>8k depending on system).
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
		ByteBuffer toBuffer = to.getByteBuffer(toOffset, size);
		ByteBuffer fromBuffer = from.getByteBuffer(fromOffset, size);
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
	public static ExceptionSupplier<RuntimeException, Memory> lazyBuffer(long size) {
		return ValueCache.of(() -> new Memory(size));
	}

	/**
	 * Gets the memory offset by the given number of bytes.
	 */
	public static Memory offset(Memory m, long offset) {
		return m == null ? null : offset(m, offset, m.size() - offset);
	}

	/**
	 * Gets the memory offset by the given number of bytes.
	 */
	public static Memory offset(Memory m, long offset, long length) {
		if (m == null || (offset == 0 && length == m.size())) return m;
		return (Memory) m.share(offset, length);
	}

	/**
	 * Provide typed share method for memory.
	 */
	public static Memory share(Memory m, long offset, long size) {
		return m == null ? null : (Memory) m.share(offset, size);
	}

	/**
	 * Gets the memory size. Throws ArithmeticException if outside signed int range.
	 */
	public static int size(Memory m) {
		return m == null ? 0 : Math.toIntExact(m.size());
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static short ubyte(Pointer p, int offset) {
		return MathUtil.ubyte(p.getByte(offset));
	}

	/**
	 * Get unsigned value from reference pointer.
	 */
	public static short ubyte(ByteByReference ref) {
		return MathUtil.ubyte(ref.getValue());
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static int ushort(Pointer p, int offset) {
		return MathUtil.ushort(p.getShort(offset));
	}

	/**
	 * Get unsigned value from reference pointer.
	 */
	public static int ushort(ShortByReference ref) {
		return MathUtil.ushort(ref.getValue());
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static long uint(Pointer p, int offset) {
		return MathUtil.uint(p.getInt(offset));
	}

	/**
	 * Get unsigned value from reference pointer.
	 */
	public static long uint(IntByReference ref) {
		return MathUtil.uint(ref.getValue());
	}

	/**
	 * Convenience constructor for native long.
	 */
	public static NativeLong nlong(long value) {
		return new NativeLong(value);
	}

	/**
	 * Convenience constructor for unsigned native long.
	 */
	public static NativeLong unlong(long value) {
		return new NativeLong(value, true);
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static long unlong(Pointer p, int offset) {
		return unlong(p.getNativeLong(offset));
	}

	/**
	 * Get unsigned value from pointer.
	 */
	public static long unlong(NativeLongByReference ref) {
		return unlong(ref.getValue());
	}

	/**
	 * Converts value to unsigned.
	 */
	public static long unlong(NativeLong value) {
		return NLONG_LONG ? value.longValue() : MathUtil.uint(value.intValue());
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
	public static NativeLongByReference nlongRef(NativeLong value) {
		return new NativeLongByReference(value);
	}

	/**
	 * Creates a reference containing the value.
	 */
	public static NativeLongByReference nlongRef(long value) {
		return nlongRef(new NativeLong(value));
	}

	/**
	 * Creates a reference containing the unsigned value.
	 */
	public static NativeLongByReference unlongRef(long value) {
		return nlongRef(new NativeLong(value, true));
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
	public static Pointer nlongRefPtr(NativeLong value) {
		return nlongRef(value).getPointer();
	}

	/**
	 * Creates a reference pointer containing the value.
	 */
	public static Pointer nlongRefPtr(long value) {
		return nlongRefPtr(new NativeLong(value));
	}

	/**
	 * Creates a reference pointer containing the value.
	 */
	public static Pointer unlongRefPtr(long value) {
		return unlongRef(value).getPointer();
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
	public static <T> T[] mallocArray(Function<Pointer, T> constructor, IntFunction<T[]> arrayFn,
		int count, int size) {
		return arrayByVal(gcMalloc(size * count).m, constructor, arrayFn, count, size);
	}

	/**
	 * Creates a zeroed fixed-length contiguous array of given type size. For {@code type*} array
	 * types.
	 */
	public static <T> T[] callocArray(Function<Pointer, T> constructor, IntFunction<T[]> arrayFn,
		int count, int size) {
		return arrayByVal(gcMalloc(size * count).clear().m, constructor, arrayFn, count, size);
	}

	/**
	 * Creates a typed array from an indirect contiguous null-terminated pointer array. If the
	 * pointer is null, an empty array is returned. For {@code type**} array types.
	 */
	public static <T> T[] arrayByRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn) {
		return Stream.of(PointerUtil.arrayByRef(p)).map(constructor).toArray(arrayFn);
	}

	/**
	 * Creates a typed array from an indirect contiguous fixed-length pointer array. If the pointer
	 * is null, and for any null indirect pointers, the array will contain null items. Make sure
	 * count is unsigned (use ubyte/ushort if needed). For {@code type**} array types.
	 */
	public static <T> T[] arrayByRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count) {
		return Stream.of(PointerUtil.arrayByRef(p, count)).map(typeFn(constructor))
			.toArray(arrayFn);
	}

	/**
	 * Creates a type from index i of an indirect contiguous pointer array. If the pointer or
	 * indirect pointer is null, null is returned. For {@code type**} array types.
	 */
	public static <T> T byRef(Pointer p, int i, Function<Pointer, T> constructor) {
		return type(PointerUtil.byRef(p, i), constructor);
	}

	/**
	 * Creates a typed array from a fixed-length contiguous array of given type size. Returns an
	 * array of null pointers if the pointer is null. For {@code type*} array types.
	 */
	public static <T> T[] arrayByVal(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayFn, int count, int size) {
		return Stream.of(PointerUtil.arrayByVal(p, count, size)).map(typeFn(constructor))
			.toArray(arrayFn);
	}

	/**
	 * Creates a type from index i of a contiguous pointer array. Returns null if the pointer is
	 * null. For {@code type*} array types.
	 */
	public static <T> T byVal(Pointer p, int i, Function<Pointer, T> constructor, int size) {
		return type(PointerUtil.byVal(p, i, size), constructor);
	}

	/**
	 * Creates a type from a pointer. Returns null if the pointer is null.
	 */
	public static <T> T type(Pointer p, Function<Pointer, T> constructor) {
		return p == null ? null : constructor.apply(p);
	}

	/**
	 * Converts a constructor into a null-aware constructor. Useful for streams.
	 */
	public static <T> Function<Pointer, T> typeFn(Function<Pointer, T> constructor) {
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
	private static byte[] bytes(Memory m, long offset) {
		return bytes(m, offset, Math.toIntExact(m.size() - offset));
	}

	/**
	 * Creates an array of bytes from the given memory pointer.
	 */
	public static byte[] bytes(Pointer p, long offset, long length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		if (p == null) throw new IllegalArgumentException("Pointer is null");
		return p.getByteArray(offset, Math.toIntExact(length));
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
		return bytes(buffer, position, buffer.limit() - position);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] bytes(ByteBuffer buffer, int position, int length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		if (buffer == null) throw new IllegalArgumentException("Buffer is null");
		if (position == 0 && length == buffer.limit() && buffer.hasArray()) return buffer.array();
		byte[] bytes = new byte[length];
		buffer.get(position, bytes);
		return bytes;
	}

	/**
	 * Throws an exception if the given buffer is not direct.
	 */
	public static <T extends Buffer> T validateDirect(T buffer) {
		if (buffer.isDirect()) return buffer;
		throw exceptionf("Buffer must be direct: " + buffer);
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
		if (m == null) return ByteBuffer.allocate(0);
		return buffer(m, offset, m.size() - offset);
	}

	/**
	 * Convenience method to get a byte buffer for the pointer.
	 */
	public static ByteBuffer buffer(Pointer p, long offset, long length) {
		if (length == 0) return ByteBuffer.allocate(0);
		if (p == null) throw new IllegalArgumentException("Pointer is null");
		return p.getByteBuffer(offset, length);
	}

	/**
	 * Decodes string from zero-terminated bytes using default charset.
	 */
	public static String string(byte[] bytes) {
		return string(DEFAULT_CHARSET, bytes);
	}

	/**
	 * Decodes string from zero-terminated bytes using charset.
	 */
	public static String string(Charset charset, byte[] bytes) {
		int i = ArrayUtil.indexOf(bytes, 0, (byte) 0);
		return new String(bytes, 0, i >= 0 ? i : bytes.length, charset);
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
	 * Copies bytes from the pointer to the byte array. Returns the array offset after reading.
	 */
	public static int read(Pointer p, byte[] buffer) {
		return read(p, buffer, 0);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array offset after reading.
	 */
	public static int read(Pointer p, byte[] buffer, int offset) {
		return read(p, buffer, offset, buffer.length - offset);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array offset after reading.
	 */
	public static int read(Pointer p, byte[] buffer, int offset, int length) {
		return read(p, 0, buffer, offset, length);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array offset after reading.
	 */
	public static int read(Pointer p, long index, byte[] buffer) {
		return read(p, index, buffer, 0);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array offset after reading.
	 */
	public static int read(Pointer p, long index, byte[] buffer, int offset) {
		return read(p, index, buffer, offset, buffer.length - offset);
	}

	/**
	 * Copies bytes from the pointer to the byte array. Returns the array offset after reading.
	 */
	public static int read(Pointer p, long index, byte[] buffer, int offset, int length) {
		validateSlice(buffer.length, offset, length);
		p.read(index, buffer, offset, length);
		return offset + length;
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, byte[] buffer) {
		return write(p, buffer, 0);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, byte[] buffer, int offset) {
		return write(p, buffer, offset, buffer.length - offset);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, byte[] buffer, int offset, int length) {
		return write(p, 0, buffer, offset, length);
	}

	/**
	 * Copies bytes to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, int index, int... buffer) {
		return write(p, index, ArrayUtil.bytes(buffer));
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, int index, byte[] buffer) {
		return write(p, index, buffer, 0);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, int index, byte[] buffer, int offset) {
		return write(p, index, buffer, offset, buffer.length - offset);
	}

	/**
	 * Copies bytes from the array to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, int index, byte[] buffer, int offset, int length) {
		validateSlice(buffer.length, offset, length);
		p.write(index, buffer, offset, length);
		return index + length;
	}

	/**
	 * Writes byte value to memory. Returns the pointer offset after writing.
	 */
	public static int fill(Memory m, int value) {
		return fill(m, 0, value);
	}

	/**
	 * Writes byte value to memory, starting at offset. Returns the pointer offset after writing.
	 */
	public static int fill(Memory m, int offset, int value) {
		int size = size(m);
		validateSlice(size, offset, 0);
		return fill(m, offset, size - offset, value);
	}

	/**
	 * Writes byte value to the pointer, starting at offset for given length. Returns the pointer
	 * offset after writing.
	 */
	public static int fill(Pointer p, int offset, int length, int value) {
		p.setMemory(offset, length, (byte) value);
		return offset + length;
	}

	/* Support methods */

	private static Charset defaultCharset() {
		String encoding = Native.getDefaultStringEncoding();
		if (encoding == null || !Charset.isSupported(encoding)) return Charset.defaultCharset();
		return Charset.forName(encoding);
	}

}
