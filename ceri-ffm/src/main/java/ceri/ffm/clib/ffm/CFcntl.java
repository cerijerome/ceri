package ceri.ffm.clib.ffm;

import java.nio.file.Path;
import ceri.common.collect.Enums;
import ceri.common.data.Xcoder;
import ceri.common.io.Paths;
import ceri.common.reflect.Reflect;
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
		/** Open for reading only. */
		O_RDONLY(0x0),
		/** Open for writing only. */
		O_WRONLY(0x1),
		/** Open for reading and writing. */
		O_RDWR(0x2),
		// creation flags
		/** Create if non-existent. */
		O_CREAT(Const.O_CREAT),
		/** Error if already exists. */
		O_EXCL(Const.O_EXCL),
		/** Don't assign controlling terminal. */
		O_NOCTTY(Const.O_NOCTTY),
		/** Truncate to 0 length. */
		O_TRUNC(Const.O_TRUNC),
		/** Set append mode. */
		O_APPEND(Const.O_APPEND),
		/** No delay. */
		O_NONBLOCK(Const.O_NONBLOCK),
		/** Sync I/O data integrity. */
		O_DSYNC(Const.O_DSYNC),
		/** Signal process group when data ready. */
		O_ASYNC(Const.O_ASYNC),
		/** Open fails if not a directory. */
		O_DIRECTORY(Const.O_DIRECTORY),
		/** Don't follow symbolic links. */
		O_NOFOLLOW(Const.O_NOFOLLOW),
		/** Implicitly set FD_CLOEXEC, close on exec. */
		O_CLOEXEC(Const.O_CLOEXEC),
		/** Sync I/O file integrity. */
		O_SYNC(Const.O_SYNC);

		public static final int O_ACCMODE = 0x3; // Access mode mask
		public static final Xcoder.Types<Open> xcoder = Xcoder.types(Open.class).custom(types -> {
			if (types.contains(O_RDWR) || types.contains(O_WRONLY)) types.remove(O_RDONLY);
			else types.add(O_RDONLY);
		});
		public final int value;

		/**
		 * Encapsulates a flag mask, with flag type access.
		 */
		public record Mask(int mask) {
			/**
			 * Decodes the mask to typed flags.
			 */
			public Xcoder.Rem<Open> flags() {
				return xcoder.decodeAllRem(mask());
			}

			@Override
			public final String toString() {
				return flags().toString();
			}
		}

		/**
		 * Encodes flag types to a mask.
		 */
		public static Mask mask(Open... flags) {
			return mask(xcoder.encodeInt(flags));
		}

		/**
		 * Returns a mask access wrapper.
		 */
		public static Mask mask(int mask) {
			return new Mask(mask);
		}

		private Open(int value) {
			this.value = value;
		}
	}

	/**
	 * Mode masks from {@code <sys/stat.h>}
	 */
	public enum Mode {
		/** Execute other. */
		S_IXOTH(0000001),
		/** Write other. */
		S_IWOTH(0000002),
		/** Read other. */
		S_IROTH(0000004),
		/** Read, write, and execute other. */
		S_IRWXO(0000007),
		/** Execute group. */
		S_IXGRP(0000010),
		/** Write group. */
		S_IWGRP(0000020),
		/** Read group. */
		S_IRGRP(0000040),
		/** Read, write, and execute group. */
		S_IRWXG(0000070),
		/** Execute user. */
		S_IXUSR(0000100),
		/** Write user. */
		S_IWUSR(0000200),
		/** Read user. */
		S_IRUSR(0000400),
		/** Read, write, and execute user. */
		S_IRWXU(0000700),
		/** Directory restricted delete. */
		S_ISVTX(0001000),
		/** Set group id on execution. */
		S_ISGID(0002000),
		/** Set user id on execution. */
		S_ISUID(0004000),
		/** Named pipe. */
		S_IFIFO(0010000),
		/** Character special. */
		S_IFCHR(0020000),
		/** Directory. */
		S_IFDIR(0040000),
		/** Block special. */
		S_IFBLK(0060000),
		/** Regular. */
		S_IFREG(0100000),
		/** Symbolic link. */
		S_IFLNK(0120000),
		/** Socket. */
		S_IFSOCK(0140000),
		/** Type of file mask. */
		S_IFMT(0170000);

		public static final Xcoder.Types<Mode> xcoder =
			Xcoder.types(Enums.of(Mode.class).reversed(), t -> t.value);
		public final int value;

		/**
		 * Encapsulates a flag mask, with flag type access.
		 */
		public record Mask(int mask) {
			/**
			 * Decodes the mask to typed flags.
			 */
			public Xcoder.Rem<Mode> flags() {
				return xcoder.decodeAllRem(mask());
			}

			@Override
			public final String toString() {
				return Integer.toOctalString(mask());
			}
		}

		/**
		 * Encodes flag types to a mask.
		 */
		public static Mask mask(Mode... flags) {
			return mask(xcoder.encodeInt(flags));
		}

		/**
		 * Returns a mask access wrapper.
		 */
		public static Mask mask(int mask) {
			return new Mask(mask);
		}

		private Mode(int value) {
			this.value = value;
		}
	}

	/**
	 * File descriptor actions. Only a subset are included here.
	 */
	public enum Action {
		/** Duplicate file descriptor. */
		F_DUPFD(0),
		/** Get file descriptor flags. */
		F_GETFD(1),
		/** Set file descriptor flags. */
		F_SETFD(2),
		/** Get file status flags. */
		F_GETFL(3),
		/** Set file status flags. */
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
			m -> m.accept("open", path, Open.mask(flags)));
	}

	/**
	 * Opens the path with flags and mode, and returns a file descriptor.
	 */
	public static int open(String path, int flags, int mode) throws CException {
		// mode_t vararg type is promoted to int
		return CLib.caller.verifyInt(lib -> lib.open(path, flags, mode), -1,
			m -> m.accept("open", path, Open.mask(flags), Mode.mask(mode)));
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
	 * Gets the file descriptor flags. Currently open FD_CLOEXEC supported.
	 */
	public static int getFd(int fd) throws CException {
		return fcntl(fd, Action.F_GETFD);
	}

	/**
	 * Sets the file descriptor flags. Currently open FD_CLOEXEC supported.
	 */
	public static void setFd(int fd, int flags) throws CException {
		fcntl(fd, Action.F_SETFD, flags);
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

	// support

	private static int fcntl(int fd, Action action, Object... objs) throws CException {
		return fcntl(fd, action.name(), action.value, objs);
	}

	// os-specific initialization

	private static class Const {
		private static final int O_CREAT;
		private static final int O_EXCL;
		private static final int O_NOCTTY;
		private static final int O_TRUNC;
		private static final int O_APPEND;
		private static final int O_NONBLOCK;
		private static final int O_DSYNC;
		private static final int O_ASYNC;
		private static final int O_DIRECTORY;
		private static final int O_NOFOLLOW;
		private static final int O_CLOEXEC;
		private static final int O_SYNC;

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
