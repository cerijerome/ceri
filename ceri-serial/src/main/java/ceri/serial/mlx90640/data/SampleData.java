package ceri.serial.mlx90640.data;

import static ceri.serial.mlx90640.register.ReadingPattern.chess;
import static ceri.serial.mlx90640.register.Resolution._19bit;
import java.io.IOException;
import ceri.common.io.IoUtil;
import ceri.serial.mlx90640.register.ReadingPattern;
import ceri.serial.mlx90640.register.Resolution;

public enum SampleData {
	ram0("eeprom.bin", "ram0.bin", _19bit, chess),
	ram1("eeprom.bin", "ram1.bin", _19bit, chess);

	private final String eepromPath;
	private final String ramPath;
	public final Resolution resolution;
	public final ReadingPattern pattern;

	private SampleData(String eepromPath, String ramPath, Resolution resolution,
		ReadingPattern pattern) {
		this.eepromPath = eepromPath;
		this.ramPath = ramPath;
		this.resolution = resolution;
		this.pattern = pattern;
	}

	public byte[] loadEeprom() throws IOException {
		return IoUtil.resource(SampleData.class, eepromPath);
	}

	public byte[] loadRam() throws IOException {
		return IoUtil.resource(SampleData.class, ramPath);
	}

}