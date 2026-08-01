package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Set;
import ceri.common.array.Array;
import ceri.common.function.Excepts;
import ceri.common.math.Maths;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.type.IntType.size_t;
import ceri.ffm.type.Memory;
import ceri.ffm.type.Primitive;

/**
 * Types and functions from {@code <unistd.h>}
 */
@CInclude("unistd.h")
public class CUnistd {
	private static final Set<Integer> NONBLOCK_ERRORS =
		CErrNo.codes(CErrNo.EAGAIN, CErrNo.EWOULDBLOCK, CErrNo.EINTR);
	public static final int STDIN_FILENO = 0;
	public static final int STDOUT_FILENO = 1;
	public static final int STDERR_FILENO = 2;
	// Constants from <stdio.h>
	/** From start of file. */
	public static final int SEEK_SET = 0;
	/** From current position. */
	public static final int SEEK_CUR = 1;
	/** From end of file. */
	public static final int SEEK_END = 2;

	private CUnistd() {}

	/**
	 * Closes the file descriptor.
	 */
	public static void close(int fd) throws CException {
		if (fd >= 0) CLib.caller.verifyInt(lib -> lib.close(fd), -1, "close", fd);
	}

	/**
	 * Closes file descriptors without throwing an exception. Returns true if successful, false if
	 * any errors occurred.
	 */
	public static boolean closeSilently(int... fds) {
		boolean closed = true;
		for (int fd : fds)
			if (fd >= 0 && CLib.lib().close(fd) < 0) closed = false;
		return closed;
	}

	/**
	 * Tests whether a file descriptor refers to a terminal.
	 */
	public static boolean isatty(int fd) throws CException {
		// errno set on 0 response
		return CLib.caller.callInt(c -> c.lib().isatty(fd), "isatty", fd) == 1;
	}

	/**
	 * Returns the number of bytes in a memory allocation block for mmap.
	 */
	public static int getpagesize() throws CException {
		return CLib.caller.callInt(c -> c.lib().getpagesize(), "getpagesize");
	}

	/**
	 * Creates a unidirectional data channel. The returned array contains the read [0] and write [1]
	 * fds for the pipe. Data written to the pipe is buffered by the kernel until read.
	 */
	public static int[] pipe() throws CException {
		int[] pipefd = new int[2];
		CLib.caller.verifyInt(lib -> lib.pipe(pipefd), -1, "pipe", pipefd);
		return pipefd;
	}

