package ceri.ffm.clib.ffm;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import ceri.common.collect.Enums;
import ceri.common.data.Xcoder;
import ceri.common.io.Paths;
import ceri.common.reflect.Reflect;
import ceri.common.text.Joiner;
import ceri.common.util.Os;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CUndefined;

/**
 * Types and functions from {@code <fcntl.h>}
 */
@CInclude("fcntl.h")
public class CFcntl {
	@CUndefined
	public static final int INVALID_FD = -1;
	/** Used by fcntl F_SETFD/F_GETFD. */
	public static final int FD_CLOEXEC = 1;

	private CFcntl() {}

	static {
		Reflect.init(Const.class); // make sure initialized first
	}

	/**
	 * Open flags.
	 */
	public enum Open {
		// access modes
		O_RDONLY(0x0),
		O_WRONLY(0x1),
		O_RDWR(0x2),
		// creation flags
		O_CREAT(Const.O_CREAT),
		O_EXCL(Const.O_EXCL),
		O_NOCTTY(Const.O_NOCTTY),
		O_TRUNC(Const.O_TRUNC),
		O_APPEND(Const.O_APPEND),
		O_NONBLOCK(Const.O_NONBLOCK),
		O_DSYNC(Const.O_DSYNC),
		O_ASYNC(Const.O_ASYNC),
		O_DIRECTORY(Const.O_DIRECTORY),
		O_NOFOLLOW(Const.O_NOFOLLOW),
		O_CLOEXEC(Const.O_CLOEXEC),
		O_SYNC(Const.O_SYNC);

		public static final int O_ACCMODE = 0x3; // mask
		public static final Xcoder.Types<Open> xcoder = Xcoder.types(Open.class).custom(types -> {
			if (types.contains(O_RDWR) || types.contains(O_WRONLY)) types.remove(O_RDONLY);
			else types.add(O_RDONLY);
		});
		public final int value;

		/**
		 * Holds combined flag value.
		 */
		public record Value(int value) {
			/**
			 * Decode the value to flag enums.
			 */
			public Set<Open> flags() {
				return xcoder.decodeAll(value());
			}

			@Override
			public String toString() {
				return Joiner.OR.join(flags());
			}
		}

		/**
		 * Encapsulates open flags.
		 */
		public static Value of(int value) {
			return new Value(value);
		}

		/**
		 * Encapsulates open flags.
		 */
		public static Value of(Open... flags) {
			return of(xcoder.encodeInt(flags));
		}

		private Open(int value) {
			this.value = value;
		}
	}

	/**
	 * Mode masks from {@code <sys/stat.h>}
	 */
	public enum Mode {
		S_IXOTH(0000001),
		S_IWOTH(0000002),
		S_IROTH(0000004),
		S_IRWXO(0000007),
		S_IXGRP(0000010),
		S_IWGRP(0000020),
		S_IRGRP(0000040),
		S_IRWXG(0000070),
		S_IXUSR(0000100),
		S_IWUSR(0000200),
		S_IRUSR(0000400),
		S_IRWXU(0000700),
		S_ISVTX(0001000),
		S_ISGID(0002000),
		S_ISUID(0004000),
		S_IFIFO(0010000),
		S_IFCHR(0020000),
		S_IFDIR(0040000),
		S_IFBLK(0060000),
		S_IFREG(0100000),
		S_IFLNK(0120000),
		S_IFSOCK(0140000),
		S_IFMT(0170000);

		public static final Xcoder.Types<Mode> xcoder =
			Xcoder.types(Enums.of(Mode.class).reversed(), t -> t.value);
		public final int value;

		/**
		 * Holds combined mode masks.
		 */
		public record Value(int value) {
			/**
			 * Extracts mode masks.
			 */
			public Set<Mode> modes() {
				return xcoder.decodeAll(value());
			}

			@Override
			public String toString() {
				return "0" + Integer.toOctalString(value());
			}
		}

		/**
		 * Encapsulates combined mode masks.
		 */
		public static Value of(int value) {
			return new Value(value);
		}

		/**
		 * Encapsulates combined mode masks.
		 */
		public static Value of(Mode... modes) {
			return of(xcoder.encodeInt(modes));
		}

		private Mode(int value) {
			this.value = value;
		}
	}

	/**
	 * File descriptor actions. Only a subset are included here.
	 */
	public enum Action {
		F_DUPFD(0),
		F_GETFD(1),
		F_SETFD(2),
		F_GETFL(3),
		F_SETFL(4);

		public static final Xcoder.Type<Action> xcoder = Xcoder.type(Action.class);
		public final int value;

		private Action(int value) {
			this.value = value;
		}
	}

	/**
	 * Opens the path with flags, and returns a file descriptor.
	 */
	public static int open(String path, int flags) throws CException {
		return CLib.caller.verifyInt(lib -> lib.open(path, flags), -1,
			m -> m.accept("open", path, Open.of(flags)));
	}

