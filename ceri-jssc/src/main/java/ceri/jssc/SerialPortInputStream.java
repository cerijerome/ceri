package ceri.jssc;

import java.io.IOException;
import java.io.InputStream;
import jssc.SerialPort;
import jssc.SerialPortException;
import ceri.common.collection.ArrayUtil;

public class SerialPortInputStream extends InputStream {
	private final SerialPort serialPort;
	private volatile boolean closed = false;

	public SerialPortInputStream(SerialPort serialPort) {
		if (serialPort == null) throw new NullPointerException();
		this.serialPort = serialPort;
	}

	@Override
	public int read() throws IOException {
		if (closed) return -1;
		byte[] b = readBytes(1);
		if (b.length > 0) return b[0] & 0xff;
		throw new IOException("Failed to read byte");
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
		if (closed) return -1;
		if (len == 0) return 0;
		int count = Math.min(len, available());
		byte[] data = readBytes(count);
		System.arraycopy(data, 0, b, off, data.length);
		return data.length;
	}

	@Override
	public int available() throws IOException {
		if (closed) return 0;
		try {
			return serialPort.getInputBufferBytesCount();
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		closed = true;
		try {
			serialPort.purgePort(SerialPort.PURGE_RXABORT);
		} catch (SerialPortException e) {
			throw new SerialPortIOException(e);
		}
	}

	private byte[] readBytes(int count) throws IOException {
		if (count == 0) return ArrayUtil.EMPTY_BYTE;
		try {
			byte[] data = serialPort.readBytes(count);
			if (data == null) return ArrayUtil.EMPTY_BYTE;
			return data;
		} catch (SerialPortException e) {
			throw new IOException(e);
		}
	}

}