	/**
	 * Reads bytes into the buffer. Returns the number of bytes read, or 0 on EAGAIN/EWOULDBLOCK
	 * (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, MemorySegment buffer) throws CException {
		return read(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Reads bytes into the buffer. Returns the number of bytes read, or 0 on EAGAIN/EWOULDBLOCK
	 * (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, MemorySegment buffer, int length) throws CException {
		int n = Memory.limitInt(buffer, length);
		if (n == 0) return 0;
		return CLib.caller.callInt(c -> {
			int result = c.lib().read(fd, buffer, new size_t(n)).intValue();
			if (result != -1) return result;
			int code = c.errNo();
			if (!NONBLOCK_ERRORS.contains(code)) c.fail(code);
			return 0;
		}, "read", fd, buffer, length);
	}

	/**
	 * Reads bytes and copies to the byte array. Returns the number of bytes read, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes) throws CException {
		return read(fd, bytes, 0);
	}

	/**
	 * Reads bytes and copies to the byte array. Returns the number of bytes read, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes, int offset) throws CException {
		return read(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Reads bytes and copies to the byte array. Returns the number of bytes read, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes, int offset, int length) throws CException {
		return applySlice(bytes, offset, length, (o, l) -> {
			try (var arena = Arena.ofConfined()) {
				var buffer = arena.allocate(l);
				int n = read(fd, buffer);
				if (n > 0) Primitive.BYTE.readArray(buffer, 0, n, bytes, o, n, false);
				return n;
			}
		});
	}

	/**
	 * Reads and returns a new byte array up to specified size. Returns empty array on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, int length) throws CException {
		if (length <= 0) return Array.BYTE.empty;
		try (var arena = Arena.ofConfined()) {
			var buffer = arena.allocate(length);
			int n = read(fd, buffer);
			return Primitive.BYTE.getArray(buffer, 0, n, false);
		}
	}

	/**
	 * Calls read() incrementally until buffer is full, or read() returns 0. May block without
	 * O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, MemorySegment buffer) throws CException {
		return readAll(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Calls read() incrementally until specified number of bytes are read, or read() returns 0. May
	 * block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, MemorySegment buffer, int length) throws CException {
		length = Memory.limitInt(buffer, length);
		int rem = length;
		while (rem > 0) {
			int n = read(fd, buffer, rem);
			if (n <= 0) break; // n < rem?
			rem -= n;
			buffer = buffer.asSlice(n);
		}
		return length - rem;
	}

	/**
	 * Calls read() incrementally until specified number of bytes are read, or read() returns 0. May
	 * block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes) throws CException {
		return readAll(fd, bytes, 0);
	}

	/**
	 * Calls read() incrementally until specified number of bytes are read, or read() returns 0. May
	 * block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes, int offset) throws CException {
		return readAll(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Calls read() incrementally until specified number of bytes are read, or read() returns 0. May
	 * block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes, int offset, int length) throws CException {
		return applySlice(bytes, offset, length, (o, l) -> {
			try (var arena = Arena.ofConfined()) {
				var buffer = arena.allocate(l);
				int n = readAll(fd, buffer);
				if (n > 0) Primitive.BYTE.readArray(buffer, 0, n, bytes, o, n, false);
				return n;
			}
		});
	}

	/**
	 * Calls read() incrementally until specified number of bytes are read, or read() returns 0. May
	 * block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static byte[] readAllBytes(int fd, int length) throws CException {
		if (length <= 0) return Array.BYTE.empty;
		try (var arena = Arena.ofConfined()) {
			var buffer = arena.allocate(length);
			int n = read(fd, buffer);
			return Primitive.BYTE.getArray(buffer, 0, n, false);
		}
	}

	/**
	 * Writes bytes from the buffer, up to buffer size. Returns the number of bytes written, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, MemorySegment buffer) throws CException {
		return write(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Writes bytes from the buffer up to specified count. Returns the number of bytes written, or 0
	 * on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, MemorySegment buffer, int length) throws CException {
		int n = Memory.limitInt(buffer, length);
		if (n == 0) return 0;
		return CLib.caller.callInt(c -> {
			int result = c.lib().write(fd, buffer, new size_t(length)).intValue();
			if (result != -1) return result;
			int code = c.errNo();
			if (!NONBLOCK_ERRORS.contains(code)) c.fail(code);
			return 0;
		}, "write", fd, buffer, length);
	}

	/**
	 * Writes bytes up to specified count using a buffer. Returns the number of bytes written, or 0
	 * on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, int... bytes) throws CException {
		try (var arena = Arena.ofConfined()) {
			var buffer = Primitive.BYTE.allocAll(arena, false, bytes);
			return write(fd, buffer);
		}
	}

	/**
	 * Writes bytes up to specified count using a buffer. Returns the number of bytes written, or 0
	 * on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes) throws CException {
		return write(fd, bytes, 0);
	}

	/**
	 * Writes bytes up to specified count using a buffer. Returns the number of bytes written, or 0
	 * on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes, int offset) throws CException {
		return write(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Copies bytes from the array to a new buffer, and writes up to length bytes from the buffer.
	 * Returns the number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR
	 * errors.
	 */
	public static int write(int fd, byte[] bytes, int offset, int length) throws CException {
		try (var arena = Arena.ofConfined()) {
			var buffer = Primitive.BYTE.allocArray(arena, bytes, offset, length, false);
			return write(fd, buffer);
		}
	}

	/**
	 * Calls write() incrementally over the buffer, until all is written, or write() returns 0. May
	 * block without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, MemorySegment buffer) throws CException {
		return writeAll(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Calls write() incrementally over the buffer, until specified count is written, or write()
	 * returns 0. May block without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, MemorySegment buffer, int length) throws CException {
		length = Memory.limitInt(buffer, length);
		int rem = length;
		while (rem > 0) {
			int n = write(fd, buffer, rem);
			if (n <= 0) break; // n < rem?
			rem -= n;
			buffer = buffer.asSlice(n);
		}
		return length - rem;

	}

	/**
	 * Calls write() incrementally up to specified count using a buffer. Returns the number of bytes
	 * written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int writeAll(int fd, int... bytes) throws CException {
		try (var arena = Arena.ofConfined()) {
			var buffer = Primitive.BYTE.allocAll(arena, false, bytes);
			return writeAll(fd, buffer);
		}
	}

	/**
	 * Calls write() incrementally up to specified count using a buffer. Returns the number of bytes
	 * written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int writeAll(int fd, byte[] bytes) throws CException {
		return writeAll(fd, bytes, 0);
	}

	/**
	 * Calls write() incrementally up to specified count using a buffer. Returns the number of bytes
	 * written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int writeAll(int fd, byte[] bytes, int offset) throws CException {
		return writeAll(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Calls write() incrementally up to specified count using a buffer. Returns the number of bytes
	 * written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int writeAll(int fd, byte[] bytes, int offset, int length) throws CException {
		try (var arena = Arena.ofConfined()) {
			var buffer = Primitive.BYTE.allocArray(arena, bytes, offset, length, false);
			return writeAll(fd, buffer);
		}
	}

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return CLib.caller.verifyInt(lib -> lib.lseek(fd, offset, whence), -1, "lseek", fd, offset,
			whence);
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

	// support

	private static int applySlice(byte[] bytes, int offset, int length,
		Excepts.IntBiOperator<CException> operator) throws CException {
		if (bytes == null) return 0;
		offset = Maths.limit(offset, 0, bytes.length);
		length = Maths.limit(length, 0, bytes.length - offset);
		if (length == 0) return 0;
		return operator.applyAsInt(offset, length);
	}
}
