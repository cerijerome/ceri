package ceri.serial.mlx90640.data;

import static ceri.serial.mlx90640.register.ReadingPattern.chess;
import static ceri.serial.mlx90640.register.Resolution._19bit;
import java.io.IOException;
import ceri.common.io.IoUtil;
import ceri.serial.mlx90640.register.ReadingPattern;
import ceri.serial.mlx90640.register.Resolution;
import ceri.serial.mlx90640.util.Mlx90650I2cEmulator;

public enum MlxTestData {
	ram0("eeprom.bin", "ram0.bin", _19bit, chess),
	ram1("eeprom.bin", "ram1.bin", _19bit, chess);

	private final String eepromPath;
	private final String ramPath;
	public final Resolution resolution;
	public final ReadingPattern pattern;

	private MlxTestData(String eepromPath, String ramPath, Resolution resolution,
		ReadingPattern pattern) {
		this.eepromPath = eepromPath;
		this.ramPath = ramPath;
		this.resolution = resolution;
		this.pattern = pattern;
	}

	public byte[] loadEeprom() throws IOException {
		return IoUtil.resource(MlxTestData.class, eepromPath);
	}

	public byte[] loadRam() throws IOException {
		return IoUtil.resource(MlxTestData.class, ramPath);
	}

	public Mlx90650I2cEmulator.Builder populate(Mlx90650I2cEmulator.Builder b) throws IOException {
		return b.eepromData(loadEeprom()).ramFrame(loadRam());
	}

}