package ceri.serial.jssc;

import java.io.IOException;
import java.io.OutputStream;
import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialPortOutputStream extends OutputStream {
	private final SerialPort serialPort;
	private volatile boolean closed = false;

	public SerialPortOutputStream(SerialPort serialPort) {
		if (serialPort == null) throw new NullPointerException();
		this.serialPort = serialPort;
	}

	@Override
	public void write(int b) throws IOException {
		try {
			if (serialPort.writeInt(b)) return;
			throw new IOException("Failed to write byte to port " + serialPort.getPortName());
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
		if (closed || len == 0) return;
		try {
			byte[] data = new byte[len];
			System.arraycopy(b, off, data, 0, len);
			if (serialPort.writeBytes(data)) return;
			throw new IOException("Failed to write bytes to port " + serialPort.getPortName());
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		closed = true;
		try {
			serialPort.purgePort(SerialPort.PURGE_TXABORT);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

}
