package ceri.serial.javax.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.io.IoStreamUtil;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.ResponseStream;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A test serial connector using a response stream to output data based on input.
 */
public class ResponseSerialConnector implements SerialConnector {
	private static final Logger logger = LogManager.getLogger();
	private final Listeners<State> listeners = new Listeners<>();
	private final ResponseStream stream;
	private final InputStream in;
	private final OutputStream out;
	private boolean breakBit;
	private boolean rts;
	private boolean dtr;
	private FlowControl flowControl;
	private boolean broken;

	public static ResponseSerialConnector of(ResponseStream stream) {
		return new ResponseSerialConnector(stream);
	}
	
	protected ResponseSerialConnector(ResponseStream stream) {
		this.stream = stream;
		in = IoStreamUtil.in(this::read);
		out = IoStreamUtil.out(this::write);
	}

	@Override
	public Listenable<State> listeners() {
		return listeners;
	}

	@Override
	public void broken() {
		logger.debug("broken()");
		broken = true;
		listeners.accept(State.broken);
	}

	@Override
	public void connect() {
		logger.debug("connect()");
	}

	@Override
	public void close() {
		logger.debug("close()");
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
	public void setBreakBit(boolean on) {
		logger.debug("setBreakBit({})", on);
		breakBit = on;
	}

	@Override
	public void setDtr(boolean on) {
		logger.debug("setDtr({})", on);
		dtr = on;
	}

	@Override
	public void setFlowControl(FlowControl flowControl) {
		logger.debug("setFlowControl({})", flowControl);
		this.flowControl = flowControl;
	}

	@Override
	public void setRts(boolean on) {
		logger.debug("setRts({})", on);
		rts = on;
	}

	public boolean getBreakBit() {
		return breakBit;
	}

	public boolean getRts() {
		return rts;
	}

	public boolean getDtr() {
		return dtr;
	}

	public FlowControl getFlowControl() {
		return flowControl;
	}

	public boolean isBroken() {
		return broken;
	}
	
	public void fixed() {
		broken = false;
		listeners.accept(State.fixed);
	}
	
	private int read(byte[] b, int offset, int len) throws IOException {
		logger.info("read([], {}, {})", offset, len);
		if (isBroken()) throw new IOException("Failed to read");
		if (stream.in().available() == 0) return 0;
		int count = stream.in().read(b, offset, len);
		BinaryPrinter.ASCII.print(b, offset, count);
		return count;
	}
	
	private void write(byte[] b, int offset, int len) throws IOException {
		logger.info("write([], {}, {})", offset, len);
		if (isBroken()) throw new IOException("Failed to write");
		stream.out().write(b, offset, len);
		BinaryPrinter.ASCII.print(b, offset, len);
	}
}
