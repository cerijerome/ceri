package ceri.ci.zwave;

import java.io.IOException;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.zwave.ZWaveAlerter.Builder;
import ceri.zwave.veralite.VeraLite;

public class ZWaveFactoryImpl implements ZWaveFactory {
	static final Logger logger = LogManager.getLogger();

	@Override
	public ZWaveController createController(String host, int callDelayMs, boolean testMode) {
		if (testMode) logger.info("Creating ZWave controller in test mode");
		final VeraLite veraLite = testMode ? null : new VeraLite(host, callDelayMs);
		return new ZWaveController() {
			@Override
			public void on(int device) throws IOException {
				if (veraLite != null) veraLite.switchPower.on(device);
				logger.debug("Device {} on", device);
			}

			@Override
			public void off(int device) throws IOException {
				if (veraLite != null) veraLite.switchPower.off(device);
				logger.debug("Device {} off", device);
			}
		};
	}

	@Override
	public Builder builder(ZWaveController controller) {
		return ZWaveAlerter.builder(controller);
	}

	@Override
	public ZWaveGroup createGroup(ZWaveController controller, Collection<Integer> devices) {
		return new ZWaveGroup(controller, devices);
	}

}
