package ceri.jna.clib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.data.TypeTranscoder;
import ceri.common.text.StringUtil;
import ceri.jna.clib.jna.CFcntl;

/**
 * Flags for CLib open() and specific fcntl() calls, usually defined in fcntl.h.
 * <p/>
 * Warning: numbers vary by specific OS, and are verified only for Mac, Raspberry Pi.
 */
public enum OpenFlag {
	O_RDONLY(CFcntl.O_RDONLY),
	O_WRONLY(CFcntl.O_WRONLY),
	O_RDWR(CFcntl.O_RDWR),
	O_ACCMODE(CFcntl.O_ACCMODE),
	O_CREAT(CFcntl.O_CREAT),
	O_EXCL(CFcntl.O_EXCL),
	O_NOCTTY(CFcntl.O_NOCTTY),
	O_TRUNC(CFcntl.O_TRUNC),
	O_APPEND(CFcntl.O_APPEND),
	O_NONBLOCK(CFcntl.O_NONBLOCK),
	O_DSYNC(CFcntl.O_DSYNC),
	O_DIRECTORY(CFcntl.O_DIRECTORY),
	O_NOFOLLOW(CFcntl.O_NOFOLLOW),
	O_CLOEXEC(CFcntl.O_CLOEXEC);

	private static final int NO_RDONLY_MASK = O_WRONLY.value | O_RDWR.value;
	private static final TypeTranscoder<OpenFlag> xcoder = TypeTranscoder.of(t -> t.value, OpenFlag.class);
	public final int value;

	public static int encode(OpenFlag... flags) {
		return encode(Arrays.asList(flags));
	}

	public static int encode(Collection<OpenFlag> flags) {
		return xcoder.encodeInt(flags);
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
