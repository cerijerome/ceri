package ceri.serial.javax.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.StateChange;
import ceri.common.test.ResponseStream;
import ceri.common.text.ToStringHelper;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * A test serial connector using a response stream to output data based on input received.
 */
public class ResponseSerialConnector implements SerialConnector {
	private static final Logger logger = LogManager.getLogger();
	private final Listeners<StateChange> listeners = new Listeners<>();
	private final ResponseStream stream;
	private final InputStream in;
	private final OutputStream out;
	private boolean breakBit;
	private boolean rts;
	private boolean dtr;
	private FlowControl flowControl;
	private volatile boolean connected = false;
	private volatile boolean broken;

	public static ResponseSerialConnector echo() {
		return of(ResponseStream.echo());
	}

	public static ResponseSerialConnector of(ResponseStream stream) {
		return new ResponseSerialConnector(stream);
	}

	private ResponseSerialConnector(ResponseStream stream) {
		this.stream = stream;
		in = IoStreamUtil.in(this::read, this::available);
		out = IoStreamUtil.out(this::write);
		logger.debug("Created");
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void broken() {
		logger.debug("broken()");
		broken = true;
		connected = false;
		listeners.accept(StateChange.broken);
	}

	@Override
	public void connect() throws IOException {
		logger.debug("connect()");
		verifyUnbroken();
		connected = true;
	}

	@Override
	public void close() {
		logger.debug("close()");
		connected = false;
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
	public void setBreakBit(boolean on) throws IOException {
		logger.debug("setBreakBit({})", on);
		verifyUnbroken();
		verifyConnected();
		breakBit = on;
	}

	@Override
	public void setDtr(boolean on) throws IOException {
		logger.debug("setDtr({})", on);
		verifyUnbroken();
		verifyConnected();
		dtr = on;
	}

	@Override
	public void setFlowControl(FlowControl flowControl) throws IOException {
		logger.debug("setFlowControl({})", flowControl);
		verifyUnbroken();
		verifyConnected();
		this.flowControl = flowControl;
	}

	@Override
	public void setRts(boolean on) throws IOException {
		logger.debug("setRts({})", on);
		verifyUnbroken();
		verifyConnected();
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
		logger.debug("fixed()");
		broken = false;
		connected = true;
		listeners.accept(StateChange.fixed);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, stream).toString();
	}

	@SuppressWarnings("resource")
	private int available() throws IOException {
		logger.debug("available()");
		verifyUnbroken();
		verifyConnected();
		return stream.in().available();
	}

	@SuppressWarnings("resource")
	private int read(byte[] b, int offset, int len) throws IOException {
		logger.debug("read([], {}, {})", offset, len);
		verifyUnbroken();
		verifyConnected();
		if (stream.in().available() == 0) return 0;
		return stream.in().read(b, offset, len);
	}

	@SuppressWarnings("resource")
	private void write(byte[] b, int offset, int len) throws IOException {
		logger.debug("write([], {}, {})", offset, len);
		verifyUnbroken();
		verifyConnected();
		stream.out().write(b, offset, len);
	}

	private void verifyConnected() throws IOException {
		if (!connected) throw new IOException("Not connected");
	}

	private void verifyUnbroken() throws IOException {
		if (isBroken()) throw new IOException("Connector is broken");
	}

}
