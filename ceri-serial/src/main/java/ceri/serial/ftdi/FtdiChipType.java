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
	public static final Set<FtdiChipType> SYNC_FIFO_TYPES = enumSet(TYPE_2232H, TYPE_232H);
	public final int value;

	private FtdiChipType(int value) {
		this.value = value;
	}

	public boolean isHType() {
		return H_TYPES.contains(this);
	}

	public boolean isSyncFifoType() {
		return SYNC_FIFO_TYPES.contains(this);
	}

	public static FtdiChipType guess(int device, int serial) {
		switch (device & 0xffff) {
		case 0x0200:
			return serial == 0 ? TYPE_BM : TYPE_AM;
		case 0x0400:
			return TYPE_BM;
		case 0x0500:
			return TYPE_2232C;
		case 0x0600:
			return TYPE_R;
		case 0x0700:
			return TYPE_2232H;
		case 0x0800:
			return TYPE_4232H;
		case 0x0900:
			return TYPE_232H;
		case 0x1000:
			return TYPE_230X;
		default:
			return TYPE_BM;
		}
	}

}
