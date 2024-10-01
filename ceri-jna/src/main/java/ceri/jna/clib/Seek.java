package ceri.jna.clib;

import ceri.common.data.TypeTranscoder;
import ceri.jna.clib.jna.CUnistd;

/**
 * Flags for lseek() call.
 */
public enum Seek {
	SET(CUnistd.SEEK_SET),
	CUR(CUnistd.SEEK_CUR),
	END(CUnistd.SEEK_END);

	private static final TypeTranscoder<Seek> xcoder = TypeTranscoder.of(t -> t.value, Seek.class);
	public final int value;

	public static Seek from(int value) {
		return xcoder.decode(value);
	}

	private Seek(int value) {
		this.value = value;
	}
}
