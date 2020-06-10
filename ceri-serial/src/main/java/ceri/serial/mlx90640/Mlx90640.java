package ceri.serial.mlx90640;

import static ceri.common.data.ByteArray.encoder;
import static ceri.common.data.ByteUtil.toMsb;
import static ceri.common.io.IoUtil.ioExceptionf;
import static ceri.serial.mlx90640.register.ReadingPattern.chess;
import static ceri.serial.mlx90640.register.RefreshRate._8Hz;
import static ceri.serial.mlx90640.register.Resolution._19bit;
import static ceri.serial.mlx90640.util.TerminalDisplay.Type.ansi;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.sun.jna.Memory;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.StartupValues;
import ceri.process.uptime.Uptime;
//import ceri.serial.clib.jna.CException;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.I2cDevice;
import ceri.serial.jna.JnaUtil;
import ceri.serial.mlx90640.data.CalibrationData;
import ceri.serial.mlx90640.data.EepromData;
import ceri.serial.mlx90640.data.RamData;
import ceri.serial.mlx90640.register.ControlRegister1;
import ceri.serial.mlx90640.register.I2cConfigRegister;
import ceri.serial.mlx90640.register.MlxI2cAddress;
import ceri.serial.mlx90640.register.RefreshRate;
import ceri.serial.mlx90640.register.StatusRegister;
import ceri.serial.mlx90640.util.TerminalDisplay;

/**
 * MLX90640 I2C controller class.
 * <p/>
 * To change I2C frequency on Raspberry Pi, edit /boot/config.txt for i2c_arm_baudrate. This example
 * sets baudrate to 400k, which seems to work well: dtparam=i2c_arm=on,i2c_arm_baudrate=400000
 */
public class Mlx90640 {
	public static final int ROWS = 24;
	public static final int COLUMNS = 32;
	public static final int PIXELS = ROWS * COLUMNS;
	public static final int SUBPAGES = 2;
	// Addresses
	public static final int RAM_START = 0x400;
	public static final int RAM_WORDS = 0x340;
	public static final int RAM_AUX = 0x700;
	public static final int EEPROM_START = 0x2400;
	public static final int EEPROM_WORDS = 0x340;
	public static final int EEPROM_DEVICE_ID = 0x2407;
	public static final int EEPROM_CONTROL_REGISTER_1 = 0x240c;
	public static final int EEPROM_I2C_CONF_REGISTER = 0x240e;
	public static final int EEPROM_I2C_ADDRESS = 0x240f;
	public static final int EEPROM_PIXEL_START = 0x2440;
	public static final int STATUS_REGISTER = 0x8000;
	public static final int CONTROL_REGISTER_1 = 0x800d;
	public static final int I2C_CONF_REGISTER = 0x800f;
	public static final int I2C_ADDRESS = 0x8010;
	//
	private static final int DEVICE_ID_BYTES = 6;
	private static final int POWER_ON_WAIT_MIN_MS = 80;
	private static final int ABSOLUTE_TEMP_WAIT_MS = (int) TimeUnit.MINUTES.toMillis(4);
	private final I2c i2c;
	private final I2cAddress address;
	private final double vdd0;
	private final double ta0;
	private final double trTaOffset;
	private final int eepromWriteMicros;
	private final int ramWriteMicros;
	private final int dataAvailablePollMicros;
	private final int maxPollRetries;
	// Data buffer used for EEPROM data then frame data processing
	private final byte[] data = new byte[RAM_WORDS * Short.BYTES];
	private final Memory writeBuffer = new Memory(Short.BYTES);
	private final Memory readBuffer = new Memory(data.length);
	private CalibrationData calibrationData = null;
	private RamData ramData = null;

	// TODO:
	// check write register verify yes/no
	// reset i2c if i2c error?
	// check mlx/adafruit algorithm for averages/fixes

	public static void main(String[] args) throws Exception {
		StartupValues values = StartupValues.of(args);
		int device = values.next().asInt(0x33);
		int bus = values.next().asInt(1);
		try (I2c i2c = I2cDevice.open(bus)) {
			System.out.println("Scan: " + i2c.scan7Bit());
			I2cAddress address = I2cAddress.of(device);
			Mlx90640 mlx = Mlx90640.of(i2c, address);
			System.out.printf("Power on reset wait %d ms%n", powerOnWaitMs(RefreshRate.DEFAULT));
			System.out.printf("Absolute temp wait %d ms%n", absoluteTempWaitMs());
			System.out.println("DeviceId: " + mlx.deviceId().toHex(0, "-"));
			MlxFrame frame = MlxFrame.of();
			TerminalDisplay display = TerminalDisplay.of(ansi, 25.0, 40.0);
			MlxRunner runner = MlxRunner.of(mlx);
			MlxRunnerConfig config = MlxRunnerConfig.builder().refreshRate(_8Hz).resolution(_19bit)
				.pattern(chess).emissivity(1.0).build();
			runner.start(config);
			while (true) {
				runner.awaitFrame(frame);
				display.print(frame);
			}
		}
	}

