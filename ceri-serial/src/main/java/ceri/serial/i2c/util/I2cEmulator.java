package ceri.serial.i2c.util;

import static ceri.serial.i2c.jna.I2cDev.i2c_msg_flag.I2C_M_RD;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.sun.jna.Pointer;
import ceri.common.event.Listeners;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cUtil;
import ceri.serial.i2c.SmBus;
import ceri.serial.i2c.SmBusI2c;
import ceri.serial.i2c.jna.I2cDev.i2c_func;
import ceri.serial.i2c.jna.I2cDev.i2c_msg;

/**
 * I2C emulator, where listeners can register and respond to messages.
 */
public class I2cEmulator implements I2c {
	public final Listeners<List<I2cMessage>> listeners = new Listeners<>();
	private volatile boolean smBusPec = false;

	// TODO:
	// - how to generate exceptions?
	// - move to single slave?
	
	@Override
	public I2c retries(int count) {
		return this;
	}

	@Override
	public I2c timeout(int timeoutMs) {
		return this;
	}

	@Override
	public Collection<i2c_func> functions() {
		return i2c_func.xcoder.all();
	}

	public boolean smBusPec() {
		return smBusPec;
	}

	@Override
	public void smBusPec(boolean on) {
		smBusPec = on;
	}

	@Override
	public SmBus smBus(I2cAddress address, boolean useI2c) {
		return SmBusI2c.of(smBusSupport(), address);
	}

	/**
	 * I2C write using supplied memory buffer for ioctl.
	 */
	@Override
	public void write(I2cAddress address, int writeLen, Pointer writeBuf) throws IOException {
		i2c_msg.ByReference msg = new i2c_msg.ByReference();
		I2cUtil.populate(msg, address, writeLen, writeBuf);
		transfer(msg);
	}

	/**
	 * I2C write and read using supplied memory buffers for ioctl.
	 */
	@Override
	public void writeRead(I2cAddress address, Pointer writeBuf, int writeLen, Pointer readBuf,
		int readLen) throws IOException {
		i2c_msg.ByReference[] msgs = i2c_msg.array(2);
		I2cUtil.populate(msgs[0], address, writeLen, writeBuf);
		I2cUtil.populate(msgs[1], address, readLen, readBuf, I2C_M_RD);
		transfer(msgs);
	}

	@Override
	public void close() {}

	private void transfer(i2c_msg.ByReference... msgs) {
		var messages = Collections.unmodifiableList(I2cMessage.fromAll(msgs));
		listeners.accept(messages);
	}

	private SmBusI2c.I2cSupport smBusSupport() {
		return new SmBusI2c.I2cSupport() {
			@Override
			public void transfer(i2c_msg.ByReference... msgs) {
				I2cEmulator.this.transfer(msgs);
			}

			@Override
			public boolean smBusPec() {
				return I2cEmulator.this.smBusPec;
			}
		};
	}

}
