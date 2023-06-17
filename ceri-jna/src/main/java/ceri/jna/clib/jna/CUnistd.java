package ceri.jna.clib.jna;

import static ceri.common.collection.ArrayUtil.validateRange;
import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import java.util.Set;
import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.jna.util.JnaSize;
import ceri.jna.util.JnaUtil;

/**
 * Types and functions from {@code <unistd.h>}
 */
public class CUnistd {
	private static final Set<Integer> NONBLOCK_ERRORS =
		CError.codes(CError.EAGAIN, CError.EWOULDBLOCK, CError.EINTR);
	public static final int STDIN_FILENO = 0;
	public static final int STDOUT_FILENO = 1;
	public static final int STDERR_FILENO = 2;
	/* Constants from <stdio.h> */
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	private CUnistd() {}

	@SuppressWarnings("serial")
	public static class size_t extends IntegerType {
		public size_t() {
			this(0);
		}

		public size_t(long value) {
			super(JnaSize.SIZE_T.size, value, true);
		}
	}

	@SuppressWarnings("serial")
	public static class ssize_t extends IntegerType {
		public ssize_t() {
			this(0);
		}

		public ssize_t(long value) {
			super(JnaSize.SIZE_T.size, value);
		}
	}

	/**
	 * Closes the file descriptor.
	 */
	public static void close(int fd) throws CException {
		if (fd >= 0) caller.verify(() -> lib().close(fd), "close", fd);
	}

	/**
	 * Closes file descriptors without throwing an exception. Returns true if successful, false if
	 * any errors occurred.
	 */
	public static boolean closeSilently(int... fds) {
		boolean closed = true;
		for (int fd : fds)
			try {
				close(fd);
			} catch (CException e) {
				closed = false;
			}
		return closed;
	}

	/**
	 * Tests whether a file descriptor refers to a terminal
	 */
	public static boolean isatty(int fd) throws CException {
		return caller.verifyInt(() -> lib().isatty(fd), "isatty", fd) == 1;
	}

	/**
	 * Creates a unidirectional data channel. The returned array contains the read [0] and write [1]
	 * fds for the pipe. Data written to the pipe is buffered by the kernel until read.
	 */
	public static int[] pipe() throws CException {
		int[] pipefd = new int[2];
		caller.verify(() -> lib().pipe(pipefd), "pipe", pipefd);
		return pipefd;
	}

	/**
	 * Reads up to length bytes into the buffer. Returns the number of bytes read, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		try {
			return caller.verifyInt(() -> lib().read(fd, buffer, new size_t(length)).intValue(),
				"read", fd, buffer, length);
		} catch (CException e) {
			if (NONBLOCK_ERRORS.contains(e.code)) return 0;
			throw e;
		}
	}

	/**
	 * Reads bytes into a new buffer and copies to the byte array. Returns the number of bytes read,
	 * or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes) throws CException {
		return read(fd, bytes, 0);
	}

	/**
	 * Reads bytes into a new buffer and copies to the byte array. Returns the number of bytes read,
	 * or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes, int offset) throws CException {
		return read(fd, bytes, offset, bytes.length - offset);
	}

	/**
	 * Reads up to length bytes into a new buffer and copies to the byte array. Returns the number
	 * of bytes read, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes, int offset, int length) throws CException {
		try (var m = JnaUtil.malloc(length)) {
			return read(fd, m, bytes, offset, length);
		}
	}

	/**
	 * Reads bytes into the buffer and copies to the byte array. Returns the number of bytes read,
	 * or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors. The length must not be larger
	 * than the buffer size.
	 */
	public static int read(int fd, Pointer buffer, byte[] bytes) throws CException {
		return read(fd, buffer, bytes, 0);
	}

	/**
	 * Reads bytes into the buffer and copies to the byte array. Returns the number of bytes read,
	 * or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors. The length must not be larger
	 * than the buffer size.
	 */
	public static int read(int fd, Pointer buffer, byte[] bytes, int offset) throws CException {
		return read(fd, buffer, bytes, offset, bytes.length - offset);
	}

	/**
	 * Reads up to length bytes into the buffer and copies to the byte array. Returns the number of
	 * bytes read, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors. The length must
	 * not be larger than the buffer size.
	 */
	public static int read(int fd, Pointer buffer, byte[] bytes, int offset, int length)
		throws CException {
		int n = read(fd, buffer, length);
		JnaUtil.read(buffer, bytes, offset, n);
		return n;
	}

