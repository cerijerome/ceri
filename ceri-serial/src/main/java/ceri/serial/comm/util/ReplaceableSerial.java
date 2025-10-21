package ceri.serial.comm.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import ceri.common.io.ReplaceableStream;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;

/**
 * A serial port pass-through that allows the underlying serial port to be replaced.
 */
public class ReplaceableSerial extends ReplaceableStream.Con<Serial> implements Serial {

	public static ReplaceableSerial of() {
		return new ReplaceableSerial();
	}

	private ReplaceableSerial() {
		super("serial");
	}

	@Override
	public String port() {
		return applyIfSet(Serial::port, null);
	}

	@Override
	public void inBufferSize(int size) {
		acceptIfSet(serial -> serial.inBufferSize(size));
	}

	@Override
	public int inBufferSize() {
		return applyIfSet(Serial::inBufferSize, 0);
	}

	@Override
	public void outBufferSize(int size) {
		acceptIfSet(serial -> serial.outBufferSize(size));
	}

	@Override
	public int outBufferSize() {
		return applyIfSet(Serial::outBufferSize, 0);
	}

	@Override
	public void params(SerialParams params) throws IOException {
		acceptValid(serial -> serial.params(params));
	}

	@Override
	public SerialParams params() throws IOException {
		return applyIfSet(Serial::params, SerialParams.NULL);
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		acceptValid(serial -> serial.flowControl(flowControl));
	}

	@Override
	public Set<FlowControl> flowControl() throws IOException {
		return applyIfSet(Serial::flowControl, Set.of());
	}

	@Override
	public void brk(boolean on) throws IOException {
		acceptValid(serial -> serial.brk(on));
	}

	@Override
	public void rts(boolean on) throws IOException {
		acceptValid(serial -> serial.rts(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		acceptValid(serial -> serial.dtr(on));
	}

	@Override
	public boolean rts() throws IOException {
		return applyValid(Serial::rts);
	}

	@Override
	public boolean dtr() throws IOException {
		return applyValid(Serial::dtr);
	}

	@Override
	public boolean cd() throws IOException {
		return applyValid(Serial::cd);
	}

	@Override
	public boolean cts() throws IOException {
		return applyValid(Serial::cts);
	}

	@Override
	public boolean dsr() throws IOException {
		return applyValid(Serial::dsr);
	}

	@Override
	public boolean ri() throws IOException {
		return applyValid(Serial::ri);
	}
}
