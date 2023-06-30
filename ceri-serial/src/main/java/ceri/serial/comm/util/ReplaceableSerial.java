package ceri.serial.comm;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import ceri.common.io.ReplaceableConnector;

/**
 * A serial port pass-through that allows the underlying serial port to be replaced.
 */
public class ReplaceableSerial extends ReplaceableConnector<Serial> implements Serial {

	public static ReplaceableSerial of() {
		return new ReplaceableSerial();
	}

	private ReplaceableSerial() {}

	@Override
	public String port() {
		return applyConnector(Serial::port);
	}

	@Override
	public void inBufferSize(int size) {
		acceptConnector(serial -> serial.inBufferSize(size));
	}

	@Override
	public int inBufferSize() {
		return applyConnector(Serial::inBufferSize, 0);
	}

	@Override
	public void outBufferSize(int size) {
		acceptConnector(serial -> serial.outBufferSize(size));
	}

	@Override
	public int outBufferSize() {
		return applyConnector(Serial::outBufferSize, 0);
	}

	@Override
	public void params(SerialParams params) throws IOException {
		acceptValidConnector(serial -> serial.params(params));
	}

	@Override
	public SerialParams params() {
		return applyConnector(Serial::params, SerialParams.NULL);
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		acceptValidConnector(serial -> serial.flowControl(flowControl));
	}

	@Override
	public Set<FlowControl> flowControl() {
		return applyConnector(Serial::flowControl);
	}

	@Override
	public void brk(boolean on) throws IOException {
		acceptValidConnector(serial -> serial.brk(on));
	}

	@Override
	public void rts(boolean on) throws IOException {
		acceptValidConnector(serial -> serial.rts(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		acceptValidConnector(serial -> serial.dtr(on));
	}

	@Override
	public boolean rts() throws IOException {
		return applyValidConnector(Serial::rts);
	}

	@Override
	public boolean dtr() throws IOException {
		return applyValidConnector(Serial::dtr);
	}

	@Override
	public boolean cd() throws IOException {
		return applyValidConnector(Serial::cd);
	}

	@Override
	public boolean cts() throws IOException {
		return applyValidConnector(Serial::cts);
	}

	@Override
	public boolean dsr() throws IOException {
		return applyValidConnector(Serial::dsr);
	}

	@Override
	public boolean ri() throws IOException {
		return applyValidConnector(Serial::ri);
	}
}
