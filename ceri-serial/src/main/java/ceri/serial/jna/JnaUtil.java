package ceri.serial.jna;

import static ceri.common.collection.ArrayUtil.validateSlice;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionSupplier;
import ceri.common.math.MathUtil;
import ceri.common.util.ValueCache;

public class JnaUtil {
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
	 * A lazy buffer, that only allocates memory when needed.
	 */
	public static ExceptionSupplier<RuntimeException, Memory> lazyBuffer(long size) {
		return ValueCache.of(() -> new Memory(size));
	}

	/**
	 * Returns a pointer from the native peer, or null if 0. Use with caution.
	 */
	public static Pointer pointer(long peer) {
		return peer == 0L ? null : new Pointer(peer);
	}

	/**
	 * Returns the native peer for a pointer, or 0L if null. Use with caution.
	 */
	public static long peer(Pointer p) {
		return p == null ? 0L : Pointer.nativeValue(p);
	}

	/**
	 * Returns a pointer from the pointer type.
	 */
	public static Pointer pointer(PointerType pt) {
		return pt == null ? null : pt.getPointer();
	}

	/**
	 * Gets the pointer offset by the given number of bytes.
	 */
	public static Pointer offset(Pointer p, long offset) {
		validateNotNull(p);
		return offset == 0 ? p : p.share(offset);
	}

	/**
	 * Gets the memory offset by the given number of bytes.
	 */
	public static Memory offset(Memory m, long offset) {
		validateNotNull(m);
		return offset(m, offset, m.size() - offset);
	}

	/**
	 * Gets the memory offset by the given number of bytes.
	 */
	public static Memory offset(Memory m, long offset, long length) {
		validateNotNull(m);
		return offset == 0 ? m : (Memory) m.share(offset, length);
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
	 * Creates a typed array constructed from a contiguous null-terminated pointer array at the
	 * given pointer. If the pointer is null, or count is 0, an empty array is returned.
	 */
	public static <T> T[] arrayByRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor) {
		if (p == null) return arrayConstructor.apply(0);
		Pointer[] refs = p.getPointerArray(0);
		return Stream.of(refs).map(constructor).toArray(arrayConstructor);
	}

	/**
	 * Creates a typed array constructed from a contiguous pointer array at the given pointer. If
	 * the pointer is null, or count is 0, an empty array is returned. Make sure count is unsigned
	 * (call JnaUtil.ubyte/ushort if needed).
	 */
	public static <T> T[] arrayByRef(Pointer p, Function<Pointer, T> constructor,
		IntFunction<T[]> arrayConstructor, int count) {
		if (count == 0) return arrayConstructor.apply(0);
		if (p == null) throw new IllegalArgumentException("Null pointer but count > 0: " + count);
		Pointer[] refs = p.getPointerArray(0, count);
		return Stream.of(refs).map(ref -> byType(ref, constructor)).toArray(arrayConstructor);
	}

	/**
	 * Creates a type from index i of contiguous pointer array at the given pointer. If the pointer
	 * is null, null is returned.
	 */
	public static <T> T byRef(Pointer p, int i, Function<Pointer, T> constructor) {
		Pointer ref = PointerUtil.ref(p, i);
		return ref == null ? null : constructor.apply(ref);
	}

	/**
	 * Creates a type from pointer. If the pointer is null, null is returned.
	 */
	public static <T> T byType(Pointer p, Function<Pointer, T> constructor) {
		return p == null ? null : constructor.apply(p);
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
		if (position == 0 && length == buffer.limit()) return buffer.array();
		byte[] bytes = new byte[length];
		buffer.position(position).get(bytes);
		return bytes;
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
