package ceri.serial.terminal.command;

import ceri.common.data.TypeTranscoder;

public enum StatusFlag {
	dtr(3),
	ri(4),
	rts(5);
	
//	ringIndicator(3),
//	breakInterrupt(8),
//	dataAvailable(9),
//	outputEmpty(10),
//	framingError(16),
//	overrunError(17),
//	parityError(18);


	public static final TypeTranscoder.Flag<StatusFlag> xcoder =
		TypeTranscoder.flag(t -> t.mask, StatusFlag.class);
	public static final int BYTES = Integer.BYTES;
	public final int mask;

	private StatusFlag(int bit) {
		this.mask = 1 << bit;
	}

}
