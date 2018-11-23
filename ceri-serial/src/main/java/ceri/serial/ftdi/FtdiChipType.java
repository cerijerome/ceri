package ceri.serial.ftdi;

import static ceri.common.collection.ImmutableUtil.enumSet;
import java.util.Set;
import ceri.common.data.TypeTranscoder;

public enum FtdiChipType {
	TYPE_AM(0),
	TYPE_BM(1),
	TYPE_2232C(2),
	TYPE_R(3),
	TYPE_2232H(4),
	TYPE_4232H(5),
	TYPE_232H(6),
	TYPE_230X(7);

	public static final TypeTranscoder.Single<FtdiChipType> xcoder =
		TypeTranscoder.single(t -> t.value, FtdiChipType.class);
	public static final Set<FtdiChipType> H_TYPES = enumSet(TYPE_2232H, TYPE_4232H, TYPE_232H);
	public final int value;

	private FtdiChipType(int value) {
		this.value = value;
	}

	public boolean isHType() {
		return H_TYPES.contains(this);
	}
	
}
