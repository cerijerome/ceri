package ceri.serial.javax;

import ceri.common.util.BasicUtil;

public enum StopBits {
	_1(purejavacomm.SerialPort.STOPBITS_1),
	_1_5(purejavacomm.SerialPort.STOPBITS_1_5),
	_2(purejavacomm.SerialPort.STOPBITS_2);

	public final int value;

	private StopBits(int value) {
		this.value = value;
	}

	public static StopBits from(int value) {
		return BasicUtil.find(StopBits.class, t -> t.value == value);
	}

}
