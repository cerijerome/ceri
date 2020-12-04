package ceri.serial.clib.jna;

import java.nio.ByteBuffer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.text.StringUtil;
import ceri.serial.clib.Seek;
import ceri.serial.jna.JnaUtil;

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
	 * Attempt to close a file descriptor. Returns true if successful, false for any error.
	 */
	public static boolean close(int fd) {
		try {
			CLib.close(fd);
			return true;
		} catch (CException e) {
			return false;
		}
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, int... bytes) throws CException {
		write(fd, ArrayUtil.bytes(bytes));
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, byte[] bytes) throws CException {
		write(fd, bytes, 0);
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, byte[] bytes, int offset) throws CException {
		write(fd, bytes, offset, bytes.length - offset);
	}

	/**
	 * Writes bytes to file.
	 */
	public static void write(int fd, byte[] bytes, int offset, int length) throws CException {
		Memory m = malloc(bytes, offset, length);
		CLib.write(fd, m, length);
	}

	/**
	 * Reads all bytes from current position in file.
	 */
	public static byte[] readAll(int fd) throws CException {
		return read(fd, size(fd));
	}

	/**
	 * Reads up to length bytes from current position in file.
	 */
	public static byte[] read(int fd, int length) throws CException {
		Memory m = new Memory(length);
		int n = CLib.read(fd, m, length);
		if (n <= 0) return ArrayUtil.EMPTY_BYTE;
		return JnaUtil.byteArray(m, 0, n);
	}

	/**
	 * Returns the current position in the file.
	 */
	public static int position(int fd) throws CException {
		return CLib.lseek(fd, 0, Seek.SEEK_CUR.value);
	}

	/**
	 * Sets the current position in the file.
	 */
	public static void position(int fd, int position) throws CException {
		int n = CLib.lseek(fd, position, Seek.SEEK_SET.value);
		if (n != position)
			throw CException.general("Unable to set position on %d: %d", fd, position);
	}

	/**
	 * Returns the file size by moving to end of file then back to original position.
	 */
	public static int size(int fd) throws CException {
		int pos = position(fd);
		int size = CLib.lseek(fd, 0, Seek.SEEK_END.value);
		position(fd, pos);
		return size;
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
	 * Allocates new memory, or null if size is 0.
	 */
	public static Memory mallocSize(int size) {
		return size == 0 ? null : new Memory(size);
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
