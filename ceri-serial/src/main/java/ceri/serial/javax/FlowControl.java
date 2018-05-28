package ceri.serial.javax;

import ceri.common.util.BasicUtil;

public enum FlowControl {
	none(purejavacomm.SerialPort.FLOWCONTROL_NONE),
	rtsCtsIn(purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_IN),
	rtsCtsOut(purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_OUT),
	xonXoffIn(purejavacomm.SerialPort.FLOWCONTROL_XONXOFF_IN),
	xonXoffOut(purejavacomm.SerialPort.FLOWCONTROL_XONXOFF_OUT);

	public final int value;
	
	private FlowControl(int value) {
		this.value = value;
	}
	
	public static FlowControl from(int value) {
		return BasicUtil.find(FlowControl.class, t -> t.value == value);
	}
	
}
