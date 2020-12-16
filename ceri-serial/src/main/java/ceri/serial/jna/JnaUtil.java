package ceri.serial.jna;

import static ceri.common.collection.ArrayUtil.validateSlice;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.TypeTranscoder;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionSupplier;
import ceri.common.math.MathUtil;
import ceri.common.util.ValueCache;

public class JnaUtil {
	public static final Charset DEFAULT_CHARSET = defaultCharset();
	public static final boolean NLONG_LONG = NativeLong.SIZE == Long.BYTES;
	private static final NativeLong ZERO = new NativeLong(0);

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
	 * Enum type transcoder filtering values -1, which signifies value is not available for the OS.
	 */
	public static <T extends Enum<T>> TypeTranscoder<T> xcoder(ToIntFunction<T> valueFn,
		Class<T> cls, IntFunction<T[]> arrayFn) {
		return TypeTranscoder.of(valueFn,
			StreamUtil.stream(cls).filter(t -> valueFn.applyAsInt(t) != -1).toArray(arrayFn));
	}

	/**
	 * Printable representation of structure reference.
	 */
	public static <T extends Structure & Structure.ByReference> String print(T t) {
		if (t == null) return String.valueOf(t);
		return String.format("%s@%x+%x", t.getClass().getSimpleName(),
			Pointer.nativeValue(t.getPointer()), t.size());
	}

	/**
	 * Printable representation of pointer.
	 */
	public static String print(Pointer p) {
		if (p == null) return String.valueOf(p);
		if (p instanceof Memory) return print((Memory) p);
		return String.format("@%x", Pointer.nativeValue(p));
	}

	/**
	 * Printable representation of memory pointer.
	 */
	public static String print(Memory m) {
		if (m == null) return String.valueOf(m);
		return String.format("@%x+%x", Pointer.nativeValue(m), m.size());
	}

	/**
	 * A lazy buffer, that only allocates memory when needed.
	 */
	public static ExceptionSupplier<RuntimeException, Memory> lazyBuffer(long size) {
		return ValueCache.of(() -> new Memory(size));
	}

	/**
	 * Returns a pointer from the native peer. Use with caution.
	 */
	public static Pointer pointer(long peer) {
		return peer == 0L ? null : new Pointer(peer);
	}

