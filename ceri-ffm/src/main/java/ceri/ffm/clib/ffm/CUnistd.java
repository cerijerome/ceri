package ceri.ffm.clib.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import ceri.common.array.Array;
import ceri.common.data.Xcoder;
import ceri.common.function.Excepts;
import ceri.common.math.Maths;
import ceri.common.util.Validate;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.reflect.Refine.Unsigned;
import ceri.ffm.type.IntType;
import ceri.ffm.type.IntType.CLong;
import ceri.ffm.type.Memory;
import ceri.ffm.type.Primitive;

/**
 * Types and functions from {@code <unistd.h>}
 */
@CInclude("unistd.h")
public class CUnistd {
	public static final int STDIN_FILENO = 0;
	public static final int STDOUT_FILENO = 1;
	public static final int STDERR_FILENO = 2;

	private CUnistd() {}

	/**
	 * Unsigned size type.
	 */
	@Unsigned
	@Size(type = "size_t")
	public static class size_t extends IntType<size_t> {
		public static final Supporter<size_t> $ = support(size_t.class);

		public size_t(Number value) {
			super(value);
		}
	}

	/**
	 * Signed size type.
	 */
	@Size(type = "size_t")
	public static class ssize_t extends IntType<ssize_t> {
		public static final Supporter<ssize_t> $ = support(ssize_t.class);

		public ssize_t(Number value) {
			super(value);
		}
	}

	/**
	 * Constants for lseek whence from <stdio.h>
	 */
	public enum Seek {
		/** From start of file. */
		SEEK_SET(0),
		/** From current position. */
		SEEK_CUR(1),
		/** From end of file. */
		SEEK_END(2);

		public static final Xcoder.Type<Seek> xcoder = Xcoder.type(Seek.class);
		public final int value;

		private Seek(int value) {
			this.value = value;
		}
	}

	/**
	 * Closes the file descriptor. Returns false for standard fds or if interrupted.
	 */
	public static boolean close(int fd) throws CException {
		if (stdFileNo(fd)) return false;
		return CLib.caller.callInt(c -> c.verifyInt(c.lib().close(fd), -1, CErrNo.EINTR), "close",
			fd) == 0;
	}

	/**
	 * Closes file descriptors without throwing an exception. Returns true if successful, false if
	 * any errors occurred.
	 */
	public static boolean closeSilently(int... fds) {
		boolean closed = true;
		for (int fd : fds)
			if (!stdFileNo(fd) && CLib.lib().close(fd) != 0) closed = false;
		return closed;
	}

