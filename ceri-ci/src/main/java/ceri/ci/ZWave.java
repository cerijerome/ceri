package ceri.ci;

import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.ci.zwave.ZWaveAlerterProperties;
import ceri.ci.zwave.ZWaveController;
import ceri.zwave.veralite.VeraLite;

/**
 * Creates z-wave alerter.
 */
public class ZWave {
	private static final Logger logger = LogManager.getLogger();
	public final ZWaveAlerter alerter;

	public ZWave(Properties properties) {
		ZWaveAlerterProperties zwaveProperties = new ZWaveAlerterProperties(properties, "zwave");
		if (!zwaveProperties.enabled()) {
			alerter = null;
		} else {
			ZWaveController controller = new ZWaveController(new VeraLite(zwaveProperties.host()));
			alerter = createAlerter(controller, zwaveProperties);
		}
	}

	private ZWaveAlerter
		createAlerter(ZWaveController controller, ZWaveAlerterProperties properties) {
		logger.debug("Creating ZWave alerter");
		ZWaveAlerter.Builder builder = ZWaveAlerter.builder(controller);
		for (String name : properties.names()) {
			Integer device = properties.device(name);
			builder.device(name, device);
		}
		return builder.build();
	}

}
