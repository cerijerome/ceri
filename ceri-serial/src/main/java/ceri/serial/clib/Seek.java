package ceri.serial.clib;

import ceri.common.data.TypeTranscoder;

/**
 * Flags for CLib lseek() call, usually defined in fcntl.h.
 */
public enum Seek {
	SEEK_SET(0), // start of file
	SEEK_CUR(1), // current position
	SEEK_END(2); // end of file

	private static final TypeTranscoder<Seek> xcoder = TypeTranscoder.of(t -> t.value, Seek.class);
	public final int value;

	public static Seek from(int value) {
		return xcoder.decode(value);
	}

	Seek(int value) {
		this.value = value;
	}
}