	/**
	 * Tests whether a file descriptor refers to a terminal. Fails unless true or ENOTTY.
	 */
	public static boolean isatty(int fd) throws CException {
		return CLib.caller.callInt(c -> {
			int result = c.lib().isatty(fd);
			if (result == 0) c.verify(CErrNo.ENOTTY);
			return result;
		}, "isatty", fd) == 1;
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
	 * Reads bytes into the buffer up to the buffer size. Returns the number of bytes read, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, MemorySegment buffer) throws CException {
		return read(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Reads bytes into the buffer up to the specified count. Returns the number of bytes read, or
	 * -1 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, MemorySegment buffer, int length) throws CException {
		int n = Memory.limitInt(buffer, length);
		if (n == 0) return 0;
		return CLib.caller
			.callInt(c -> c.verifyInt(c.lib().read(fd, buffer, new size_t(n)).intValue(), -1,
				CErrNo.EAGAIN, CErrNo.EWOULDBLOCK, CErrNo.EINTR), "read", fd, buffer, length);
	}

	/**
	 * Reads bytes into the array using a buffer. Returns the number of bytes read, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes) throws CException {
		return read(fd, bytes, 0);
	}

	/**
	 * Reads bytes into the array using a buffer. Returns the number of bytes read, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int read(int fd, byte[] bytes, int offset) throws CException {
		return read(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Reads bytes into the array using a buffer. Returns the number of bytes read, or -1 on
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
	 * Reads bytes into an array using a buffer. Returns an empty array on EAGAIN/EWOULDBLOCK (with
	 * O_NONBLOCK) and EINTR errors.
	 */
	public static byte[] readBytes(int fd, int length) throws CException {
		if (length <= 0) return Array.BYTE.empty;
		try (var arena = Arena.ofConfined()) {
			var buffer = arena.allocate(length);
			int n = read(fd, buffer);
			if (n <= 0) return Array.BYTE.empty;
			return Primitive.BYTE.getArray(buffer, 0, n, false);
		}
	}

	/**
	 * Calls read() incrementally over the buffer, until all or no bytes are read. May block without
	 * O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, MemorySegment buffer) throws CException {
		return readAll(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Calls read() incrementally over the buffer, until the specified count or no bytes are read.
	 * May block without O_NONBLOCK. Returns the total number of bytes read.
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
	 * Calls read() incrementally over the array using a buffer, until all or no bytes are read. May
	 * block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes) throws CException {
		return readAll(fd, bytes, 0);
	}

	/**
	 * Calls read() incrementally over the array using a buffer, until the specified count or no
	 * bytes are read. May block without O_NONBLOCK. Returns the total number of bytes read.
	 */
	public static int readAll(int fd, byte[] bytes, int offset) throws CException {
		return readAll(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Calls read() incrementally over the array using a buffer, until the specified count or no
	 * bytes are read. May block without O_NONBLOCK. Returns the total number of bytes read.
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
	 * Calls read() incrementally into an array using a buffer, until the specified count or no
	 * bytes are read. May block without O_NONBLOCK.
	 */
	public static byte[] readAllBytes(int fd, int length) throws CException {
		if (length <= 0) return Array.BYTE.empty;
		try (var arena = Arena.ofConfined()) {
			var buffer = arena.allocate(length);
			int n = readAll(fd, buffer);
			if (n <= 0) return Array.BYTE.empty;
			return Primitive.BYTE.getArray(buffer, 0, n, false);
		}
	}

	/**
	 * Writes bytes from the buffer, up to the buffer size. Returns the number of bytes written, or
	 * -1 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, MemorySegment buffer) throws CException {
		return write(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Writes bytes from the buffer up to the specified count. Returns the number of bytes written,
	 * or -1 on EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, MemorySegment buffer, int length) throws CException {
		int n = Memory.limitInt(buffer, length);
		if (n == 0) return 0;
		return CLib.caller
			.callInt(c -> c.verifyInt(c.lib().write(fd, buffer, new size_t(n)).intValue(), -1,
				CErrNo.EAGAIN, CErrNo.EWOULDBLOCK, CErrNo.EINTR), "write", fd, buffer, n);
	}

	/**
	 * Writes bytes from the array using a buffer. Returns the number of bytes written, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, int... bytes) throws CException {
		return write(fd, Array.BYTE.of(bytes));
	}

	/**
	 * Writes bytes from the array using a buffer. Returns the number of bytes written, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes) throws CException {
		return write(fd, bytes, 0);
	}

	/**
	 * Writes bytes from the array using a buffer. Returns the number of bytes written, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes, int offset) throws CException {
		return write(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Writes bytes from the array using a buffer. Returns the number of bytes written, or -1 on
	 * EAGAIN/EWOULDBLOCK (with O_NONBLOCK) and EINTR errors.
	 */
	public static int write(int fd, byte[] bytes, int offset, int length) throws CException {
		try (var arena = Arena.ofConfined()) {
			var buffer = Primitive.BYTE.allocArray(arena, bytes, offset, length, false);
			return write(fd, buffer);
		}
	}

	/**
	 * Calls write() incrementally over the buffer, until all or no bytes are written. May block
	 * without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, MemorySegment buffer) throws CException {
		return writeAll(fd, buffer, Integer.MAX_VALUE);
	}

	/**
	 * Calls write() incrementally over the buffer, until the specified count or no bytes are
	 * written. May block without O_NONBLOCK. Returns the total number of bytes written.
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
	 * Calls write() incrementally over the array using a buffer, until all or no bytes are written.
	 * May block without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, int... bytes) throws CException {
		return writeAll(fd, Array.BYTE.of(bytes));
	}

	/**
	 * Calls write() incrementally over the array using a buffer, until all or no bytes are written.
	 * May block without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, byte[] bytes) throws CException {
		return writeAll(fd, bytes, 0);
	}

	/**
	 * Calls write() incrementally over the array using a buffer, until the specified count or no
	 * bytes are written. May block without O_NONBLOCK. Returns the total number of bytes written.
	 */
	public static int writeAll(int fd, byte[] bytes, int offset) throws CException {
		return writeAll(fd, bytes, offset, Integer.MAX_VALUE);
	}

	/**
	 * Calls write() incrementally over the array using a buffer, until the specified count or no
	 * bytes are written. May block without O_NONBLOCK. Returns the total number of bytes written.
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
	public static long lseek(int fd, long offset, Seek whence) throws CException {
		Validate.nonNull(whence, "whence");
		return lseek(fd, offset, whence.value);
	}
	
	/**
	 * Moves the position of file descriptor. Returns the new position.
	 */
	public static long lseek(int fd, long offset, int whence) throws CException {
		return CLib.caller.callLong(c -> {
			long result = c.lib().lseek(fd, new CLong(offset), whence).value();
			if (result < 0L) c.verify();
			return result;
		}, "lseek", fd, offset, whence);
	}

	/**
	 * Returns the current position in the file.
	 */
	public static long position(int fd) throws CException {
		return lseek(fd, 0, Seek.SEEK_CUR.value);
	}

	/**
	 * Sets the current position in the file.
	 */
	public static long position(int fd, long position) throws CException {
		return lseek(fd, position, Seek.SEEK_SET.value);
	}

	/**
	 * Returns the file size by moving to end of file then back to original position.
	 */
	public static long size(int fd) throws CException {
		long pos = position(fd);
		long size = lseek(fd, 0, Seek.SEEK_END.value);
		position(fd, pos);
		return size;
	}

	// support

	private static boolean stdFileNo(int fd) {
		return Maths.within(fd, STDIN_FILENO, STDERR_FILENO);
	}

	private static int applySlice(byte[] bytes, int offset, int length,
		Excepts.IntBiOperator<CException> operator) throws CException {
		if (bytes == null) return 0;
		offset = Maths.limit(offset, 0, bytes.length);
		length = Maths.limit(length, 0, bytes.length - offset);
		if (length == 0) return 0;
		return operator.applyAsInt(offset, length);
	}
}
