package ceri.serial.clib.jna;

import java.nio.ByteBuffer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.text.StringUtil;

/**
 * Utilities for CLib and CLib-style calls.
 */
public class CUtil {
	private static final int MEMMOVE_OPTIMAL_MAX_SIZE = 8 * 1024; // determined from test results
	public static final int INVALID_FD = -1;

	private CUtil() {}

	/**
	 * Checks function result, throwing a CException if negative, and converts any
	 * LastErrorException to CException.
	 */
	public static int verifyErrno(IntSupplier function, String format, Object... args)
		throws CException {
		return verifyErrno(function, () -> StringUtil.format(format, args));
	}

	/**
	 * Checks function result, throwing a CException if negative, and converts any
	 * LastErrorException to CException.
	 */
	public static int verifyErrno(IntSupplier function, Supplier<String> messageSupplier)
		throws CException {
		try {
			int result = function.getAsInt();
			if (result >= 0) return result;
			throw CException.full(result, messageSupplier.get());
		} catch (LastErrorException e) {
			throw CException.from(e, messageSupplier.get());
		}
	}

	/**
	 * Checks function result, and throws exception if negative
	 */
	@Deprecated // ?
	public static int verify(int result) throws CException {
		return verify(result, "call");
	}

	/**
	 * Checks function result, and throws exception if negative
	 */
	@Deprecated // ?
	public static int verify(int result, String name) throws CException {
		return verify(result, () -> name);
	}

	/**
	 * Checks function result, and throws exception if negative
	 */
	@Deprecated // ?
	public static int verify(int result, Supplier<String> nameSupplier) throws CException {
		if (result >= 0) return result;
		throw CException.full(result, "JNA %s failed", nameSupplier.get());
	}

	/**
	 * Checks function result, and throws exception if negative
	 */
	@Deprecated // ?
	public static int verifyf(int result, String format, Object... params) throws CException {
		if (result >= 0) return result;
		throw CException.full(result, format, params);
	}

	/**
	 * Validates a file descriptor
	 */
	public static int validateFd(int fd) throws CException {
		if (isValidFd(fd)) return fd;
		throw CException.full(fd, "Invalid file descriptor");
	}

	/**
	 * Checks a file descriptor validity
	 */
	public static boolean isValidFd(int fd) {
		return fd != INVALID_FD;
	}

	/**
	 * Allocate a contiguous array, returning the pointers.
	 */
	public static Pointer[] mallocArray(int size, int count) {
		if (count == 0) return new Pointer[0];
		Memory m = new Memory(size * count);
		Pointer[] ps = new Pointer[count];
		for (int i = 0; i < count; i++)
			ps[i] = m.share(size * i);
		return ps;
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory malloc(int... array) {
		return malloc(ArrayUtil.bytes(array));
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory malloc(byte[] array) {
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
	 * Copies byte array from pointer offset to pointer offset. Faster than memmove only for large
	 * sizes (>8k depending on system).
	 */
	public static int memcpy(Pointer p, long toOffset, long fromOffset, int size) {
		return memcpy(p, toOffset, p, fromOffset, size);
	}

	/**
	 * Copies byte array from pointer offset to pointer offset. Faster than memmove only for large
	 * sizes (>8k depending on system). Returns the number of bytes copied.
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

}
