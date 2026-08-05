package ceri.ffm.clib.ffm;

import java.util.Set;
import java.util.function.IntUnaryOperator;
import ceri.common.collect.Enums;
import ceri.common.collect.Maps;
import ceri.common.data.Xcoder;
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

	private CFcntl() {}

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
		public static final Xcoder.Types<Open> xcoder = new Xcoder.Types<>(
			Maps.convert(Maps::link, t -> (long) t.value, t -> t, Enums.of(Open.class))) {
			@Override
			protected Xcoder.Rem<Open> rem(Set<Open> types, long diff) {
				if (types.contains(O_RDWR) || types.contains(O_WRONLY)) types.remove(O_RDONLY);
				else types.add(O_RDONLY);
				return super.rem(types, diff);
			}
		};
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

		/**
		 * Returns true if access modes are valid.
		 */
		public boolean isValidAccess(int value) {
			return (value & O_ACCMODE) != O_ACCMODE;
		}

		private Open(int value) {
			this.value = value;
		}
	}

	/**
	 * Mode masks.
	 */
	public enum Mode {
		xoth(0001),
		woth(0002),
		roth(0004),
		rwxo(0007),
		xgrp(0010),
		wgrp(0020),
		rgrp(0040),
		rwxg(0070),
		xusr(0100),
		wusr(0200),
		rusr(0400),
		rwxu(0700),
		svtx(01000),
		sgid(02000),
		suid(04000),
		fifo(010000),
		fchr(020000),
		fdir(040000),
		fblk(060000),
		freg(0100000),
		flnk(0120000),
		fsock(0140000),
		fmt(0170000);

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
	 * File descriptor actions.
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
	public static int open(String path, Open... flags) throws CException {
		return open(path, Open.xcoder.encodeInt(flags));
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
	public static int fcntl(String name, int fd, int command, Object... objs) throws CException {
		return CLib.caller.verifyInt(lib -> lib.fcntl(fd, command, objs), -1,
			m -> m.accept(name, fd, name + ":0x" + Integer.toHexString(command), objs));
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
	 * Gets the file access mode and file status flags.
	 */
	public static int getFl(int fd) throws CException {
		return fcntl(fd, Action.F_GETFL);
	}

	/**
	 * Sets the file status flags.
	 */
	public static void setFl(int fd, int flags) throws CException {
		fcntl(fd, Action.F_SETFL, flags);
	}

	/**
	 * Applies the modifier to current flags. Returns the new flags value.
	 */
	public static int applyFl(int fd, IntUnaryOperator flagFn) throws CException {
		int flags = flagFn.applyAsInt(getFl(fd));
		setFl(fd, flags);
		return flags;
	}

	// support

	private static int fcntl(int fd, Action action, Object... objs) throws CException {
		return fcntl(action.name(), fd, action.value, objs);
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
