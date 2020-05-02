package ceri.serial.clib;

import static ceri.common.util.OsUtil.macInt;
import ceri.common.data.TypeTranscoder;

/**
 * Flags for lseek() call.
 */
public enum Seek {
	SEEK_SET(0), // start of file
	SEEK_CUR(1), // current position
	SEEK_END(2), // end of file
	SEEK_DATA(macInt(4, 3)), // start of next non-hole region >= offset
	SEEK_HOLE(macInt(3, 4)); // start of next next hole >= offset

	private static final TypeTranscoder<Seek> xcoder = TypeTranscoder.of(t -> t.value, Seek.class);
	public final int value;

	public static Seek from(int value) {
		return xcoder.decode(value);
	}

	Seek(int value) {
		this.value = value;
	}
}
