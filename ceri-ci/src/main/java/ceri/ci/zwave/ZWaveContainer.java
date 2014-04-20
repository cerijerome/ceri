package ceri.ci.zwave;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.property.BaseProperties;
import ceri.zwave.veralite.VeraLite;

/**
 * Creates the zwave alerter.
 */
public class ZWaveContainer {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "zwave";
	public final ZWaveAlerter alerter;

	public ZWaveContainer(BaseProperties properties) {
		ZWaveAlerterProperties zwaveProperties = new ZWaveAlerterProperties(properties, GROUP);
		if (!zwaveProperties.enabled()) {
			logger.info("Zwave alerter disabled");
			alerter = null;
		} else {
			alerter = createAlerter(zwaveProperties);
		}
	}

	private ZWaveAlerter createAlerter(ZWaveAlerterProperties properties) {
		logger.info("Creating zwave controller");
		ZWaveController controller = new ZWaveController(new VeraLite(properties.host()));
		logger.info("Creating zwave alerter");
		ZWaveAlerter.Builder builder = ZWaveAlerter.builder(controller);
		for (String name : properties.names()) {
			Integer device = properties.device(name);
			builder.device(name, device);
		}
		return builder.build();
	}

}
