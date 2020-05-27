package ceri.serial.mlx90640;

import static ceri.common.data.ByteArray.encoder;
import static ceri.common.data.ByteUtil.toMsb;
import static ceri.common.text.AnsiEscape.csi;
import static ceri.serial.mlx90640.Mlx90640.COLUMNS;
import static ceri.serial.mlx90640.MlxError.failedToStart;
import static ceri.serial.mlx90640.MlxError.i2cWriteFailed;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.sun.jna.Memory;
import ceri.common.color.ColorUtil;
import ceri.common.color.HsbColor;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteUtil;
import ceri.common.date.DateUtil;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.OsUtil;
import ceri.common.util.StartupValues;
import ceri.process.uptime.Uptime;
import ceri.serial.clib.jna.CException;
import ceri.serial.i2c.I2c;
import ceri.serial.i2c.I2cAddress;
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
	static final int RAM_ADDRESS = 0x0400;
	static final int EEPROM_ADDRESS = 0x2400;
	private static final int DEVICE_ID_ADDRESS = 0x2407;
	private static final int MELEXIS_I2C_ADDRESS = 0x240f;
	private static final int STATUS_REGISTER_ADDRESS = 0x8000;
	private static final int CONTROL_REGISTER1_ADDRESS = 0x800d;
	private static final int I2C_CONFIG_REGISTER_ADDRESS = 0x800f;
	private static final int RAM_WORDS = 0x0340;
	private static final int EEPROM_WORDS = 0x0340;
	private static final int DEVICE_ID_BYTES = 6;
	private static final int POWER_ON_WAIT_MIN_MS = 80;
	private static final int ABSOLUTE_TEMP_WAIT_MS = (int) TimeUnit.MINUTES.toMillis(4);
	private static final int SUBPAGES = 2;
	// configurable settings: (TODO: make config class)
	private static final int dataAvailablePollMicros = 500;
	private static final int resetDelayMicros = 100;
	private static final int maxPollRetries = 20;
	//
	private final I2c i2c;
	private final I2cAddress address;
	private final Memory writeBuffer = new Memory(Short.BYTES);
	private final Memory readBuffer = new Memory(RAM_WORDS * Short.BYTES);
	// Data buffer used for EEPROM data then frame data processing
	private final byte[] data = new byte[FrameData.WORDS * Short.BYTES];
	private CalibrationData calibrationData = null;
	private FrameData frameData = null;

	public static void main(String[] args) throws CException {
		StartupValues values = StartupValues.of(args);
		int device = values.next().asInt(0x33);
		int bus = values.next().asInt(1);
		try (I2c i2c = I2c.open(bus)) {
			System.out.println("Scan: " + i2c.scan7Bit());
			I2cAddress address = I2cAddress.of(device);
			Mlx90640 mlx = Mlx90640.of(i2c, address);
			System.out.printf("Power on reset wait %d ms%n", powerOnWaitMs(RefreshRate.DEFAULT));
			System.out.printf("Absolute temp wait %d ms%n", absoluteTempWaitMs());
			System.out.println("DeviceId: " + ByteUtil.toHex(mlx.deviceId(), "-"));
			mlx.init();
			System.out.println(mlx.statusRegister());
			ControlRegister1 cr1 = mlx.controlRegister1();
			System.out.println(cr1);
			mlx.controlRegister1(cr1.refreshRate(RefreshRate._16Hz));
			System.out.println(mlx.i2cConfigRegister());
			MlxFrame frame = MlxFrame.of();
			AnsiDisplay display = AnsiDisplay.builder(frame).min(25).max(38).build();
			while (true) {
				try {
					mlx.calculateTo(frame, 0.95, 22);
					display.print();
				} catch (CException e) {
					System.out.println(e.getMessage());
				}
				BasicUtil.delay(60);
			}
		}
	}

	/**
	 * Returns the time in milliseconds until data can be read from the device. Assumes device POR
	 * happened on system start, and uses system up-time to calculate.
	 */
	public static int powerOnWaitMs(RefreshRate refreshRate) {
		long ms = POWER_ON_WAIT_MIN_MS + SUBPAGES * refreshRate.timeMs();
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
	 * Return offset for pixel row and column (1-based).
	 */
	public static int px(int row, int column) {
		return (row - 1) * COLUMNS + column - 1;
	}

	/**
	 * Creates an instance, using given I2C bus controller, and address.
	 */
	public static Mlx90640 of(I2c i2c, I2cAddress address) {
		return new Mlx90640(i2c, address);
	}

	private Mlx90640(I2c i2c, I2cAddress address) {
		this.i2c = i2c;
		this.address = address;
	}

	/* Register access */

	public byte[] deviceId() throws CException {
		byte[] deviceId = new byte[DEVICE_ID_BYTES];
		readBytes(DEVICE_ID_ADDRESS, deviceId, 0, deviceId.length);
		return deviceId;
	}

	private StatusRegister statusRegister() throws CException {
		return StatusRegister.of(readRegister(STATUS_REGISTER_ADDRESS));
	}

	private void statusRegister(StatusRegister statusRegister) throws CException {
		writeRegister(STATUS_REGISTER_ADDRESS, statusRegister.value(), false);
	}

	public ControlRegister1 controlRegister1() throws CException {
		return ControlRegister1.of(readRegister(CONTROL_REGISTER1_ADDRESS));
	}

	public void controlRegister1(ControlRegister1 register) throws CException {
		writeRegister(CONTROL_REGISTER1_ADDRESS, register.value(), true);
	}

	public I2cConfigRegister i2cConfigRegister() throws CException {
		return I2cConfigRegister.of(readRegister(I2C_CONFIG_REGISTER_ADDRESS));
	}

	public void i2cConfigRegister(I2cConfigRegister register) throws CException {
		writeRegister(I2C_CONFIG_REGISTER_ADDRESS, register.value(), true);
	}

	/**
	 * Initialize device by reading calibration data from EEPROM.
	 */
	public void init() throws CException {
		readEepromData();
		calibrationData = EepromData.of(data).restoreCalibrationData();
	}

	/**
	 * Load RAM frame data and validate.
	 */
	public void loadFrame() throws CException {
		if (calibrationData == null) init();
		if (frameData == null) frameData = FrameData.of(data, calibrationData);
		readFrameData();
		frameData.init();
	}

	public void calculateTo(MlxFrame frame, double emissivity, double tr) throws CException {
		loadFrame();
		frameData.calculateTo(frame.values(), tr, emissivity);
		// frameData.fixBadPixels(frame.values());
		frame.setContext(frameData.mode(), frameData.vdd(), frameData.ta(), tr, emissivity);
	}

	public void getImage(MlxFrame frame) throws CException {
		loadFrame();
		frameData.getImage(frame.values());
		// frameData.fixBadPixels(frame.values());
	}

	private void syncFrame() throws CException {
		statusRegister(StatusRegister.startSync());
		pollForData(true);
	}

	private void triggerMeasurement() throws CException {
		ControlRegister1 controlRegister = controlRegister1();
		controlRegister.startMeasurement(true);
		controlRegister1(controlRegister);
		generalReset();
		controlRegister = controlRegister1();
		if (controlRegister.startMeasurement())
			throw MlxException.of(failedToStart, "Failed to start: " + controlRegister);
	}

	private void readEepromData() throws CException {
		readBytes(EepromData.ADDRESS, data, 0, EepromData.WORDS * Short.BYTES);
	}

	private void readFrameData() throws CException {
		StatusRegister statusRegister = pollForData(true);
		statusRegister(StatusRegister.startSync());
		int i = 0;
		i = readBytes(RAM_ADDRESS, data, 0, RAM_WORDS * Short.BYTES);
		i = readBytes(CONTROL_REGISTER1_ADDRESS, data, i, Short.BYTES);
		data[i + 1] = (byte) statusRegister.lastSubPageBit();
	}

	/**
	 * Poll status register until it shows data available (true/false). Implements a delay between
	 * retries, and a maximum retry count.
	 */
	private StatusRegister pollForData(boolean available) throws CException {
		for (int i = 0; i < maxPollRetries; i++) {
			if (i > 0) BasicUtil.delayMicros(dataAvailablePollMicros);
			StatusRegister statusRegister = statusRegister();
			if (statusRegister.dataAvailable() == available) return statusRegister;
		}
		throw CException.general("Data unavailable: %d attempts", maxPollRetries);
	}

	/**
	 * Send I2C software reset.
	 */
	private void generalReset() throws CException {
		i2c.softwareReset();
		BasicUtil.delayMicros(resetDelayMicros);
	}

	/**
	 * Write 16-bit value to 16-bit register, and verify it was written correctly.
	 */
	private void writeRegister(int register, int value, boolean validate) throws CException {
		i2c.writeData(address, encoder().writeShortMsb(register).writeShortMsb(value).bytes());
		BasicUtil.delayMicros(10);
		int actual = readRegister(register);
		if (!validate) return;
		if (actual != value) throw MlxException.of(i2cWriteFailed,
			"Value not written: 0x%x (expected 0x%x)", actual, value);
	}

	/**
	 * Read unsigned 16-bit value from 16-bit register.
	 */
	private int readRegister(int register) throws CException {
		byte[] data = new byte[Short.BYTES];
		readBytes(register, data, 0, data.length);
		return MathUtil.ushort(ByteUtil.fromMsb(data));
	}

	/**
	 * Read byte array from 16-bit register. Returns the byte array index after reading.
	 */
	private int readBytes(int register, byte[] data, int offset, int length) throws CException {
		JnaUtil.write(writeBuffer, toMsb(register, Short.BYTES));
		i2c.writeRead(address, writeBuffer, Short.BYTES, readBuffer, length);
		readBuffer.read(0, data, offset, length);
		return offset + length;
	}

}
