package ceri.serial.i2c.util;

import static ceri.serial.i2c.I2cAddress.DEVICE_ID;
import static ceri.serial.i2c.I2cAddress.GENERAL_CALL;
import java.util.List;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cUtil;

public class I2cSlaveEmulator implements Consumer<List<I2cMessage>> {
	private static final Logger logger = LogManager.getLogger();
	public final I2cEmulator i2c;
	public final I2cAddress address;

	public static void main(String[] args) throws Exception {
		I2cEmulator i2c = new I2cEmulator();
		I2cAddress addr = I2cAddress.of10Bit(0x3ff);
		DeviceId deviceId = DeviceId.of(0xabc, 0x123, 0x5);
		System.out.println(deviceId);
		I2cSlaveEmulator slave = new I2cSlaveEmulator(i2c, addr) {
			@Override
			protected DeviceId deviceId() {
				return deviceId;
			}
		};
		System.out.println(i2c.deviceId(addr));
	}
	
	protected I2cSlaveEmulator(I2cEmulator i2c, I2cAddress address) {
		this.i2c = i2c;
		this.address = address;
		i2c.listeners.listen(this);
	}

	@Override
	public void accept(List<I2cMessage> messages) {
		try {
			if (messages.isEmpty()) return;
			if (processSingle(messages)) return;
			if (processRead(messages)) return;
			other(messages);
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	@SuppressWarnings("unused")
	protected void softwareReset() {}

	@SuppressWarnings("unused")
	protected DeviceId deviceId() {
		return null;
	}

	@SuppressWarnings("unused")
	protected void write(I2cMessage command) {}

	@SuppressWarnings("unused")
	protected void read(I2cMessage command, I2cMessage response) {}

	@SuppressWarnings("unused")
	protected void other(List<I2cMessage> messages) {}

	private boolean processRead(List<I2cMessage> messages) {
		if (messages.size() != 2) return false;
		I2cMessage command = messages.get(0);
		if (command.isRead()) return false;
		I2cMessage response = messages.get(1);
		if (!response.isRead()) return false;
		if (!command.isAddress(response.address)) return false; // must match address
		if (processDeviceId(command, response)) return true;
		if (!command.isAddress(address)) return false;
		read(command, response);
		return true;
	}

	private boolean processDeviceId(I2cMessage command, I2cMessage response) {
		if (!command.isAddress(DEVICE_ID)) return false;
		I2cAddress address = I2cAddress.fromFrames(command.in.copy(0));
		if (!this.address.equals(address)) return true; // not meant for me
		DeviceId deviceId = deviceId();
		if (deviceId != null) response.out.copyFrom(0, deviceId.encodeBytes());
		return true;
	}

	private boolean processSingle(List<I2cMessage> messages) {
		if (messages.size() != 1) return false;
		I2cMessage message = messages.get(0);
		if (processGeneralCall(message)) return true;
		if (!message.isAddress(address)) return false;
		if (message.isRead()) return false; // single read not supported
		write(message);
		return true;
	}

	private boolean processGeneralCall(I2cMessage message) {
		if (!GENERAL_CALL.equals(message.address)) return false;
		if (message.in.length() != 1) return false;
		if (message.in.getUbyte(0) != I2cUtil.SOFTWARE_RESET) return false;
		softwareReset();
		return true;
	}

}
