package ceri.serial.clib.jna;

import com.sun.jna.Memory;
import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.Seek;
import ceri.serial.jna.JnaUtil;

/**
 * Utilities for CLib.
 */
public class CLibUtil {
	public static final int INVALID_FD = -1;

	private CLibUtil() {}

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
		Memory m = JnaUtil.mallocBytes(bytes, offset, length);
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

}