	/**
	 * Returns the time in milliseconds until data can be read from the device. Assumes device POR
	 * happened on system start, and uses system up-time to calculate.
	 */
	public static int powerOnWaitMs(RefreshRate refreshRate) {
		long ms = POWER_ON_WAIT_MIN_MS + SUBPAGES * refreshRate.timeMicros();
		return (int) Math.max(0, ms - Uptime.systemUptimeMs());
	}

	/**
	 * Returns the time in milliseconds until absolute temperature is available. Assumes device POR
	 * happened on system start, and uses system up-time to calculate.
	 */
	public static int absoluteTempWaitMs() {
		return (int) Math.max(0, ABSOLUTE_TEMP_WAIT_MS - Uptime.systemUptimeMs());
	}

	/**
	 * Creates an instance, using given I2C bus controller, and address.
	 */
	public static Mlx90640 of(I2c i2c, I2cAddress address) {
		return builder(i2c, address).build();
	}

	public static class Builder {
		final I2c i2c;
		final I2cAddress address;
		double vdd0 = 3.3;
		double ta0 = 25;
		double trTaOffset = -8;
		int eepromWriteMicros = 5000;
		int ramWriteMicros = 100;
		int dataAvailablePollMicros = 200;
		int maxPollRetries = 1000;

		Builder(I2c i2c, I2cAddress address) {
			this.i2c = i2c;
			this.address = address;
		}

		public Builder vdd0(double vdd0) {
			this.vdd0 = vdd0;
			return this;
		}

		public Builder ta0(double ta0) {
			this.ta0 = ta0;
			return this;
		}

		public Builder trTaOffset(double trTaOffset) {
			this.trTaOffset = trTaOffset;
			return this;
		}

		public Builder eepromWriteMicros(int eepromWriteMicros) {
			this.eepromWriteMicros = eepromWriteMicros;
			return this;
		}

		public Builder ramWriteMicros(int ramWriteMicros) {
			this.ramWriteMicros = ramWriteMicros;
			return this;
		}

		public Builder dataAvailablePollMicros(int dataAvailablePollMicros) {
			this.dataAvailablePollMicros = dataAvailablePollMicros;
			return this;
		}

		public Builder maxPollRetries(int maxPollRetries) {
			this.maxPollRetries = maxPollRetries;
			return this;
		}

		public Mlx90640 build() {
			return new Mlx90640(this);
		}
	}

	public static Builder builder(I2c i2c, I2cAddress address) {
		return new Builder(i2c, address);
	}

	private Mlx90640(Builder builder) {
		i2c = builder.i2c;
		address = builder.address;
		vdd0 = builder.vdd0;
		ta0 = builder.ta0;
		trTaOffset = builder.trTaOffset;
		eepromWriteMicros = builder.eepromWriteMicros;
		ramWriteMicros = builder.ramWriteMicros;
		dataAvailablePollMicros = builder.dataAvailablePollMicros;
		maxPollRetries = builder.maxPollRetries;
	}

	/* Register access */

	/**
	 * Set I2C address. This writes the address to EEPROM and will take effect from the next POR.
	 */
	public void saveAddress(I2cAddress address) throws IOException {
		if (this.address.equals(address)) return;
		MlxI2cAddress mlxAddress = MlxI2cAddress.of(readRegister(I2C_ADDRESS));
		writeRegister(EEPROM_I2C_ADDRESS, mlxAddress.address(address).value(), true);
	}

	/**
	 * Retrieve the unique device id.
	 */
	public ByteProvider deviceId() throws IOException {
		byte[] deviceId = new byte[DEVICE_ID_BYTES];
		readBytes(EEPROM_DEVICE_ID, deviceId, 0, deviceId.length);
		return ByteArray.Immutable.wrap(deviceId);
	}

	/**
	 * Read Status Register.
	 */
	public StatusRegister status() throws IOException {
		return StatusRegister.of(readRegister(STATUS_REGISTER));
	}

	/**
	 * Set Status Register.
	 */
	public StatusRegister status(StatusRegister status) throws IOException {
		writeRegister(STATUS_REGISTER, status.value(), false);
		return status;
	}

	/**
	 * Read Control Register 1.
	 */
	public ControlRegister1 control1() throws IOException {
		return ControlRegister1.of(readRegister(CONTROL_REGISTER_1));
	}

	/**
	 * Set Control Register 1.
	 */
	public ControlRegister1 control1(ControlRegister1 control1) throws IOException {
		writeRegister(CONTROL_REGISTER_1, control1.value(), false);
		return control1;
	}

	/**
	 * Save Control Register 1 to EEPROM.
	 */
	public ControlRegister1 saveControl1(ControlRegister1 control1) throws IOException {
		writeRegister(EEPROM_CONTROL_REGISTER_1, control1.value(), false);
		return control1;
	}

