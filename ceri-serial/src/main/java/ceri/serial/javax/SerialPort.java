package ceri.serial.javax;

import java.io.IOException;
import com.sun.jna.Platform;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.TermiosUtil;
import jtermios.Termios;
import purejavacomm.PureJavaSerialPort;

public class SerialPort extends CommPort {
	public static final int CONNECTION_TIMEOUT_MS_DEF =
		CommPortIdentifier.CONNECTION_TIMEOUT_MS_DEF;
	private static final String UNKNOWN_OWNER = "Unknown";
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
	private final int nativeFileDescriptor;

	public static SerialPort open(String commPort) throws IOException {
		return open(commPort, UNKNOWN_OWNER);
	}

	public static SerialPort open(String commPort, String owner) throws IOException {
		return open(commPort, owner, CONNECTION_TIMEOUT_MS_DEF);
	}

	public static SerialPort open(String commPort, String owner, int connectionTimeoutMs)
		throws IOException {
		CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(commPort);
		return open(cpi, owner, connectionTimeoutMs);
	}

	@SuppressWarnings("resource") // Incorrect - comm port is closed on error
	private static SerialPort open(CommPortIdentifier cpi, String owner, int timeoutMs)
		throws PortInUseException {
		CommPort commPort = null;
		try {
			commPort = cpi.open(owner, timeoutMs);
			return SerialPort.class.cast(commPort);
		} catch (PortInUseException | RuntimeException e) {
			IoUtil.close(commPort);
			throw e;
		}
	}

	SerialPort(purejavacomm.SerialPort serialPort) {
		super(serialPort);
		this.serialPort = serialPort;
		nativeFileDescriptor = nativeFileDescriptor(serialPort);
	}

	public void setParams(SerialPortParams params) throws UnsupportedCommOperationException {
		try {
			serialPort.setSerialPortParams(params.baudRate, params.dataBits.value,
				params.stopBits.value, params.parity.value);
			if (isIncorrectBaudRate(params.baudRate)) setBaudAlt(params.baudRate);
		} catch (purejavacomm.UnsupportedCommOperationException | IOException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	private boolean isIncorrectBaudRate(int baud) throws IOException {
		if (!JnaUtil.isValidFileDescriptor(nativeFileDescriptor)) return false;
		Termios t = TermiosUtil.getTermios(nativeFileDescriptor);
		return t.c_ospeed != baud;
	}

	private boolean setBaudAlt(int b) throws IOException {
		if (Platform.isMac()) return setBaudForMac(b);
		return false;
	}

	private boolean setBaudForMac(int b) throws IOException {
		TermiosUtil.setIossSpeed(nativeFileDescriptor, b);
		return true;
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

	public void setBreakBit() throws IOException {
		JnaUtil.validateFileDescriptor(nativeFileDescriptor);
		TermiosUtil.setBreakBit(nativeFileDescriptor);
	}

	public void clearBreakBit() throws IOException {
		JnaUtil.validateFileDescriptor(nativeFileDescriptor);
		TermiosUtil.clearBreakBit(nativeFileDescriptor);
	}

	public int nativeFileDescriptor() {
		return nativeFileDescriptor;
	}

	private static int nativeFileDescriptor(purejavacomm.SerialPort port) {
		PureJavaSerialPort pure = BasicUtil.castOrNull(PureJavaSerialPort.class, port);
		if (pure == null) return JnaUtil.INVALID_FILE_DESCRIPTOR;
		return pure.getNativeFileDescriptor();
	}

}
