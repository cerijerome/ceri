package ceri.serial.mlx90640.util;

import static ceri.common.text.StringUtil.toHex;
import static ceri.common.util.ExceptionUtil.exceptionf;
import static ceri.common.validation.ValidationUtil.validateEqualL;
import static ceri.common.validation.ValidationUtil.validateMinL;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.serial.mlx90640.Mlx90640.CONTROL_REGISTER_1;
import static ceri.serial.mlx90640.Mlx90640.EEPROM_START;
import static ceri.serial.mlx90640.Mlx90640.EEPROM_WORDS;
import static ceri.serial.mlx90640.Mlx90640.RAM_START;
import static ceri.serial.mlx90640.Mlx90640.RAM_WORDS;
import static ceri.serial.mlx90640.Mlx90640.STATUS_REGISTER;
import static ceri.serial.mlx90640.Mlx90640.SUBPAGES;
import java.io.IOException;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.util.BasicUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cSlaveDeviceEmulator;
import ceri.serial.mlx90640.data.EepromData;
import ceri.serial.mlx90640.data.RamData;
import ceri.serial.mlx90640.register.ControlRegister1;
import ceri.serial.mlx90640.register.RefreshRate;
import ceri.serial.mlx90640.register.StatusRegister;
import ceri.serial.mlx90640.register.SubPage;

/**
 * Emulates MLX90640 I2C device using a EEPROM data and a set of RAM frames. Refresh rate is
 * observed, but other controls are not, such as pattern, sub-page mode/repeat, and resolution. RAM
 * frame data is based on sub-page repeat mode, chess pattern, and fixed resolution.
 */
public class Mlx90650I2cEmulator extends I2cSlaveDeviceEmulator {
	private final byte[] eepromData;
	private final byte[] ramFrames;
	private final int frameCount;
	private final StatusRegister status;
	private final ControlRegister1 control1;
	private long startMicros;
	private long periodMicros;
	private long dataAvailableMicros;

	public static class Builder {
		byte[] eepromData = null;
		byte[] ramFrames = null;
		I2cAddress address = I2cAddress.of7Bit(0x33);
		StatusRegister status = StatusRegister.of(0);
		ControlRegister1 control1 = ControlRegister1.ofDefault();

		Builder() {}

		public Builder eepromData(byte[] eepromData) {
			validateEqualL(eepromData.length, EepromData.BYTES);
			this.eepromData = eepromData;
			return this;
		}

		public Builder ramFrame(byte[] ramFrames) {
			validateMinL(eepromData.length, RamData.BYTES);
			validateEqualL(eepromData.length % RamData.BYTES, 0); // must be full frames
			this.ramFrames = ramFrames;
			return this;
		}

		public Builder address(I2cAddress address) {
			this.address = address;
			return this;
		}

		public Builder status(StatusRegister status) {
			this.status = status;
			return this;
		}

		public Builder control1(ControlRegister1 control1) {
			this.control1 = control1;
			return this;
		}

		public Mlx90650I2cEmulator build() {
			validateNotNull(eepromData);
			validateNotNull(ramFrames);
			return new Mlx90650I2cEmulator(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Mlx90650I2cEmulator(Builder builder) {
		super(builder.address);
		eepromData = builder.eepromData;
		ramFrames = builder.ramFrames;
		status = builder.status;
		control1 = builder.control1;
		frameCount = ramFrames.length / (RAM_WORDS * Short.BYTES);
		startMicros = BasicUtil.microTime();
		periodMicros = control1.refreshRate().timeMicros();
		dataAvailableMicros = startMicros + periodMicros;
	}

	@Override
	protected byte[] read(byte[] command, int readLen) throws IOException {
		if (command.length != Short.BYTES)
			throw exceptionf("Invalid read command: %s", toHex(command));
		if (readLen % Short.BYTES != 0) throw exceptionf("Invalid read length: %d", readLen);
		int address = (int) ByteUtil.fromMsb(command, 0, Short.BYTES);
		int words = readLen / Short.BYTES;
		if (words == 1) return ByteUtil.toMsb(readRegister(address), Short.BYTES);
		if (isEeprom(address, words)) return eepromData(address, words);
		if (isRam(address, words)) return ramData(address, words);
		throw exceptionf("Invalid read request: 0x%04x+0x%x", address, words);
	}

	@Override
	protected void write(byte[] command) throws IOException {
		if (command.length != Short.BYTES + Short.BYTES)
			throw exceptionf("Invalid write command: %s", toHex(command));
		int address = (int) ByteUtil.fromMsb(command, 0, Short.BYTES);
		int value = (int) ByteUtil.fromMsb(command, Short.BYTES, Short.BYTES);
		if (address == STATUS_REGISTER) setStatus(value);
		else if (address == CONTROL_REGISTER_1) setControl1(value);
		else throw exceptionf("Address not supported: 0x%04x", address);
	}

	private StatusRegister getStatus() {
		// Check if data is available, and determine sub-page
		long currentMicros = BasicUtil.microTime();
		boolean dataAvailable = currentMicros >= dataAvailableMicros;
		SubPage subPage = SubPage.from(period(currentMicros) % SUBPAGES);
		return StatusRegister.of(status.value()).dataAvailable(dataAvailable).lastSubPage(subPage);
	}

	private void setStatus(int value) {
		status.value(value);
		if (status.dataAvailable()) return;
		// Reset data available time if clearing flag
		int period = period(BasicUtil.microTime());
		dataAvailableMicros = startMicros + (period + 1) * periodMicros;
	}

	private void setControl1(int value) {
		RefreshRate previousRate = control1.refreshRate();
		control1.value(value);
		RefreshRate currentRate = control1.refreshRate();
		if (currentRate == previousRate) return;
		startMicros = BasicUtil.microTime();
		periodMicros = currentRate.timeMicros();
	}

	private int period(long currentMicros) {
		return (int) ((currentMicros - startMicros) / periodMicros);
	}

	private int readRegister(int address) {
		if (address == STATUS_REGISTER) return getStatus().value();
		if (address == CONTROL_REGISTER_1) return control1.value();
		throw exceptionf("Address not supported: 0x%04x", address);
	}

	private boolean isEeprom(int address, int words) {
		return ArrayUtil.isValidSlice(EEPROM_WORDS, address - EEPROM_START, words);
	}

	private boolean isRam(int address, int words) {
		return ArrayUtil.isValidSlice(RAM_WORDS, address - RAM_START, words);
	}

	private byte[] eepromData(int address, int words) {
		int index = (address - EEPROM_START) * Short.BYTES;
		if (index == 0 && words == EEPROM_WORDS) return eepromData;
		return ArrayUtil.copyOf(eepromData, index, words * Short.BYTES);
	}

	private byte[] ramData(int address, int words) {
		long currentMicros = BasicUtil.microTime();
		int frameIndex = period(currentMicros) % frameCount;
		int index = RamData.BYTES * frameIndex + (address - RAM_START) * Short.BYTES;
		return ArrayUtil.copyOf(ramFrames, index, words * Short.BYTES);
	}

}