	/**
	 * Reads up to length bytes into a new buffer and returns a new byte array. Returns empty array
	 * on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, int length) throws CException {
		try (var m = JnaUtil.malloc(length)) {
			return readBytes(fd, m);
		}
	}

	/**
	 * Reads bytes into the buffer and returns a new byte array. Returns empty array on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, Memory buffer) throws CException {
		return readBytes(fd, buffer, JnaUtil.intSize(buffer));
	}

	/**
	 * Reads up to length bytes into the buffer and returns a new byte array. Returns empty array on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, Pointer buffer, int length) throws CException {
		int n = read(fd, buffer, length);
		return JnaUtil.bytes(buffer, 0, n);
	}

	/**
	 * Calls read() incrementally over the buffer, until length bytes are read, or read() returns 0.
	 * May block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer buffer, int length) throws CException {
		int rem = length;
		while (rem > 0) {
			int n = read(fd, buffer, rem);
			if (n <= 0) break; // n < rem?
			rem -= n;
			buffer = buffer.share(n);
		}
		return length - rem;
	}

	/**
	 * Calls read() incrementally over a new buffer, until length bytes are read, or read() returns
	 * 0. May block without O_NONBLOCK. Returns the total bytes read as a new array.
	 */
	public static byte[] readAllBytes(int fd, int length) throws CException {
		try (var m = JnaUtil.malloc(length)) {
			return readAllBytes(fd, m);
		}
	}

	/**
	 * Calls read() incrementally over the buffer, until full, or read() returns 0. May block
	 * without O_NONBLOCK. Returns the total bytes read as a new array.
	 */
	public static byte[] readAllBytes(int fd, Memory buffer) throws CException {
		return readAllBytes(fd, buffer, JnaUtil.intSize(buffer));
	}

	/**
	 * Calls read() incrementally over the buffer, until length bytes are read, or read() returns 0.
	 * May block without O_NONBLOCK. Returns the total bytes read as a new array.
	 */
	public static byte[] readAllBytes(int fd, Pointer buffer, int length) throws CException {
		int n = readAll(fd, buffer, length);
		return JnaUtil.bytes(buffer, 0, n);
	}

	/**
	 * Calls read() incrementally over a new buffer, until all bytes are read, or read() returns 0.
	 * Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns the total
	 * number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes) throws CException {
		return readAll(fd, bytes, 0);
	}

	/**
	 * Calls read() incrementally over a new buffer, until all bytes are read, or read() returns 0.
	 * Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns the total
	 * number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes, int offset) throws CException {
		return readAll(fd, bytes, offset, bytes.length - offset);
	}

	/**
	 * Calls read() incrementally over a new buffer, until length bytes are read, or read() returns
	 * 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns the total
	 * number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes, int offset, int length) throws CException {
		validateRange(bytes.length, offset, length);
		try (Memory m = JnaUtil.malloc(length)) {
			return readAll(fd, m, length, bytes, offset, length);
		}
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Memory buffer, byte[] bytes) throws CException {
		return readAll(fd, buffer, bytes, 0);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Memory buffer, byte[] bytes, int offset) throws CException {
		return readAll(fd, buffer, bytes, offset, bytes.length - offset);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until length bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Memory buffer, byte[] bytes, int offset, int length)
		throws CException {
		return readAll(fd, buffer, JnaUtil.intSize(buffer), bytes, offset, length);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer buffer, int size, byte[] bytes) throws CException {
		return readAll(fd, buffer, size, bytes, 0);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer buffer, int size, byte[] bytes, int offset)
		throws CException {
		return readAll(fd, buffer, size, bytes, offset, bytes.length - offset);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until length bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer buffer, int size, byte[] bytes, int offset,
		int length) throws CException {
		validateRange(bytes.length, offset, length);
		int rem = length;
		while (rem > 0) {
			int n = Math.min(rem, size);
			int m = readAll(fd, buffer, n);
			JnaUtil.read(buffer, bytes, offset, m);
			offset += m;
			rem -= m;
			if (m < n) break;
		}
		return length - rem;
	}

	/**
	 * Writes up to length bytes from the buffer. Returns the number of bytes written, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, Pointer buffer, int length) throws CException {
		if (length == 0) return 0;
		try {
			return caller.verifyInt(() -> lib().write(fd, buffer, new size_t(length)).intValue(),
				"write", fd, buffer, length);
		} catch (CException e) {
			if (NONBLOCK_ERRORS.contains(e.code)) return 0;
			throw e;
		}
	}

	/**
	 * Copies bytes from the array to a new buffer, and writes bytes from the buffer. Returns the
	 * number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, int... bytes) throws CException {
		return write(fd, ArrayUtil.bytes(bytes));
	}

	/**
	 * Copies bytes from the array to a new buffer, and writes bytes from the buffer. Returns the
	 * number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes) throws CException {
		return write(fd, bytes, 0);
	}

	/**
	 * Copies bytes from the array to a new buffer, and writes bytes from the buffer. Returns the
	 * number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes, int offset) throws CException {
		return write(fd, bytes, offset, bytes.length - offset);
	}

	/**
	 * Copies bytes from the array to a new buffer, and writes up to length bytes from the buffer.
	 * Returns the number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR
	 * errors.
	 */
	public static int write(int fd, byte[] bytes, int offset, int length) throws CException {
		try (var m = JnaUtil.malloc(length)) {
			return write(fd, m, bytes, offset, length);
		}
	}

