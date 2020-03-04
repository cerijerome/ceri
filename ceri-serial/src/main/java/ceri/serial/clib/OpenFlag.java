package ceri.serial.clib;

import java.util.Set;
import ceri.common.data.TypeTranscoder;

public enum OpenFlag {
	O_RDONLY(0x0000),
	O_WRONLY(0x0001),
	O_RDWR(0x0002);

	public static final Set<OpenFlag> WRITE = Set.of(O_WRONLY, O_RDWR);
	public static final TypeTranscoder<OpenFlag> xcoder =
		TypeTranscoder.of(t -> t.value, OpenFlag.class);
	public final short value;

	OpenFlag(int value) {
		this.value = (short) value;
	}
}
