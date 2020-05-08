package ceri.serial.mlx90640;

import static ceri.common.data.ByteArray.encoder;
import static ceri.common.data.ByteUtil.fromMsb;
import static ceri.common.data.ByteUtil.toMsb;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.text.StringUtil.HEX_BINARY_DIGITS;
import static ceri.common.validation.ValidationUtil.*;
import java.util.function.ToIntFunction;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.StartupValues;
import ceri.serial.clib.jna.CException;
import ceri.serial.i2c.DeviceId;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;

public class Mlx90640 {
	private static final int EEPROM_CONFIG_REGISTER = 0x2403;
	private static final int EEPROM_DEVICE_ID = 0x2407; // 6 bytes for ID1..3
	private static final int EEPROM_DEVICE_OPTIONS = 0x240a;
	private static final int EEPROM_CONTROL_REGISTER1 = 0x240c;
	private static final int EEPROM_CONTROL_REGISTER2 = 0x240d;
	private static final int EEPROM_I2C_CONFIG_REGISTER = 0x240e;
	private static final int EEPROM_I2C_ADDRESS = 0x240f;
	private static final int STATUS_REGISTER = 0x8000;
	private static final int CONTROL_REGISTER1 = 0x800d;
	private static final int I2C_CONFIG_REGISTER = 0x800f;
	private static final int DEVICE_ID_BYTES = 6;
	private static final int I2C_READ_LEN = 2048;
	private static final double SCALEALPHA = 0.000001;
	private static final int OPENAIR_TA_SHIFT = 8; // ambient temp shift?
	private final I2c i2c;
	private final I2cAddress address;

	public static void main(String[] args) throws CException {
		StartupValues values = StartupValues.of(args);
		int device = values.next().asInt(0x33);
		int bus = values.next().asInt(1);
		try (I2c i2c = I2c.open(bus)) {
			System.out.println("Scan: " + i2c.scan7Bit());
			I2cAddress address = I2cAddress.of(device);
			// i2c.softwareReset();
			// BasicUtil.delayMicros(200);
			// System.out.printf("DeviceID: %s%n", i2c.deviceId(address));
			byte[] bytes = i2c.readData(address, ByteUtil.toMsb(0x2407, 2), 6);
			System.out.println(ByteUtil.toHex(bytes, "-"));
			Mlx90640 mlx = Mlx90640.of(i2c, address);
			print("StatusRegister", mlx.statusRegister(), StatusRegister::encode);
			print("ControlRegister1", mlx.controlRegister1(), ControlRegister1::encode);
			print("I2cConfigRegister", mlx.i2cConfigRegister(), I2cConfigRegister::encode);
			print("EepromConfigRegister", mlx.eepromConfigRegister(), i -> i);
			print("EepromDeviceId", mlx.eepromDeviceId().toHex(0, "-"), i -> 0);
			print("EepromDeviceOptions", mlx.eepromDeviceOptions(), i -> i);
			print("EepromControlRegister1", mlx.eepromControlRegister1(), ControlRegister1::encode);
			print("EepromControlRegister2", mlx.eepromControlRegister2(), i -> i);
			print("EepromI2cConfigRegister", mlx.eepromI2cConfigRegister(),
				I2cConfigRegister::encode);
			print("EepromI2cAddress", mlx.eepromI2cAddress(), MlxI2cAddress::encode);
		}
	}

	private static <T> void print(String name, T t, ToIntFunction<T> toIntFn) {
		short value = (short) toIntFn.applyAsInt(t);
		System.out.printf("%25s: 0b%s 0x%s %s%n", name,
			StringUtil.toBinary(value, "_", HEX_BINARY_DIGITS), StringUtil.toHex(value), t);
	}

	public static Mlx90640 of(I2c i2c, I2cAddress address) {
		return new Mlx90640(i2c, address);
	}

	private Mlx90640(I2c i2c, I2cAddress address) {
		this.i2c = i2c;
		this.address = address;
	}

	public StatusRegister statusRegister() throws CException {
		return StatusRegister.decode(readRegister(STATUS_REGISTER));
	}

	public ControlRegister1 controlRegister1() throws CException {
		return ControlRegister1.decode(readRegister(CONTROL_REGISTER1));
	}

	public void controlRegister1(ControlRegister1 value) throws CException {
		writeRegister(CONTROL_REGISTER1, value.encode());
	}

	public I2cConfigRegister i2cConfigRegister() throws CException {
		return I2cConfigRegister.decode(readRegister(I2C_CONFIG_REGISTER));
	}

	public void i2cConfigRegister(I2cConfigRegister value) throws CException {
		writeRegister(I2C_CONFIG_REGISTER, value.encode());
	}

	public int eepromConfigRegister() throws CException {
		return readRegister(EEPROM_CONFIG_REGISTER);
	}

	public ByteProvider eepromDeviceId() throws CException {
		return Immutable.wrap(readRegister(EEPROM_DEVICE_ID, DEVICE_ID_BYTES));
	}

	public int eepromDeviceOptions() throws CException {
		return readRegister(EEPROM_DEVICE_OPTIONS);
	}

	public ControlRegister1 eepromControlRegister1() throws CException {
		return ControlRegister1.decode(readRegister(EEPROM_CONTROL_REGISTER1));
	}

	public int eepromControlRegister2() throws CException {
		return readRegister(EEPROM_CONTROL_REGISTER2);
	}

	public I2cConfigRegister eepromI2cConfigRegister() throws CException {
		return I2cConfigRegister.decode(readRegister(EEPROM_I2C_CONFIG_REGISTER));
	}

	public MlxI2cAddress eepromI2cAddress() throws CException {
		return MlxI2cAddress.decode(readRegister(EEPROM_I2C_ADDRESS));
	}

	/**
	 * Read 16-bit value from 16-bit register.
	 */
	private int readRegister(int register) throws CException {
		int value = ushort(fromMsb(readRegister(register, Short.BYTES)));
		System.out.printf("0x%04x = 0x%04x  ", register, value);
		return value;
	}

	/**
	 * Read byte array from 16-bit register.
	 */
	private byte[] readRegister(int register, int count) throws CException {
		return i2c.readData(address, toMsb(register, Short.BYTES), count);
	}

	/**
	 * Write 16-bit value to 16-bit register, and verify it was written correctly.
	 */
	private void writeRegister(int register, int value) throws CException {
		i2c.writeData(address, encoder().writeShortMsb(register).writeShortMsb(value).bytes());
		int actual = readRegister(register);
		if (actual != value)
			throw CException.general("Value not written: 0x%x (expected 0x%x)", actual, value);
	}

}
