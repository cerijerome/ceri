package ceri.serial.clib;

import static ceri.common.util.OsUtil.macInt;
import ceri.common.data.TypeTranscoder;

/**
 * Flags for lseek() call.
 */
public enum Seek {
	/** From start of file. */
	SEEK_SET(0),
	/** From current position. */
	SEEK_CUR(1),
	/** From end of file. */
	SEEK_END(2),
	/** Start of next non-hole region >= offset */
	SEEK_DATA(macInt(4, 3)),
	/** Start of next hole >= offset */
	SEEK_HOLE(macInt(3, 4));

	private static final TypeTranscoder<Seek> xcoder = TypeTranscoder.of(t -> t.value, Seek.class);
	public final int value;

	public static Seek from(int value) {
		return xcoder.decode(value);
	}

	Seek(int value) {
		this.value = value;
	}
}
