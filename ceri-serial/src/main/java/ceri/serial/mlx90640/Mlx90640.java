package ceri.serial.mlx90640;

import static ceri.common.data.ByteArray.encoder;
import static ceri.common.data.ByteUtil.toMsb;
import static ceri.serial.mlx90640.MlxError.*;
import static ceri.serial.mlx90640.ReadingPattern.*;
import static ceri.serial.mlx90640.RefreshRate.*;
import static ceri.serial.mlx90640.Resolution.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.sun.jna.Memory;
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
	private static final int DEVICE_ID_ADDRESS = 0x2407;
	private static final int MLX_I2C_ADDRESS = 0x240f;
	private static final int STATUS_REGISTER_ADDRESS = 0x8000;
	private static final int CONTROL_REGISTER1_ADDRESS = 0x800d;
	private static final int I2C_CONFIG_REGISTER_ADDRESS = 0x800f;
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
	private final int resetDelayMicros;
	private final int maxPollRetries;
	private final Memory writeBuffer = new Memory(Short.BYTES);
	private final Memory readBuffer = new Memory(RamData.WORDS * Short.BYTES);
	// Data buffer used for EEPROM data then frame data processing
	private final byte[] data = new byte[RamData.WORDS * Short.BYTES];
	private CalibrationData calibrationData = null;
	private RamData ramData = null;

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
			System.out.println("DeviceId: " + ByteUtil.toHex(mlx.deviceId(), "-"));
			MlxFrame frame = MlxFrame.of();
			AnsiDisplay display = AnsiDisplay.builder().min(10).max(40).build();
			MlxRunner runner = MlxRunner.of(mlx);
			runner.start(_8Hz, _19bit, chess, 1, null);
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
		int resetDelayMicros = 100;
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

		public Builder resetDelayMicros(int resetDelayMicros) {
			this.dataAvailablePollMicros = resetDelayMicros;
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

	/**
	 * Return offset for pixel row and column (1-based).
	 */
	public static int px(int row, int column) {
		return (row - 1) * COLUMNS + column - 1;
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
		resetDelayMicros = builder.resetDelayMicros;
		maxPollRetries = builder.maxPollRetries;
	}

	/* Register access */

	/**
	 * Set I2C address. This writes the address to EEPROM and will take effect from the next POR.
	 */
	public void saveAddress(I2cAddress address) throws IOException {
		if (this.address.equals(address)) return;
		MlxI2cAddress mlxAddress = MlxI2cAddress.of(readRegister(MLX_I2C_ADDRESS));
		writeRegister(MLX_I2C_ADDRESS, mlxAddress.address(address).value(), true);
	}

	/**
	 * Retrieve the unique device id.
	 */
	public byte[] deviceId() throws IOException {
		byte[] deviceId = new byte[DEVICE_ID_BYTES];
		readBytes(DEVICE_ID_ADDRESS, deviceId, 0, deviceId.length);
		return deviceId;
	}

	public StatusRegister status() throws IOException {
		return StatusRegister.of(readRegister(STATUS_REGISTER_ADDRESS));
	}

	public StatusRegister status(StatusRegister status) throws IOException {
		writeRegister(STATUS_REGISTER_ADDRESS, status.value(), false);
		return status;
	}

	public ControlRegister1 control1() throws IOException {
		return ControlRegister1.of(readRegister(CONTROL_REGISTER1_ADDRESS));
	}

	public ControlRegister1 control1(ControlRegister1 control1) throws IOException {
		writeRegister(CONTROL_REGISTER1_ADDRESS, control1.value(), false);
		return control1;
	}

	public I2cConfigRegister i2cConfig() throws IOException {
		return I2cConfigRegister.of(readRegister(I2C_CONFIG_REGISTER_ADDRESS));
	}

	public I2cConfigRegister i2cConfig(I2cConfigRegister i2cConfig) throws IOException {
		writeRegister(I2C_CONFIG_REGISTER_ADDRESS, i2cConfig.value(), false);
		return i2cConfig;
	}

	/**
	 * Initialize device by reading calibration data from EEPROM.
	 */
	public void init() throws IOException {
		readEepromData();
		calibrationData = EepromData.of(data).restoreCalibrationData();
		ramData = RamData.of(data, calibrationData, vdd0, ta0, trTaOffset);
	}

	/**
	 * Load RAM frame data and validate.
	 */
	public void loadFrame() throws IOException {
		StatusRegister status = readFrameData();
		ControlRegister1 control1 = control1();
		ramData.init(status.lastSubPageNumber(), control1);
	}

	public StatusRegister waitForData(int subPage) throws IOException {
		for (int i = 0; i < maxPollRetries; i++) {
			if (i > 0) BasicUtil.delayMicros(dataAvailablePollMicros);
			StatusRegister status = status();
			if (!status.dataAvailable()) continue;
			if (subPage == -1 || status.lastSubPageNumber() == subPage) return status;
			// if (status.dataAvailable()) return status;
		}
		throw new IOException("Data unavailable: " + maxPollRetries + " attempts");
	}

	public void loadFrame(int subPage, ControlRegister1 control1) throws IOException {
		readBytes(RamData.ADDRESS, data, 0, RamData.WORDS * Short.BYTES);
		ramData.init(subPage, control1);
	}

	public void calculateTo(MlxFrame frame, double emissivity, Double tr) throws IOException { 
		if (tr == null) tr = ramData.tr();
		ramData.calculateTo(frame.values(), tr, emissivity);
		// frameData.fixBadPixels(frame.values());
		frame.setContext(ramData.mode(), ramData.vdd(), ramData.ta(), tr, emissivity);
	}

	public void getImage(MlxFrame frame) throws IOException {
		loadFrame();
		ramData.getImage(frame.values());
		// frameData.fixBadPixels(frame.values());
	}

	private void syncFrame() throws IOException {
		status(StatusRegister.dataReset());
		pollForData(true);
	}

	private void triggerMeasurement() throws IOException {
		control1(control1().startMeasurement(true));
		generalReset();
		ControlRegister1 control1 = control1();
		if (control1.startMeasurement())
			throw MlxException.of(failedToStart, "Failed to start: " + control1);
	}

	private void readEepromData() throws IOException {
		readBytes(EepromData.ADDRESS, data, 0, EepromData.WORDS * Short.BYTES);
	}

	private StatusRegister readFrameData() throws IOException {
		StatusRegister status = pollForData(true);
		status(StatusRegister.dataReset());
		readBytes(RamData.ADDRESS, data, 0, RamData.WORDS * Short.BYTES);
		return status;
	}

	/**
	 * Poll status register until it shows data available (true/false). Implements a delay between
	 * retries, and a maximum retry count.
	 */
	private StatusRegister pollForData(boolean available) throws IOException {
		for (int i = 0; i < maxPollRetries; i++) {
			if (i > 0) BasicUtil.delayMicros(dataAvailablePollMicros);
			StatusRegister status = status();
			if (status.dataAvailable() == available) return status;
		}
		throw new IOException("Data unavailable: " + maxPollRetries + " attempts");
	}

	/**
	 * Send I2C software reset.
	 */
	private void generalReset() throws IOException {
		i2c.softwareReset();
		BasicUtil.delayMicros(resetDelayMicros);
	}

	/**
	 * Write 16-bit value to 16-bit register, and verify it was written correctly.
	 */
	private void writeRegister(int register, int value, boolean validate) throws IOException {
		if (EepromData.isEeprom(register)) writeEepromRegister(register, value);
		else writeRamRegister(register, value);
		int actual = readRegister(register);
		if (!validate) return;
		if (actual != value) throw MlxException.of(i2cWriteFailed,
			"Value not written to 0x%04x: 0x%x (expected 0x%x)", register, actual, value);
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