	/**
	 * Copies bytes from the array to the buffer, and writes bytes from the buffer. The length must
	 * not be larger than the buffer size. Returns the number of bytes written, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, Pointer buffer, byte[] bytes) throws CException {
		return write(fd, buffer, bytes, 0);
	}

	/**
	 * Copies bytes from the array to the buffer, and writes bytes from the buffer. The length must
	 * not be larger than the buffer size. Returns the number of bytes written, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, Pointer buffer, byte[] bytes, int offset) throws CException {
		return write(fd, buffer, bytes, offset, bytes.length - offset);
	}

	/**
	 * Copies bytes from the array to the buffer, and writes up to length bytes from the buffer. The
	 * length must not be larger than the buffer size. Returns the number of bytes written, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, Pointer buffer, byte[] bytes, int offset, int length)
		throws CException {
		JnaUtil.write(buffer, bytes, offset, length);
		return write(fd, buffer, length);
	}

	/**
	 * Calls write() incrementally over the buffer, until length bytes are written, or write()
	 * returns 0. May block without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Pointer buffer, int length) throws CException {
		int rem = length;
		while (rem > 0) {
			int n = write(fd, buffer, rem);
			if (n <= 0) break;
			rem -= n;
			buffer = buffer.share(n);
		}
		return length - rem;
	}

	/**
	 * Copies bytes from the array to a new buffer, and calls write() incrementally over the buffer
	 * until all bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns the
	 * total number of bytes written.
	 */
	public static int writeAll(int fd, int... bytes) throws CException {
		return writeAll(fd, ArrayUtil.bytes(bytes));
	}

	/**
	 * Copies bytes from the array to a new buffer, and calls write() incrementally over the buffer
	 * until all bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns the
	 * total number of bytes written.
	 */
	public static int writeAll(int fd, byte[] bytes) throws CException {
		return writeAll(fd, bytes, 0);
	}

	/**
	 * Copies bytes from the array to a new buffer, and calls write() incrementally over the buffer
	 * until all bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns the
	 * total number of bytes written.
	 */
	public static int writeAll(int fd, byte[] bytes, int offset) throws CException {
		return writeAll(fd, bytes, offset, bytes.length - offset);
	}

	/**
	 * Copies bytes from the array to a new buffer, and calls write() incrementally over the buffer
	 * until length bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns
	 * the total number of bytes written.
	 */
	public static int writeAll(int fd, byte[] bytes, int offset, int length) throws CException {
		validateRange(bytes.length, offset, length);
		try (Memory m = JnaUtil.mallocBytes(bytes, offset, length)) {
			return writeAll(fd, m, length);
		}
	}

	/**
	 * Copies bytes from the array to the buffer in batches, calling write() incrementally over the
	 * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	 * Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Memory buffer, byte[] bytes) throws CException {
		return writeAll(fd, buffer, bytes, 0);
	}

	/**
	 * Copies bytes from the array to the buffer in batches, calling write() incrementally over the
	 * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	 * Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Memory buffer, byte[] bytes, int offset) throws CException {
		return writeAll(fd, buffer, bytes, offset, bytes.length - offset);
	}

	/**
	 * Copies bytes from the array to the buffer in batches, calling write() incrementally over the
	 * buffer until length bytes are written, or write() returns 0. May block without O_NONBLOCK.
	 * Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Memory buffer, byte[] bytes, int offset, int length)
		throws CException {
		return writeAll(fd, buffer, JnaUtil.intSize(buffer), bytes, offset, length);
	}

	/**
	 * Copies bytes from the array to the buffer in batches, calling write() incrementally over the
	 * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	 * Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Pointer buffer, int size, byte[] bytes) throws CException {
		return writeAll(fd, buffer, size, bytes, 0);
	}

	/**
	 * Copies bytes from the array to the buffer in batches, calling write() incrementally over the
	 * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	 * Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Pointer buffer, int size, byte[] bytes, int offset)
		throws CException {
		return writeAll(fd, buffer, size, bytes, offset, bytes.length - offset);
	}

	/**
	 * Copies bytes from the array to the buffer in batches, calling write() incrementally over the
	 * buffer until length bytes are written, or write() returns 0. May block without O_NONBLOCK.
	 * Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, Pointer buffer, int size, byte[] bytes, int offset,
		int length) throws CException {
		validateRange(bytes.length, offset, length);
		int rem = length;
		while (rem > 0) {
			int n = Math.min(rem, size);
			JnaUtil.write(buffer, bytes, offset, n);
			int m = writeAll(fd, buffer, n);
			offset += m;
			rem -= m;
			if (m < n) break;
		}
		return length - rem;
	}

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return caller.verifyInt(() -> lib().lseek(fd, offset, whence), "lseek", fd, offset, whence);
	}

	/**
	 * Returns the current position in the file.
	 */
	public static int position(int fd) throws CException {
		return lseek(fd, 0, SEEK_CUR);
	}

	/**
	 * Sets the current position in the file.
	 */
	public static void position(int fd, int position) throws CException {
		int n = lseek(fd, position, SEEK_SET);
		if (n != position)
			throw CException.general("Unable to set position on %d: %d", fd, position);
	}

	/**
	 * Returns the file size by moving to end of file then back to original position.
	 */
	public static int size(int fd) throws CException {
		int pos = position(fd);
		int size = lseek(fd, 0, SEEK_END);
		position(fd, pos);
		return size;
	}

}
