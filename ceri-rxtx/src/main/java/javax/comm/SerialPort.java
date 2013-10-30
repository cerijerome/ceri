package javax.comm;

public class SerialPort extends CommPort {
	public static final int DATABITS_8 = gnu.io.SerialPort.DATABITS_8;
	public static final int PARITY_NONE = gnu.io.SerialPort.PARITY_NONE;
	public static final int STOPBITS_1 = gnu.io.SerialPort.STOPBITS_1;
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

	public void setDTR(boolean state) {
		serialPort.setDTR(state);
	}

	public void setRTS(boolean state) {
		serialPort.setRTS(state);
	}
}
