package ceri.serial.mlx90640;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.StartupValues;
import ceri.log.util.LogUtil;
import ceri.serial.i2c.I2cAddress;
import ceri.serial.i2c.util.I2cEmulator;
import ceri.serial.mlx90640.data.SampleData;
import ceri.serial.mlx90640.register.RefreshRate;
import ceri.serial.mlx90640.util.Mlx90650I2cEmulator;
import ceri.serial.mlx90640.util.TerminalDisplay;
import ceri.serial.mlx90640.util.TerminalDisplay.Type;

public class Mlx90640Tester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws Exception {
		StartupValues v = LogUtil.startupValues(args);
		RefreshRate refreshRate = v.next().apply(RefreshRate::valueOf, RefreshRate._4Hz);
		double emissivity = v.next().asDouble(1.0);
		Double min = v.next().asDouble();
		Double max = v.next().asDouble();
		Type displayType = v.next().apply(Type::valueOf, Type.ascii);
		SampleData data = v.next().apply(SampleData::valueOf, SampleData.ram0);
		int i2cFrequency = v.next().asInt(400000);

		I2cAddress address = I2cAddress.of7Bit(0x33);
		try (I2cEmulator i2c = I2cEmulator.of(i2cFrequency)) {
			Mlx90650I2cEmulator.builder().sample(data)
				.broken(351, 353, 415, 417, 500).build().addTo(i2c);
			Mlx90640 mlx = Mlx90640.of(i2c, address);
			MlxFrame frame = MlxFrame.of();
			TerminalDisplay display = TerminalDisplay.of(displayType, min, max);
			MlxRunner runner = MlxRunner.of(mlx);
			MlxRunnerConfig config = MlxRunnerConfig.builder().refreshRate(refreshRate)
				.resolution(data.resolution).pattern(data.pattern).emissivity(emissivity).build();
			logger.info("Starting: {}", config);
			runner.start(config);
			while (true) {
				runner.awaitFrame(frame);
				display.print(frame);
			}
		}
	}

}
