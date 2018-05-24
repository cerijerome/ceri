package javax.comm;

public class SerialPort extends CommPort {
	public static final int DATABITS_5 = purejavacomm.SerialPort.DATABITS_5;
	public static final int DATABITS_6 = purejavacomm.SerialPort.DATABITS_6;
	public static final int DATABITS_7 = purejavacomm.SerialPort.DATABITS_7;
	public static final int DATABITS_8 = purejavacomm.SerialPort.DATABITS_8;
	public static final int PARITY_NONE = purejavacomm.SerialPort.PARITY_NONE;
	public static final int PARITY_EVEN = purejavacomm.SerialPort.PARITY_EVEN;
	public static final int PARITY_ODD = purejavacomm.SerialPort.PARITY_ODD;
	public static final int PARITY_SPACE = purejavacomm.SerialPort.PARITY_SPACE;
	public static final int PARITY_MARK = purejavacomm.SerialPort.PARITY_MARK;
	public static final int STOPBITS_1 = purejavacomm.SerialPort.STOPBITS_1;
	public static final int STOPBITS_1_5 = purejavacomm.SerialPort.STOPBITS_1_5;
	public static final int STOPBITS_2 = purejavacomm.SerialPort.STOPBITS_2;
	public static final int FLOWCONTROL_NONE = purejavacomm.SerialPort.FLOWCONTROL_NONE;
	public static final int FLOWCONTROL_RTSCTS_IN = purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_IN;
	public static final int FLOWCONTROL_RTSCTS_OUT = purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_OUT;
	public static final int FLOWCONTROL_XONXOFF_IN = purejavacomm.SerialPort.FLOWCONTROL_XONXOFF_IN;
	public static final int FLOWCONTROL_XONXOFF_OUT =
		purejavacomm.SerialPort.FLOWCONTROL_XONXOFF_OUT;
	private final purejavacomm.SerialPort serialPort;

	SerialPort(purejavacomm.SerialPort serialPort) {
		super(serialPort);
		this.serialPort = serialPort;
	}

	public void setSerialPortParams(int b, int d, int s, int p)
		throws UnsupportedCommOperationException {
		try {
			serialPort.setSerialPortParams(b, d, s, p);
		} catch (purejavacomm.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public boolean isDTR() {
		return serialPort.isDTR();
	}

	public void setDTR(boolean state) {
		serialPort.setDTR(state);
	}

	public boolean isCD() {
		return serialPort.isCD();
	}

	public boolean isCTS() {
		return serialPort.isCTS();
	}

	public boolean isDSR() {
		return serialPort.isDSR();
	}

	public boolean isRI() {
		return serialPort.isRI();
	}

	public boolean isRTS() {
		return serialPort.isRTS();
	}

	public void setRTS(boolean state) {
		serialPort.setRTS(state);
	}

	public void sendBreak(int duration) {
		serialPort.sendBreak(duration);
	}

}
