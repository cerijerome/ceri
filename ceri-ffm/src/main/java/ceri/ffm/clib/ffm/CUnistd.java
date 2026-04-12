package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Set;
import ceri.common.util.Validate;
import ceri.ffm.core.Memory;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.IntType.size_t;

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
		if (fd >= 0) CLib.caller.callInt(c -> c.lastError(c.lib().close(fd), -1), "close", fd);
	}

	/**
	 * Closes file descriptors without throwing an exception. Returns true if successful, false if
	 * any errors occurred.
	 */
	public static boolean closeSilently(int... fds) {
		boolean closed = true;
		for (int fd : fds)
			if (CLib.lib().close(fd) < 0) closed = false;
		return closed;
	}

	/**
	 * Tests whether a file descriptor refers to a terminal.
	 */
	public static boolean isatty(int fd) throws CException {
		// return caller.verifyInt(() -> lib().isatty(fd), "isatty", fd) == 1;
		return CLib.caller.callInt(c -> c.lib().isatty(fd), "isatty", fd) == 1;
	}

	/**
	 * Returns the number of bytes in a memory allocation block for mmap.
	 */
	public static int getpagesize() throws CException {
		// return caller.verifyInt(() -> lib().getpagesize(), "getpagesize");
		return CLib.caller.callInt(c -> c.lib().getpagesize(), "getpagesize");
	}

	/**
	 * Creates a unidirectional data channel. The returned array contains the read [0] and write [1]
	 * fds for the pipe. Data written to the pipe is buffered by the kernel until read.
	 */
	public static int[] pipe() throws CException {
		int[] pipefd = new int[2];
		// caller.verify(() -> lib().pipe(pipefd), "pipe", pipefd);
		CLib.caller.callInt(c -> c.lastError(c.lib().pipe(pipefd), -1), "pipe", pipefd);
		return pipefd;
	}

	/**
	 * Reads up to length bytes into the buffer. Returns the number of bytes read, or 0 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, Pointer<?> buffer, int length) throws CException {
		if (length == 0) return 0;
		return CLib.caller.callInt(c -> {
			int result = c.lib().read(fd, buffer, new size_t(length)).intValue();
			if (result != -1) return result;
			int code = c.lastErrorCode();
			if (!NONBLOCK_ERRORS.contains(code)) c.fail(code);
			return 0;
		}, "read", fd, buffer, length);
		// try {
		// return caller.verifyInt(() -> lib().read(fd, buffer, size_t.of(length)).getInt(),
		// "read", fd, buffer, length);
		// } catch (CException e) {
		// if (NONBLOCK_ERRORS.contains(e.code)) return 0;
		// throw e;
		// }
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
		try (var arena = Arena.ofConfined()) {
			return read(fd, Pointer.of(arena.allocate(length)), bytes, offset, length);
		}
	}

	/**
	 * Reads bytes into the buffer and copies to the byte array. Returns the number of bytes read,
	 * or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors. The length must not be larger
	 * than the buffer size.
	 */
	public static int read(int fd, Pointer<?> buffer, byte[] bytes) throws CException {
		return read(fd, buffer, bytes, 0);
	}

	/**
	 * Reads bytes into the buffer and copies to the byte array. Returns the number of bytes read,
	 * or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors. The length must not be larger
	 * than the buffer size.
	 */
	public static int read(int fd, Pointer<?> buffer, byte[] bytes, int offset) throws CException {
		return read(fd, buffer, bytes, offset, bytes.length - offset);
	}

	/**
	 * Reads up to length bytes into the buffer and copies to the byte array. Returns the number of
	 * bytes read, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors. The length must
	 * not be larger than the buffer size.
	 */
	public static int read(int fd, Pointer<?> buffer, byte[] bytes, int offset, int length)
		throws CException {
		int n = read(fd, buffer, length);
		Primitive.BYTE.readArray(buffer.memory(), 0, Integer.MAX_VALUE, bytes, offset, n, false);
		return n;
	}

	/**
	 * Reads up to length bytes into a new buffer and returns a new byte array. Returns empty array
	 * on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, int length) throws CException {
		try (var arena = Arena.ofConfined()) {
			return readBytes(fd, arena.allocate(length));
		}
	}

	/**
	 * Reads bytes into the buffer and returns a new byte array. Returns empty array on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, MemorySegment buffer) throws CException {
		return readBytes(fd, Pointer.of(buffer), Memory.sizeInt(buffer));
	}

	/**
	 * Reads up to length bytes into the buffer and returns a new byte array. Returns empty array on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, Pointer<?> buffer, int length) throws CException {
		int n = read(fd, buffer, length);
		return Primitive.BYTE.getArray(buffer.memory(), 0, n, false);
	}

	/**
	 * Calls read() incrementally over the buffer, until length bytes are read, or read() returns 0.
	 * May block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer<?> buffer, int length) throws CException {
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
		try (var arena = Arena.ofConfined()) {
			return readAllBytes(fd, arena.allocate(length));
		}
	}

	/**
	 * Calls read() incrementally over the buffer, until full, or read() returns 0. May block
	 * without O_NONBLOCK. Returns the total bytes read as a new array.
	 */
	public static byte[] readAllBytes(int fd, MemorySegment buffer) throws CException {
		return readAllBytes(fd, Pointer.of(buffer), Memory.sizeInt(buffer));
	}

	/**
	 * Calls read() incrementally over the buffer, until length bytes are read, or read() returns 0.
	 * May block without O_NONBLOCK. Returns the total bytes read as a new array.
	 */
	public static byte[] readAllBytes(int fd, Pointer<?> buffer, int length) throws CException {
		int n = readAll(fd, buffer, length);
		return Primitive.BYTE.getArray(buffer.memory(), 0, n, false);
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
		Validate.slice(bytes.length, offset, length);
		try (var arena = Arena.ofConfined()) {
			return readAll(fd, Pointer.of(arena.allocate(length)), length, bytes, offset, length);
		}
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, MemorySegment buffer, byte[] bytes) throws CException {
		return readAll(fd, buffer, bytes, 0);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, MemorySegment buffer, byte[] bytes, int offset)
		throws CException {
		return readAll(fd, buffer, bytes, offset, bytes.length - offset);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until length bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, MemorySegment buffer, byte[] bytes, int offset, int length)
		throws CException {
		return readAll(fd, Pointer.of(buffer), Memory.sizeInt(buffer), bytes, offset, length);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer<?> buffer, int size, byte[] bytes) throws CException {
		return readAll(fd, buffer, size, bytes, 0);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until all bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer<?> buffer, int size, byte[] bytes, int offset)
		throws CException {
		return readAll(fd, buffer, size, bytes, offset, bytes.length - offset);
	}

	/**
	 * Calls read() in batches incrementally over the buffer, until length bytes are read, or read()
	 * returns 0. Copies bytes from the buffer to the array. May block without O_NONBLOCK. Returns
	 * the total number of bytes read.
	 */
	public static int readAll(int fd, Pointer<?> buffer, int size, byte[] bytes, int offset,
		int length) throws CException {
		Validate.slice(bytes.length, offset, length);
		int rem = length;
		while (rem > 0) {
			int n = Math.min(rem, size);
			int m = readAll(fd, buffer, n);
			Primitive.BYTE.readArray(buffer.memory(), 0, Integer.MAX_VALUE, bytes, offset, m,
				false);
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
	public static int write(int fd, Pointer<?> buffer, int length) throws CException {
		if (length == 0) return 0;
		return CLib.caller.callInt(c -> {
			int result = c.lib().write(fd, buffer, new size_t(length)).intValue();
			if (result != -1) return result;
			int code = c.lastErrorCode();
			if (!NONBLOCK_ERRORS.contains(code)) c.fail(code);
			return 0;
		}, "write", fd, buffer, length);
	}
	
	// /**
	// * Writes up to length bytes from the buffer. Returns the number of bytes written, or 0 on
	// * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, Pointer buffer, int length) throws CException {
	// if (length == 0) return 0;
	// try {
	// return caller.verifyInt(() -> lib().write(fd, buffer, new size_t(length)).intValue(),
	// "write", fd, buffer, length);
	// } catch (CException e) {
	// if (NONBLOCK_ERRORS.contains(e.code)) return 0;
	// throw e;
	// }
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and writes bytes from the buffer. Returns the
	// * number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, int... bytes) throws CException {
	// return write(fd, Array.bytes.of(bytes));
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and writes bytes from the buffer. Returns the
	// * number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, byte[] bytes) throws CException {
	// return write(fd, bytes, 0);
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and writes bytes from the buffer. Returns the
	// * number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, byte[] bytes, int offset) throws CException {
	// return write(fd, bytes, offset, bytes.length - offset);
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and writes up to length bytes from the buffer.
	// * Returns the number of bytes written, or 0 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR
	// * errors.
	// */
	// public static int write(int fd, byte[] bytes, int offset, int length) throws CException {
	// try (var m = Jna.malloc(length)) {
	// return write(fd, m, bytes, offset, length);
	// }
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer, and writes bytes from the buffer. The length
	// must
	// * not be larger than the buffer size. Returns the number of bytes written, or 0 on
	// * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, Pointer buffer, byte[] bytes) throws CException {
	// return write(fd, buffer, bytes, 0);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer, and writes bytes from the buffer. The length
	// must
	// * not be larger than the buffer size. Returns the number of bytes written, or 0 on
	// * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, Pointer buffer, byte[] bytes, int offset) throws CException {
	// return write(fd, buffer, bytes, offset, bytes.length - offset);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer, and writes up to length bytes from the buffer.
	// The
	// * length must not be larger than the buffer size. Returns the number of bytes written, or 0
	// on
	// * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	// */
	// public static int write(int fd, Pointer buffer, byte[] bytes, int offset, int length)
	// throws CException {
	// Jna.write(buffer, bytes, offset, length);
	// return write(fd, buffer, length);
	// }
	//
	// /**
	// * Calls write() incrementally over the buffer, until length bytes are written, or write()
	// * returns 0. May block without O_NONBLOCK. Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Pointer buffer, int length) throws CException {
	// int rem = length;
	// while (rem > 0) {
	// int n = write(fd, buffer, rem);
	// if (n <= 0) break;
	// rem -= n;
	// buffer = buffer.share(n);
	// }
	// return length - rem;
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and calls write() incrementally over the
	// buffer
	// * until all bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns
	// the
	// * total number of bytes written.
	// */
	// public static int writeAll(int fd, int... bytes) throws CException {
	// return writeAll(fd, Array.bytes.of(bytes));
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and calls write() incrementally over the
	// buffer
	// * until all bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns
	// the
	// * total number of bytes written.
	// */
	// public static int writeAll(int fd, byte[] bytes) throws CException {
	// return writeAll(fd, bytes, 0);
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and calls write() incrementally over the
	// buffer
	// * until all bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns
	// the
	// * total number of bytes written.
	// */
	// public static int writeAll(int fd, byte[] bytes, int offset) throws CException {
	// return writeAll(fd, bytes, offset, bytes.length - offset);
	// }
	//
	// /**
	// * Copies bytes from the array to a new buffer, and calls write() incrementally over the
	// buffer
	// * until length bytes are written, or write() returns 0. May block without O_NONBLOCK. Returns
	// * the total number of bytes written.
	// */
	// public static int writeAll(int fd, byte[] bytes, int offset, int length) throws CException {
	// Validate.slice(bytes.length, offset, length);
	// try (Memory m = Jna.mallocBytes(bytes, offset, length)) {
	// return writeAll(fd, m, length);
	// }
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer in batches, calling write() incrementally over
	// the
	// * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	// * Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Memory buffer, byte[] bytes) throws CException {
	// return writeAll(fd, buffer, bytes, 0);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer in batches, calling write() incrementally over
	// the
	// * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	// * Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Memory buffer, byte[] bytes, int offset) throws CException
	// {
	// return writeAll(fd, buffer, bytes, offset, bytes.length - offset);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer in batches, calling write() incrementally over
	// the
	// * buffer until length bytes are written, or write() returns 0. May block without O_NONBLOCK.
	// * Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Memory buffer, byte[] bytes, int offset, int length)
	// throws CException {
	// return writeAll(fd, buffer, Jna.intSize(buffer), bytes, offset, length);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer in batches, calling write() incrementally over
	// the
	// * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	// * Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Pointer buffer, int size, byte[] bytes) throws CException
	// {
	// return writeAll(fd, buffer, size, bytes, 0);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer in batches, calling write() incrementally over
	// the
	// * buffer until all bytes are written, or write() returns 0. May block without O_NONBLOCK.
	// * Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Pointer buffer, int size, byte[] bytes, int offset)
	// throws CException {
	// return writeAll(fd, buffer, size, bytes, offset, bytes.length - offset);
	// }
	//
	// /**
	// * Copies bytes from the array to the buffer in batches, calling write() incrementally over
	// the
	// * buffer until length bytes are written, or write() returns 0. May block without O_NONBLOCK.
	// * Returns the total number of bytes written.
	// */
	// public static int writeAll(int fd, Pointer buffer, int size, byte[] bytes, int offset,
	// int length) throws CException {
	// Validate.slice(bytes.length, offset, length);
	// int rem = length;
	// while (rem > 0) {
	// int n = Math.min(rem, size);
	// Jna.write(buffer, bytes, offset, n);
	// int m = writeAll(fd, buffer, n);
	// offset += m;
	// rem -= m;
	// if (m < n) break;
	// }
	// return length - rem;
	// }

	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static int lseek(int fd, int offset, int whence) throws CException {
		return CLib.caller.callInt(c -> c.lastError(c.lib().lseek(fd, offset, whence), -1), "lseek",
			fd, offset, whence);
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
