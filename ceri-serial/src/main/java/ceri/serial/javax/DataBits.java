package ceri.serial.javax;

import ceri.common.util.BasicUtil;

public enum DataBits {
	_5(purejavacomm.SerialPort.DATABITS_5),
	_6(purejavacomm.SerialPort.DATABITS_6),
	_7(purejavacomm.SerialPort.DATABITS_7),
	_8(purejavacomm.SerialPort.DATABITS_8);

	public final int value;

	private DataBits(int value) {
		this.value = value;
	}

	public static DataBits from(int value) {
		return BasicUtil.find(DataBits.class, t -> t.value == value);
	}
	
}
