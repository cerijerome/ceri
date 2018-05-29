package ceri.serial.jssc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.io.StreamNotSetException;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import jssc.SerialPort;
import jssc.SerialPortException;

public class JsscSerialPort implements Closeable {
	// These exceptions types/messages signify the serial port is broken
	private static final List<Class<? extends Exception>> BROKEN_EXCEPTIONS = List.of( //
		StreamNotSetException.class);
	private static final Pattern BROKEN_MESSAGE_REGEX = Pattern.compile("(?i)failed");
	private final SerialPort serialPort;
	private final SerialPortInputStream in;
	private final SerialPortOutputStream out;

	public static JsscSerialPort open(String name) throws IOException {
		try {
			SerialPort serialPort = new SerialPort(name);
			if (!serialPort.openPort()) throw new IOException("Failed to open port: " + name);
			return new JsscSerialPort(serialPort);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
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

	private JsscSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
		in = new SerialPortInputStream(serialPort);
		out = new SerialPortOutputStream(serialPort);
	}

	public void setDTR(boolean state) throws IOException {
		try {
			serialPort.setDTR(state);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	public void setRTS(boolean state) throws IOException {
		try {
			serialPort.setRTS(state);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	public void purge(int flags) throws IOException {
		try {
			serialPort.purgePort(flags);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	public void setParams(int baud, int dataBits, int stopBits, int parity) throws IOException {
		try {
			if (serialPort.setParams(baud, dataBits, stopBits, parity)) return;
			throw new IOException("Failed to set port parameters: " + baud + ", " + dataBits +
				", " + stopBits + ", " + parity);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	public InputStream getInputStream() {
		return in;
	}

	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public void close() throws IOException {
		try {
			if (serialPort.closePort()) return;
			throw new IOException("Failed to close port " + serialPort.getPortName());
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}

}
