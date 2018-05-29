package ceri.serial.javax;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import com.sun.jna.Platform;
import ceri.common.io.IoUtil;
import ceri.common.io.StreamNotSetException;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.common.util.BasicUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.TermiosUtil;
import jtermios.Termios;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.PureJavaSerialPort;

public class SerialPort extends CommPort {
	public static final int CONNECTION_TIMEOUT_MS_DEF =
		CommPortIdentifier.CONNECTION_TIMEOUT_MS_DEF;
	private static final String UNKNOWN_OWNER = "Unknown";
	// These exceptions types/messages signify the serial port is broken
	private static final List<Class<? extends Exception>> BROKEN_EXCEPTIONS = List.of( //
		StreamNotSetException.class, NoSuchPortException.class,
		PureJavaIllegalStateException.class);
	private static final Pattern BROKEN_MESSAGE_REGEX =
		Pattern.compile("(?i)(?:device not configured|\\bioctl\\b)");
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

	/**
	 * Checks if an exception thrown from serial port or input/output activities means the serial
	 * port is down. Used by self-healing serial port.
	 */
	public static boolean isBroken(Exception e) {
		if (e == null) return false;
		if (ReflectUtil.instanceOfAny(e, BROKEN_EXCEPTIONS)) return true;
		return (RegexUtil.found(BROKEN_MESSAGE_REGEX, e.getMessage()) != null);
	}

	public static boolean messageMatches(Pattern pattern, Exception e) {
		String message = e.getMessage();
		if (message == null) return false;
		return pattern.matcher(message).find();
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

	public void setFlowControl(FlowControl flowControl) throws UnsupportedCommOperationException {
		try {
			serialPort.setFlowControlMode(flowControl.value);
		} catch (purejavacomm.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	/**
	 * Implementation-specific, usually a fixed 0.25/0.5s duration. Better to use set/clear break
	 * bit with a sleep in between.
	 */
	public void sendBreak(int duration) {
		serialPort.sendBreak(duration);
	}

	/**
	 * Starts a break (low signal). Make sure writing is complete or output will be overwritten.
	 */
	public void setBreakBit() throws IOException {
		JnaUtil.validateFileDescriptor(nativeFileDescriptor);
		TermiosUtil.setBreakBit(nativeFileDescriptor);
	}

	/**
	 * Stops a break and returns to a high signal (mark/idle).
	 */
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
