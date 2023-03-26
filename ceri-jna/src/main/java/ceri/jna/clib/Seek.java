package ceri.jna.clib;

import ceri.common.data.TypeTranscoder;
import ceri.jna.clib.jna.CLib;

/**
 * Flags for lseek() call.
 */
public enum Seek {
	/** From start of file. */
	SEEK_SET(CLib.SEEK_SET),
	/** From current position. */
	SEEK_CUR(CLib.SEEK_CUR),
	/** From end of file. */
	SEEK_END(CLib.SEEK_END);

	private static final TypeTranscoder<Seek> xcoder = TypeTranscoder.of(t -> t.value, Seek.class);
	public final int value;

	public static Seek from(int value) {
		return xcoder.decode(value);
	}

	Seek(int value) {
		this.value = value;
	}
}
