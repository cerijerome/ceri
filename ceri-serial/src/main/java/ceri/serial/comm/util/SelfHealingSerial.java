package ceri.serial.comm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.io.IoUtil;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.SerialPort;

/**
 * A self-healing serial port. It will automatically reconnect on fatal errors, such as if the cable
 * is removed and replaced. USB-to-serial connector device names can change after disconnecting and
 * reconnecting. The PortSupplier interface can be used to provide handling logic in this case.
 */
public class SelfHealingSerial extends LoopingExecutor implements Serial.Fixable {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingSerialConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.of();
	private volatile SerialConfig serialConfig;
	private volatile SerialPort serialPort;

	public static SelfHealingSerial of(SelfHealingSerialConfig config) {
		return new SelfHealingSerial(config);
	}

	private SelfHealingSerial(SelfHealingSerialConfig config) {
		this.config = config;
		this.serialConfig = config.serial;
		in.listeners().listen(this::checkIfBroken);
		out.listeners().listen(this::checkIfBroken);
		start();
	}

	@Override
	public void broken() {
		setBroken();
	}

	@Override
	public void open() throws IOException {
		try {
			initSerialPort();
		} catch (IOException e) {
			broken();
			throw e;
		}
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public String port() {
		return Serial.port(serialPort);
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
		serialConfig = SerialConfig.builder(serialConfig).inBufferSize(size).build();
		var serialPort = this.serialPort;
		if (serialPort != null) serialPort.inBufferSize(size);
	}

	@Override
	public int inBufferSize() {
		return serialConfig.inBufferSize;
	}

	@Override
	public void outBufferSize(int size) {
		serialConfig = SerialConfig.builder(serialConfig).outBufferSize(size).build();
		var serialPort = this.serialPort;
		if (serialPort != null) serialPort.outBufferSize(size);
	}

	@Override
	public int outBufferSize() {
		return serialConfig.outBufferSize;
	}

	@Override
	public void params(SerialParams params) throws IOException {
		serialConfig = SerialConfig.builder(serialConfig).params(params).build();
		exec(serialPort -> serialPort.params(params));
	}

	@Override
	public SerialParams params() {
		return serialConfig.params;
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		serialConfig = SerialConfig.builder(serialConfig).flowControl(flowControl).build();
		exec(serialPort -> serialPort.flowControl(flowControl));
	}

	@Override
	public Set<FlowControl> flowControl() {
		return serialConfig.flowControl;
	}

	@Override
	public void brk(boolean on) throws IOException {
		exec(serialPort -> serialPort.brk(on));
	}

	@Override
	public void rts(boolean on) throws IOException {
		exec(serialPort -> serialPort.rts(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		exec(serialPort -> serialPort.dtr(on));
	}

	@Override
	public boolean rts() throws IOException {
		return execGet(SerialPort::rts);
	}

	@Override
	public boolean dtr() throws IOException {
		return execGet(SerialPort::dtr);
	}

	@Override
	public boolean cd() throws IOException {
		return execGet(SerialPort::cd);
	}

	@Override
	public boolean cts() throws IOException {
		return execGet(SerialPort::cts);
	}

	@Override
	public boolean dsr() throws IOException {
		return execGet(SerialPort::dsr);
	}

	@Override
	public boolean ri() throws IOException {
		return execGet(SerialPort::ri);
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, serialPort);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, config);
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("Connection is broken - attempting to fix");
		fixSerialPort();
		logger.info("Connection is now fixed");
		// wait for streams to recover before clearing
		ConcurrentUtil.delay(config.recoveryDelayMs);
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixSerialPort() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initSerialPort();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to fix connection, retrying: {}", e);
				ConcurrentUtil.delay(config.fixRetryDelayMs);
			}
		}
	}

	private void notifyListeners(StateChange state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void checkIfBroken(Exception e) {
		if (!config.brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(StateChange.broken);
	}

	@SuppressWarnings("resource")
	private void initSerialPort() throws IOException {
		LogUtil.close(logger, serialPort);
		serialPort = null;
		serialPort = openSerialPort(serialConfig);
		logger.debug("Connected to {}", serialPort.port());
		IoUtil.clear(serialPort.in());
		in.setInputStream(serialPort.in());
		out.setOutputStream(serialPort.out());		
	}

	private SerialPort openSerialPort(SerialConfig serialConfig) throws IOException {
		SerialPort serialPort = null;
		try {
			String port = config.portSupplier.get();
			serialPort = SerialPort.open(port);
			serialConfig.apply(serialPort);
			return serialPort;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(logger, serialPort);
			throw e;
		}
	}

	private void exec(ExceptionConsumer<IOException, SerialPort> consumer) throws IOException {
		execGet(serialPort -> {
			consumer.accept(serialPort);
			return null;
		});
	}

	@SuppressWarnings("resource")
	private <T> T execGet(ExceptionFunction<IOException, SerialPort, T> function)
		throws IOException {
		try {
			return function.apply(serialPort());
		} catch (RuntimeException | IOException e) {
			checkIfBroken(e);
			throw e;
		}
	}

	private SerialPort serialPort() throws IOException {
		SerialPort serialPort = this.serialPort;
		if (serialPort == null) throw new IOException("Serial port unavailable");
		return serialPort;
	}

}
