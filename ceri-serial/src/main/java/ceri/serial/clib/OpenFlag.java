package ceri.serial.clib;

import static ceri.common.util.OsUtil.macInt;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.collection.StreamUtil;
import ceri.common.data.TypeTranscoder;
import ceri.common.text.StringUtil;

/**
 * Flags for CLib open() call, usually defined in fcntl.h.
 * <p/>
 * Warning: numbers vary by specific OS, and are verified only for Mac, Raspberry Pi.
 */
public enum OpenFlag {
	O_RDONLY(0x0),
	O_WRONLY(0x1),
	O_RDWR(0x2),
	O_ACCMODE(0x3),
	O_CREAT(macInt(0x200, 0x40)),
	O_EXCL(macInt(0x800, 0x80)),
	O_NOCTTY(macInt(0x20000, 0x100)),
	O_TRUNC(macInt(0x400, 0x200)),
	O_APPEND(macInt(0x8, 0x400)),
	O_NONBLOCK(macInt(0x4, 0x800)),
	O_DSYNC(macInt(0x400000, 0x1000)),
	O_DIRECT(macInt(-1, 0x10000)), // linux only, generic 0x4000?
	O_LARGEFILE(macInt(-1, 0x20000)), // linux only, generic 0x8000?
	O_DIRECTORY(macInt(0x100000, 0x4000)), // linux generic 0x10000?
	O_NOFOLLOW(macInt(0x100, 0x8000)), // linux generic 0x20000?
	O_NOATIME(macInt(-1, 0x40000)), // linux only
	O_CLOEXEC(macInt(0x1000000, 0x80000)),
	O_SHLOCK(macInt(0x10, -1)), // mac only
	O_EXLOCK(macInt(0x20, -1)), // mac only
	O_SYMLINK(macInt(0x200000, -1)); // mac only

	private static final int NO_RDONLY_MASK = 0x3;
	private static final TypeTranscoder<OpenFlag> xcoder = TypeTranscoder.of(t -> t.value,
		StreamUtil.stream(OpenFlag.class).filter(t -> t.value != -1).toArray(OpenFlag[]::new));
	public final int value;

	public static int encode(OpenFlag... flags) {
		return encode(Arrays.asList(flags));
	}

	public static int encode(Collection<OpenFlag> flags) {
		return xcoder.encode(flags);
	}

	public static Set<OpenFlag> decode(int flags) {
		Set<OpenFlag> set = xcoder.decodeAll(flags);
		if ((flags & NO_RDONLY_MASK) != 0) set.remove(O_RDONLY);
		return set;
	}

	public static String string(int value) {
		return StringUtil.join("|", decode(value));
	}

	OpenFlag(int value) {
		this.value = value;
	}
}