	/**
	 * Opens the path with flags and mode, and returns a file descriptor.
	 */
	public static int open(String path, int flags, int mode) throws CException {
		// mode_t vararg type is promoted to int
		return CLib.caller.verifyInt(lib -> lib.open(path, flags, mode), -1,
			m -> m.accept("open", path, Open.of(flags), Mode.of(mode)));
	}

	/**
	 * Opens the path with flags, and returns a file descriptor.
	 */
	public static int open(Path path, Open... flags) throws CException {
		return open(Paths.string(path), Open.xcoder.encodeInt(flags));
	}

	/**
	 * Opens the path with flags and mode, and returns a file descriptor.
	 */
	public static int open(Path path, int mode, Open... flags) throws CException {
		return open(Paths.string(path), Open.xcoder.encodeInt(flags), mode);
	}

	/**
	 * Checks a file descriptor validity
	 */
	public static boolean validFd(int fd) {
		return fd >= 0;
	}

	/**
	 * Validates a file descriptor
	 */
	public static int validateFd(int fd) throws CException {
		if (validFd(fd)) return fd;
		throw CException.of(fd, "Invalid file descriptor");
	}

	/**
	 * Performs a fcntl function. Arguments and return value depend on the function.
	 */
	public static int fcntl(int fd, String name, int command, Object... objs) throws CException {
		return CLib.caller.verifyInt(lib -> lib.fcntl(fd, command, objs), -1,
			m -> m.accept("fcntl", fd, m.fmt("%s:0x%x", name, command), objs));
	}

	/**
	 * Duplicates the file descriptor using the lowest-numbered available >= min.
	 */
	public static int dupFd(int fd, int min) throws CException {
		return fcntl(fd, Action.F_DUPFD, min);
	}

	/**
	 * Gets the file descriptor flags.
	 */
	public static int getFd(int fd) throws CException {
		return fcntl(fd, Action.F_GETFD);
	}

	/**
	 * Sets the file descriptor flags.
	 */
	public static void setFd(int fd, int flags) throws CException {
		fcntl(fd, Action.F_SETFD, flags);
	}

	/**
	 * Applies the modifier to current flags. Returns the new flags value.
	 */
	public static int applyFd(int fd, IntUnaryOperator flagFn) throws CException {
		return applyFcntl(fd, Action.F_GETFD, Action.F_SETFD, flagFn);
	}

	/**
	 * Gets the file access mode and status flags.
	 */
	public static int getFl(int fd) throws CException {
		return fcntl(fd, Action.F_GETFL);
	}

	/**
	 * Sets the file access mode and status flags.
	 */
	public static void setFl(int fd, int flags) throws CException {
		fcntl(fd, Action.F_SETFL, flags);
	}

	/**
	 * Applies the modifier to current flags. Returns the new flags value.
	 */
	public static int applyFl(int fd, IntUnaryOperator flagFn) throws CException {
		return applyFcntl(fd, Action.F_GETFL, Action.F_SETFL, flagFn);
	}

	// support

	private static int fcntl(int fd, Action action, Object... objs) throws CException {
		return fcntl(fd, action.name(), action.value, objs);
	}

	private static int applyFcntl(int fd, Action get, Action set, IntUnaryOperator flagFn)
		throws CException {
		int previous = fcntl(fd, get);
		int flags = flagFn.applyAsInt(previous);
		if (flags != previous) fcntl(fd, set, flags);
		return flags;
	}

	// os-specific initialization

	private static class Const {
		public static final int O_CREAT;
		public static final int O_EXCL;
		public static final int O_NOCTTY;
		public static final int O_TRUNC;
		public static final int O_APPEND;
		public static final int O_NONBLOCK;
		public static final int O_DSYNC;
		public static final int O_ASYNC;
		public static final int O_DIRECTORY;
		public static final int O_NOFOLLOW;
		public static final int O_CLOEXEC;
		public static final int O_SYNC;

		static {
			var os = Os.info();
			if (os.mac) {
				O_CREAT = 0x200;
				O_EXCL = 0x800;
				O_NOCTTY = 0x20000;
				O_TRUNC = 0x400;
				O_APPEND = 0x8;
				O_NONBLOCK = 0x4;
				O_DSYNC = 0x400000;
				O_ASYNC = 0x40;
				O_DIRECTORY = 0x100000;
				O_NOFOLLOW = 0x100;
				O_CLOEXEC = 0x1000000;
				O_SYNC = 0x80;
			} else {
				O_CREAT = 0x40;
				O_EXCL = 0x80;
				O_NOCTTY = 0x100;
				O_TRUNC = 0x200;
				O_APPEND = 0x400;
				O_NONBLOCK = 0x800;
				O_DSYNC = 0x1000;
				O_ASYNC = 0x2000;
				O_DIRECTORY = os.arm(0x4000, 0x10000);
				O_NOFOLLOW = os.arm(0x8000, 0x20000);
				O_CLOEXEC = 0x80000;
				O_SYNC = 0x100000 | O_DSYNC;
			}
		}
	}
}
