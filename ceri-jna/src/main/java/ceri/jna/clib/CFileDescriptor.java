package ceri.jna.clib;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import com.sun.jna.Pointer;
import ceri.common.function.ExceptionSupplier;
import ceri.common.text.RegexUtil;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CLib;
import ceri.jna.util.PointerUtil;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public class CFileDescriptor implements FileDescriptor {
	private static final Pattern BROKEN_MESSAGE_REGEX = Pattern.compile("(?i)(?:remote i/o)");
	private static final Set<Integer> BROKEN_ERROR_CODES =
		Set.of(CError.ENOENT.code, CError.EREMOTEIO.code);
	private final int fd;
	private volatile boolean closed = false;

	/**
	 * Checks an exception generated by the file descriptor as to whether it is broken. Used by
	 * default for the self-healing file descriptor.
	 */
	public static boolean isBroken(Exception e) {
		if (!(e instanceof CException ce)) return false;
		if (ce.code != CError.UNDEFINED && BROKEN_ERROR_CODES.contains(ce.code)) return true;
		return (RegexUtil.found(BROKEN_MESSAGE_REGEX, e.getMessage()) != null);
	}

	/**
	 * Encapsulates open arguments. Use Mode.NONE if mode is unspecified.
	 */
	public static record Opener(String path, Mode mode, Collection<OpenFlag> flags)
		implements ExceptionSupplier<IOException, CFileDescriptor> {
		public Opener(String path, Mode mode, OpenFlag... flags) {
			this(path, mode, List.of(flags));
		}

		@Override
		public CFileDescriptor get() throws IOException {
			return open(path, mode, flags);
		}
	}

	/**
	 * Opens a file.
	 */
	public static CFileDescriptor open(String path, OpenFlag... flags) throws IOException {
		return open(path, Arrays.asList(flags));
	}

	/**
	 * Opens a file.
	 */
	public static CFileDescriptor open(String path, Collection<OpenFlag> flags) throws IOException {
		return open(path, null, flags);
	}

	/**
	 * Opens a file.
	 */
	public static CFileDescriptor open(String path, Mode mode, OpenFlag... flags)
		throws IOException {
		return open(path, mode, Arrays.asList(flags));
	}

	/**
	 * Opens a file.
	 */
	public static CFileDescriptor open(String path, Mode mode, Collection<OpenFlag> flags)
		throws IOException {
		return new CFileDescriptor(mode == null ? CLib.open(path, OpenFlag.encode(flags))
			: CLib.open(path, OpenFlag.encode(flags), mode.value()));
	}

	/**
	 * Take ownership of an existing file descriptor.
	 */
	public static CFileDescriptor of(int fd) {
		return new CFileDescriptor(fd);
	}

	private CFileDescriptor(int fd) {
		if (fd < 0) throw new IllegalArgumentException("Invalid file descriptor: " + fd);
		this.fd = fd;
	}

	@Override
	public int fd() {
		if (closed) throw new IllegalStateException("File descriptor is closed: " + fd);
		return fd;
	}

	@Override
	public int read(Pointer p, int offset, int length) throws IOException {
		if (length == 0) return 0;
		return CLib.read(fd(), PointerUtil.offset(p, offset), length);
	}

	@Override
	public void write(Pointer p, int offset, int length) throws IOException {
		// Loop until all bytes are written, as recommended in gnu-c docs.
		for (int count = 0; count < length;) {
			int n = CLib.write(fd(), PointerUtil.offset(p, offset + count), length - count);
			if (n == 0) throw CException.general("Incomplete write: %d/%d bytes", count, length);
			count += n;
		}
	}

	@Override
	public int seek(int offset, Seek whence) throws IOException {
		return CLib.lseek(fd(), offset, whence.value);
	}

	@Override
	public int fcntl(String name, int cmd, Object... objs) throws IOException {
		return CLib.fcntl(name, fd(), cmd, objs);
	}

	@Override
	public int ioctl(String name, int request, Object... objs) throws IOException {
		return CLib.ioctl(name, fd(), request, objs);
	}

	@Override
	public void close() throws IOException {
		if (!closed) closed = close(fd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fd);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CFileDescriptor other)) return false;
		if (fd != other.fd) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("fd=%d/0x%x", fd, fd);
	}

	private static boolean close(int fd) {
		try {
			CLib.close(fd);
			return true;
		} catch (CException e) {
			return false;
		}
	}
}