package javax.comm;

public class SerialPort extends CommPort {
	public static final int DATABITS_5 = gnu.io.SerialPort.DATABITS_5;
	public static final int DATABITS_6 = gnu.io.SerialPort.DATABITS_6;
	public static final int DATABITS_7 = gnu.io.SerialPort.DATABITS_7;
	public static final int DATABITS_8 = gnu.io.SerialPort.DATABITS_8;
	public static final int PARITY_NONE = gnu.io.SerialPort.PARITY_NONE;
	public static final int PARITY_EVEN = gnu.io.SerialPort.PARITY_EVEN;
	public static final int PARITY_ODD = gnu.io.SerialPort.PARITY_ODD;
	public static final int PARITY_SPACE = gnu.io.SerialPort.PARITY_SPACE;
	public static final int PARITY_MARK = gnu.io.SerialPort.PARITY_MARK;
	public static final int STOPBITS_1 = gnu.io.SerialPort.STOPBITS_1;
	public static final int STOPBITS_1_5 = gnu.io.SerialPort.STOPBITS_1_5;
	public static final int STOPBITS_2 = gnu.io.SerialPort.STOPBITS_2;
	public static final int FLOWCONTROL_NONE = gnu.io.SerialPort.FLOWCONTROL_NONE;
	public static final int FLOWCONTROL_RTSCTS_IN = gnu.io.SerialPort.FLOWCONTROL_RTSCTS_IN;
	public static final int FLOWCONTROL_RTSCTS_OUT = gnu.io.SerialPort.FLOWCONTROL_RTSCTS_OUT;
	public static final int FLOWCONTROL_XONXOFF_IN = gnu.io.SerialPort.FLOWCONTROL_XONXOFF_IN;
	public static final int FLOWCONTROL_XONXOFF_OUT = gnu.io.SerialPort.FLOWCONTROL_XONXOFF_OUT;
	private final gnu.io.SerialPort serialPort;

	SerialPort(gnu.io.SerialPort serialPort) {
		super(serialPort);
		this.serialPort = serialPort;
	}

	public void setSerialPortParams(int b, int d, int s, int p)
		throws UnsupportedCommOperationException {
		try {
			serialPort.setSerialPortParams(b, d, s, p);
		} catch (gnu.io.UnsupportedCommOperationException e) {
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

}
