package ceri.serial.clib.jna;

import java.nio.ByteBuffer;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.Seek;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.PointerUtil;

/**
 * Utilities for CLib and CLib-style calls.
 */
public class CUtil {
	private static final int MEMCPY_OPTIMAL_MIN_SIZE = 8 * 1024; // determined from test results
	public static final int INVALID_FD = -1;

	private CUtil() {}

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
		return JnaUtil.bytes(m, 0, n);
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
	 * Validates a file descriptor
	 */
	public static int validateFd(int fd) throws CException {
		if (validFd(fd)) return fd;
		throw CException.of(fd, "Invalid file descriptor");
	}

	/**
	 * Checks a file descriptor validity
	 */
	public static boolean validFd(int fd) {
		return fd != INVALID_FD;
	}

	/**
	 * Allocates and zeroes new memory, or returns null if size is 0.
	 */
	public static Memory calloc(int size) {
		if (size == 0) return null;
		Memory m = new Memory(size);
		JnaUtil.fill(m, 0);
		return m;
	}

	/**
	 * Allocates and zeroes a contiguous array of pointers.
	 */
	public static Pointer[] callocArray(int count) {
		return callocArray(Pointer.SIZE, count);
	}

	/**
	 * Allocates and zeroes a contiguous array, returning the pointers.
	 */
	public static Pointer[] callocArray(int size, int count) {
		if (count == 0) return new Pointer[0];
		return arrayByVal(calloc(size * count), size, count);
	}

	/**
	 * Allocate a contiguous array of pointers.
	 */
	public static Pointer[] mallocArray(int count) {
		return mallocArray(Pointer.SIZE, count);
	}

	/**
	 * Allocate a contiguous array, returning the pointers.
	 */
	public static Pointer[] mallocArray(int size, int count) {
		if (count == 0) return new Pointer[0];
		return arrayByVal(malloc(size * count), size, count);
	}

	/**
	 * Allocates new memory, or returns null if size is 0.
	 */
	public static Memory malloc(int size) {
		return size == 0 ? null : new Memory(size);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static Memory mallocBytes(int... array) {
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
		if (toOffset == fromOffset) return size;
		return memcpy(p, toOffset, p, fromOffset, size);
	}

	/**
	 * Copies bytes from pointer offset to pointer offset. Calls memmove for smaller sizes (<8k), or
	 * if regions overlap. Returns the number of bytes copied.
	 */
	public static int memcpy(Pointer to, long toOffset, Pointer from, long fromOffset, int size) {
		if (size < MEMCPY_OPTIMAL_MIN_SIZE ||
			PointerUtil.overlap(to, toOffset, from, fromOffset, size))
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
	 * Splits contiguous memory into an array of pointers.
	 */
	private static Pointer[] arrayByVal(Memory m, int size, int count) {
		Pointer[] ps = new Pointer[count];
		for (int i = 0; i < count; i++)
			ps[i] = m.share(size * i);
		return ps;
	}
}
