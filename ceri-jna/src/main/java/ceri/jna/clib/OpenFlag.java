package ceri.jna.clib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.data.TypeTranscoder;
import ceri.common.text.StringUtil;
import ceri.jna.clib.jna.CLib;

/**
 * Flags for CLib open() and specific fcntl() calls, usually defined in fcntl.h.
 * <p/>
 * Warning: numbers vary by specific OS, and are verified only for Mac, Raspberry Pi.
 */
public enum OpenFlag {
	O_RDONLY(CLib.O_RDONLY),
	O_WRONLY(CLib.O_WRONLY),
	O_RDWR(CLib.O_RDWR),
	O_ACCMODE(CLib.O_ACCMODE),
	O_CREAT(CLib.O_CREAT),
	O_EXCL(CLib.O_EXCL),
	O_NOCTTY(CLib.O_NOCTTY),
	O_TRUNC(CLib.O_TRUNC),
	O_APPEND(CLib.O_APPEND),
	O_NONBLOCK(CLib.O_NONBLOCK),
	O_DSYNC(CLib.O_DSYNC),
	O_DIRECTORY(CLib.O_DIRECTORY),
	O_NOFOLLOW(CLib.O_NOFOLLOW),
	O_CLOEXEC(CLib.O_CLOEXEC);

	private static final int NO_RDONLY_MASK = O_WRONLY.value | O_RDWR.value;
	private static final TypeTranscoder<OpenFlag> xcoder =
		TypeTranscoder.of(t -> t.value, OpenFlag.class);
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
