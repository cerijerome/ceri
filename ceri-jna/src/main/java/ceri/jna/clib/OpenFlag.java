package ceri.jna.clib;

import java.util.Set;
import ceri.common.collection.EnumUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.MaskTranscoder;
import ceri.common.data.TypeTranscoder;
import ceri.common.text.StringUtil;
import ceri.jna.clib.jna.CFcntl;

/**
 * Flags for CLib open() and specific CFcntl() calls.
 */
public enum OpenFlag {
	O_RDONLY(CFcntl.O_RDONLY),
	O_WRONLY(CFcntl.O_WRONLY),
	O_RDWR(CFcntl.O_RDWR),
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

	public static final TypeTranscoder<OpenFlag> xcoder = new TypeTranscoder<>(t -> t.value,
		MaskTranscoder.NULL, EnumUtil.enums(OpenFlag.class), StreamUtil.mergeError()) {
		@Override
		protected long decodeWithRemainder(Set<OpenFlag> receiver, long value) {
			var rem = super.decodeWithRemainder(receiver, value);
			if ((value & CFcntl.O_ACCMODE) != 0) receiver.remove(O_RDONLY);
			return rem;
		}
	};
	public final int value;

	public static String string(int value) {
		return StringUtil.join("|", xcoder.decodeAll(value));
	}

	private OpenFlag(int value) {
		this.value = value;
	}
}
