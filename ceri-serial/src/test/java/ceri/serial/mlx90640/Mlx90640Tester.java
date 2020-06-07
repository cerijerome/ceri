package ceri.serial.mlx90640;

import ceri.common.util.StartupValues;
import ceri.log.util.LogUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cEmulator;
import ceri.serial.mlx90640.data.MlxTestData;
import ceri.serial.mlx90640.display.AnsiDisplay;
import ceri.serial.mlx90640.display.AsciiDisplay;
import ceri.serial.mlx90640.display.TerminalDisplay;
import ceri.serial.mlx90640.register.RefreshRate;
import ceri.serial.mlx90640.util.Mlx90650I2cEmulator;

public class Mlx90640Tester {
	private static final String TEMP = "temp";
	private static final String ASCII = "ascii";
	private static final String ANSI = "ansi";

	public static void main(String[] args) throws Exception {
		StartupValues v = LogUtil.startupValues(args);
		RefreshRate refreshRate = v.next().apply(RefreshRate::valueOf, RefreshRate._4Hz);
		double emissivity = v.next().asDouble(1.0);
		Double min = v.next().asDouble();
		Double max = v.next().asDouble();
		String displayType = v.next().get(ANSI);
		MlxTestData data = v.next().apply(MlxTestData::valueOf, MlxTestData.ram0);
		int i2cFrequency = v.next().asInt(400000);

		I2cAddress address = I2cAddress.of7Bit(0x33);
		try (I2cEmulator i2c = I2cEmulator.of(i2cFrequency)) {
			data.populate(Mlx90650I2cEmulator.builder()).build().addTo(i2c);
			Mlx90640 mlx = Mlx90640.of(i2c, address);
			MlxFrame frame = MlxFrame.of();
			TerminalDisplay display = display(displayType, min, max);
			MlxRunner runner = MlxRunner.of(mlx);
			runner.start(refreshRate, data.resolution, data.pattern, emissivity, null);
			while (true) {
				runner.awaitFrame(frame);
				display.print(frame);
			}
		}
	}

	private static TerminalDisplay display(String type, Double min, Double max) {
		TerminalDisplay.Builder b = displayBuilder(type);
		if (min != null) b.min(min);
		if (max != null) b.max(max);
		return b.build();
	}

	private static TerminalDisplay.Builder displayBuilder(String type) {
		if (TEMP.equals(type)) return TerminalDisplay.builder();
		if (ASCII.equals(type)) return AsciiDisplay.builder();
		return AnsiDisplay.builder();
	}

}
