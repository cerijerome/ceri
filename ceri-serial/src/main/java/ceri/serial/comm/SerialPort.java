package ceri.serial.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import ceri.jna.clib.CInputStream;
import ceri.jna.clib.COutputStream;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CUnistd;
import ceri.log.util.LogUtil;
import ceri.serial.comm.jna.CSerial;

/**
 * A simple serial port as a wrapper for CSerial functionality. Does not support event
 * notifications, receive threshold, or receive timeout from JavaComm api. I/O is non-blocking.
 */
public class SerialPort implements Serial {
	private static final Set<Integer> BROKEN_ERROR_CODES =
		CError.codes(CError.ENOENT, CError.ENXIO, CError.EBADF, CError.EACCES, CError.ENODEV);
	private final int fd;
	private final String port;
	private final CInputStream in;
	private final COutputStream out;
	private volatile SerialParams params = SerialParams.DEFAULT;
	private volatile int flowControlMode = CSerial.FLOWCONTROL_NONE;

	/**
	 * Determine if the serial port is unrecoverable, based on a thrown exception.
	 */
	public static boolean isBroken(Exception e) {
		if (!(e instanceof CException ce)) return false;
		return BROKEN_ERROR_CODES.contains(ce.code);
	}

	public static SerialPort open(String port) throws IOException {
		int fd = CSerial.open(port);
		return new SerialPort(fd, port);
	}

	private SerialPort(int fd, String port) {
		this.fd = fd;
		this.port = port;
		in = CInputStream.of(fd);
		out = createOut(fd);
	}

	@Override
	public String port() {
		return port;
	}

	@Override
	public void params(SerialParams params) throws IOException {
		CSerial.setParams(fd, params.baud, params.dataBits.bits, params.stopBits.value,
			params.parity.value);
		this.params = params;
	}

	@Override
	public SerialParams params() {
		return params;
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		int flowControlMode = FlowControl.encode(flowControl);
		CSerial.setFlowControl(fd, flowControlMode);
		this.flowControlMode = flowControlMode;
	}

	@Override
	public Set<FlowControl> flowControl() {
		return FlowControl.allFrom(flowControlMode);
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void inBufferSize(int size) {
		in.bufferSize(size);
	}

	@Override
	public int inBufferSize() {
		return in.bufferSize();
	}

	@Override
	public void outBufferSize(int size) {
		out.bufferSize(size);
	}

	@Override
	public int outBufferSize() {
		return out.bufferSize();
	}

	@Override
	public void brk(boolean enable) throws IOException {
		CSerial.brk(fd, enable);
	}

	@Override
	public void rts(boolean enable) throws IOException {
		CSerial.rts(fd, enable);
	}

	@Override
	public void dtr(boolean enable) throws IOException {
		CSerial.dtr(fd, enable);
	}

	@Override
	public boolean rts() throws IOException {
		return CSerial.rts(fd);
	}

	@Override
	public boolean dtr() throws IOException {
		return CSerial.dtr(fd);
	}

	@Override
	public boolean cd() throws IOException {
		return CSerial.cd(fd);
	}

	@Override
	public boolean cts() throws IOException {
		return CSerial.cts(fd);
	}

	@Override
	public boolean dsr() throws IOException {
		return CSerial.dsr(fd);
	}

	@Override
	public boolean ri() throws IOException {
		return CSerial.ri(fd);
	}

	@Override
	public void close() {
		LogUtil.close(in, out, () -> CUnistd.close(fd));
	}

	private COutputStream createOut(int fd) {
		return new COutputStream(fd) {
			@Override
			protected void flushBytes() throws IOException {
				CTermios.tcdrain(fd);
			}
		};
	}
}
