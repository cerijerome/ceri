package ceri.serial.comm.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.log.io.SelfHealingConnector;
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
public class SelfHealingSerial extends SelfHealingConnector<Serial> implements Serial.Fixable {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingSerialConfig config;
	private final SerialConfig.Builder serialConfig;

	public static SelfHealingSerial of(SelfHealingSerialConfig config) {
		return new SelfHealingSerial(config);
	}

	private SelfHealingSerial(SelfHealingSerialConfig config) {
		super(config.selfHealing);
		this.config = config;
		serialConfig = SerialConfig.builder(config.serial);
	}

	@Override
	public String port() {
		return device.applyIfSet(Serial::port, null);
	}

	@Override
	public void inBufferSize(int size) {
		serialConfig.inBufferSize(size);
		device.acceptIfSet(serial -> serial.inBufferSize(size));
	}

	@Override
	public int inBufferSize() {
		return serialConfig.inBufferSize;
	}

	@Override
	public void outBufferSize(int size) {
		serialConfig.outBufferSize(size);
		device.acceptIfSet(serial -> serial.outBufferSize(size));
	}

	@Override
	public int outBufferSize() {
		return serialConfig.outBufferSize;
	}

	@Override
	public void params(SerialParams params) throws IOException {
		serialConfig.params(params);
		device.acceptValid(serial -> serial.params(params));
	}

	@Override
	public SerialParams params() {
		return serialConfig.params;
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		serialConfig.flowControl(flowControl);
		device.acceptValid(serial -> serial.flowControl(flowControl));
	}

	@Override
	public Set<FlowControl> flowControl() {
		return serialConfig.flowControl;
	}

	@Override
	public void brk(boolean on) throws IOException {
		device.acceptValid(serial -> serial.brk(on));
	}

	@Override
	public void rts(boolean on) throws IOException {
		device.acceptValid(serial -> serial.rts(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		device.acceptValid(serial -> serial.dtr(on));
	}

	@Override
	public boolean rts() throws IOException {
		return device.applyValid(Serial::rts);
	}

	@Override
	public boolean dtr() throws IOException {
		return device.applyValid(Serial::dtr);
	}

	@Override
	public boolean cd() throws IOException {
		return device.applyValid(Serial::cd);
	}

	@Override
	public boolean cts() throws IOException {
		return device.applyValid(Serial::cts);
	}

	@Override
	public boolean dsr() throws IOException {
		return device.applyValid(Serial::dsr);
	}

	@Override
	public boolean ri() throws IOException {
		return device.applyValid(Serial::ri);
	}

	@SuppressWarnings("resource")
	@Override
	protected SerialPort openConnector() throws IOException {
		SerialPort serial = null;
		try {
			String port = config.portSupplier.get();
			serial = SerialPort.open(port);
			IoUtil.clear(serial.in());
			serialConfig.build().apply(serial);
			return serial;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(logger, serial);
			throw e;
		}
	}

}