	/**
	 * Read I2C Config Register.
	 */
	public I2cConfigRegister i2cConfig() throws IOException {
		return I2cConfigRegister.of(readRegister(I2C_CONF_REGISTER));
	}

	/**
	 * Set I2C Config Register.
	 */
	public I2cConfigRegister i2cConfig(I2cConfigRegister i2cConfig) throws IOException {
		writeRegister(I2C_CONF_REGISTER, i2cConfig.value(), false);
		return i2cConfig;
	}

	/**
	 * Save I2C Config Register to EEPROM.
	 */
	public I2cConfigRegister saveI2cConfig(I2cConfigRegister i2cConfig) throws IOException {
		writeRegister(EEPROM_I2C_CONF_REGISTER, i2cConfig.value(), false);
		return i2cConfig;
	}

	/* Lifecycle control */

	/**
	 * Initialize device by reading calibration data from EEPROM.
	 */
	public void init() throws IOException {
		readEepromData();
		CalibrationData.Builder cal = CalibrationData.builder();
		cal.vdd0 = vdd0;
		cal.ta0 = ta0;
		cal.trTaOffset = trTaOffset;
		EepromData.of(data).restoreCalibrationData(cal);
		calibrationData = cal.build();
		ramData = RamData.of(data, calibrationData);
	}

	/**
	 * Sets status register to clear data available. Should be called before waitForFrame().
	 */
	public void clearFrame() throws IOException {
		status(StatusRegister.dataReset());
	}

	/**
	 * Wait until status register indicates new data is available.
	 */
	public StatusRegister waitForFrame() throws IOException {
		for (int i = 0; i < maxPollRetries; i++) {
			if (i > 0) BasicUtil.delayMicros(dataAvailablePollMicros);
			StatusRegister status = status();
			if (status.dataAvailable()) return status;
		}
		throw new IOException("Data unavailable: " + maxPollRetries + " attempts");
	}

	/**
	 * Load frame data from RAM. Should only be called when data is available.
	 */
	public void loadFrame(int subPage) throws IOException {
		ControlRegister1 control1 = control1();
		readBytes(RAM_START, data, 0, RAM_WORDS * Short.BYTES);
		ramData.init(subPage, control1);
	}

	/**
	 * Calculates absolute temperatures for the frame. Should be called after loadFrame().
	 */
	public void calculateTo(MlxFrame frame, double emissivity, Double tr) {
		if (tr == null) tr = ramData.tr();
		ramData.calculateTo(frame.values(), tr, emissivity);
		ramData.fixBadPixels(frame.values());
		frame.setContext(ramData.mode(), ramData.vdd(), ramData.ta(), tr, emissivity);
	}

	/* Support methods */

	private void readEepromData() throws IOException {
		readBytes(EEPROM_START, data, 0, EEPROM_WORDS * Short.BYTES);
	}

	/**
	 * Returns true if address is an EEPROM register.
	 */
	private static boolean isEeprom(int register) {
		return register >= EEPROM_START && register < EEPROM_START + EEPROM_WORDS;
	}

	/**
	 * Write 16-bit value to 16-bit register, and verify it was written correctly.
	 */
	private void writeRegister(int register, int value, boolean validate) throws IOException {
		if (isEeprom(register)) writeEepromRegister(register, value);
		else writeRamRegister(register, value);
		int actual = readRegister(register);
		if (!validate) return;
		if (actual != value) throw ioExceptionf("Value not written to 0x%04x: 0x%x (expected 0x%x)",
			register, actual, value);
	}

	/**
	 * Write 0 then 16-bit value to 16-bit eeprom register.
	 */
	private void writeEepromRegister(int register, int value) throws IOException {
		i2c.writeData(address, encoder().writeShortMsb(register).writeShortMsb(0).bytes());
		BasicUtil.delayMicros(eepromWriteMicros);
		i2c.writeData(address, encoder().writeShortMsb(register).writeShortMsb(value).bytes());
		BasicUtil.delayMicros(eepromWriteMicros);
	}

	/**
	 * Write 16-bit value to 16-bit RAM register and wait.
	 */
	private void writeRamRegister(int register, int value) throws IOException {
		i2c.writeData(address, encoder().writeShortMsb(register).writeShortMsb(value).bytes());
		BasicUtil.delayMicros(ramWriteMicros);
	}

	/**
	 * Read unsigned 16-bit value from 16-bit register.
	 */
	private int readRegister(int register) throws IOException {
		byte[] data = new byte[Short.BYTES];
		readBytes(register, data, 0, data.length);
		return MathUtil.ushort(ByteUtil.fromMsb(data));
	}

	/**
	 * Read byte array from 16-bit register. Returns the byte array index after reading.
	 */
	private int readBytes(int register, byte[] data, int offset, int length) throws IOException {
		JnaUtil.write(writeBuffer, toMsb(register, Short.BYTES));
		i2c.writeRead(address, writeBuffer, Short.BYTES, readBuffer, length);
		readBuffer.read(0, data, offset, length);
		return offset + length;
	}

}
