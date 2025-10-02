package ceri.serial.comm;

import ceri.common.data.Xcoder;
import ceri.serial.comm.jna.CSerial;

public enum DataBits {
	_5(CSerial.DATABITS_5),
	_6(CSerial.DATABITS_6),
	_7(CSerial.DATABITS_7),
	_8(CSerial.DATABITS_8);

	private static final Xcoder.Type<DataBits> xcoder = Xcoder.type(DataBits.class, t -> t.bits);
	public final int bits;

	public static DataBits from(int bits) {
		return xcoder.decode(bits);
	}

	private DataBits(int bits) {
		this.bits = bits;
	}
}
