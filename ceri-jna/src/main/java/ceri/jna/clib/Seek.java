package ceri.jna.clib;

import ceri.common.data.TypeTranscoder;
import ceri.jna.clib.jna.CUnistd;

/**
 * Flags for lseek() call.
 */
public enum Seek {
	/** From start of file. */
	SEEK_SET(CUnistd.SEEK_SET),
	/** From current position. */
	SEEK_CUR(CUnistd.SEEK_CUR),
	/** From end of file. */
	SEEK_END(CUnistd.SEEK_END);

	private static final TypeTranscoder<Seek> xcoder = TypeTranscoder.of(t -> t.value, Seek.class);
	public final int value;

	public static Seek from(int value) {
		return xcoder.decode(value);
	}

	Seek(int value) {
		this.value = value;
	}
}