	/**
	 * Returns a pointer from the native peer. Use with caution.
	 */
	public static Pointer pointer(Structure t) {
		return t == null ? null : t.getPointer();
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
	 * Executes a function that takes a byte pointer. Returns the value after execution.
	 */
	public static <E extends Exception> byte execByte(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execByte(0, consumer);
	}

	/**
	 * Executes a function that takes a byte pointer. Returns the value after execution.
	 */
	public static <E extends Exception> byte execByte(int value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		ByteByReference ref = new ByteByReference((byte) value);
		consumer.accept(ref.getPointer());
		return ref.getValue();
	}

	/**
	 * Executes a function that takes a byte pointer. Returns the unsigned value after execution.
	 */
	public static <E extends Exception> short execUbyte(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execUbyte(0, consumer);
	}

	/**
	 * Executes a function that takes a byte pointer. Returns the unsigned value after execution.
	 */
	public static <E extends Exception> short execUbyte(int value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		return MathUtil.ubyte(execByte(value, consumer));
	}

	/**
	 * Executes a function that takes a short pointer. Returns the value after execution.
	 */
	public static <E extends Exception> short execShort(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execShort(0, consumer);
	}

	/**
	 * Executes a function that takes a short pointer. Returns the value after execution.
	 */
	public static <E extends Exception> short execShort(int value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		ShortByReference ref = new ShortByReference((short) value);
		consumer.accept(ref.getPointer());
		return ref.getValue();
	}

	/**
	 * Executes a function that takes a short pointer. Returns the unsigned value after execution.
	 */
	public static <E extends Exception> int execUshort(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execUshort(0, consumer);
	}

	/**
	 * Executes a function that takes a short pointer. Returns the unsigned value after execution.
	 */
	public static <E extends Exception> int execUshort(int value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		return MathUtil.ushort(execShort(value, consumer));
	}

	/**
	 * Executes a function that takes an int pointer. Returns the value after execution.
	 */
	public static <E extends Exception> int execInt(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execInt(0, consumer);
	}

	/**
	 * Executes a function that takes an int pointer. Returns the value after execution.
	 */
	public static <E extends Exception> int execInt(int value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		IntByReference ref = new IntByReference(value);
		consumer.accept(ref.getPointer());
		return ref.getValue();
	}

	/**
	 * Executes a function that takes an int pointer. Returns the unsigned value after execution.
	 */
	public static <E extends Exception> long execUint(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execUint(0, consumer);
	}

	/**
	 * Executes a function that takes an int pointer. Returns the unsigned value after execution.
	 */
	public static <E extends Exception> long execUint(int value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		return MathUtil.uint(execInt(value, consumer));
	}

	/**
	 * Executes a function that takes a native long pointer. Returns the value after execution.
	 */
	public static <E extends Exception> NativeLong execNlong(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execNlong(ZERO, consumer);
	}

	/**
	 * Executes a function that takes a native long pointer. Returns the value after execution.
	 */
	public static <E extends Exception> NativeLong execNlong(NativeLong value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		NativeLongByReference ref = new NativeLongByReference(value);
		consumer.accept(ref.getPointer());
		return ref.getValue();
	}

	/**
	 * Executes a function that takes a native long pointer. Returns the unsigned value after
	 * execution.
	 */
	public static <E extends Exception> long execUnlong(NativeLong value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		NativeLongByReference ref = new NativeLongByReference(value);
		consumer.accept(ref.getPointer());
		return unlong(ref.getValue());
	}

	/**
	 * Executes a function that takes a long pointer. Returns the value after execution.
	 */
	public static <E extends Exception> long execLong(ExceptionConsumer<E, Pointer> consumer)
		throws E {
		return execLong(0, consumer);
	}

	/**
	 * Executes a function that takes a long pointer. Returns the value after execution.
	 */
	public static <E extends Exception> long execLong(long value,
		ExceptionConsumer<E, Pointer> consumer) throws E {
		LongByReference ref = new LongByReference(value);
		consumer.accept(ref.getPointer());
		return ref.getValue();
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
		return nlongRef(value).getPointer();
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
	 * Creates an array of bytes from the given memory pointer.
	 */
	public static byte[] byteArray(Memory m) {
		return byteArray(m, 0);
	}

	/**
	 * Creates an array of bytes from the given memory pointer.
	 */
	private static byte[] byteArray(Memory m, long offset) {
		return byteArray(m, offset, Math.toIntExact(m.size() - offset));
	}

	/**
	 * Creates an array of bytes from the given memory pointer.
	 */
	public static byte[] byteArray(Pointer p, long offset, long length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		if (p == null) throw new IllegalArgumentException("Pointer is null");
		return p.getByteArray(offset, Math.toIntExact(length));
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] byteArray(ByteBuffer buffer) {
		return byteArray(buffer, 0);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] byteArray(ByteBuffer buffer, int position) {
		return byteArray(buffer, position, buffer.limit() - position);
	}

	/**
	 * Extracts bytes from buffer.
	 */
	public static byte[] byteArray(ByteBuffer buffer, int position, int length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
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
		if (p == null || length == 0) return ByteBuffer.allocate(0);
		return p.getByteBuffer(offset, length);
	}

	/**
	 * Decodes string from fixed-length bytes at pointer using default charset.
	 */
	public static String string(Memory m) {
		return string(DEFAULT_CHARSET, m);
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
		byte[] bytes = byteArray(p, offset, length);
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
	 * Copies bytes to the pointer. Returns the pointer offset after writing.
	 */
	public static int write(Pointer p, int... buffer) {
		return write(p, ArrayUtil.bytes(buffer));
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

	/* Support methods */

	private static Charset defaultCharset() {
		String encoding = Native.getDefaultStringEncoding();
		if (encoding == null || !Charset.isSupported(encoding)) return Charset.defaultCharset();
		return Charset.forName(encoding);
	}

}
