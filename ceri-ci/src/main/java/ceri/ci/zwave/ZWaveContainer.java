package ceri.ci.zwave;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.property.BaseProperties;

/**
 * Creates the zwave alerter.
 */
public class ZWaveContainer {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "zwave";
	public final ZWaveAlerter alerter;

	public ZWaveContainer(BaseProperties properties) {
		this(properties, new ZWaveFactoryImpl());
	}

	public ZWaveContainer(BaseProperties properties, ZWaveFactory factory) {
		ZWaveProperties zwaveProperties = new ZWaveProperties(properties, GROUP);
		if (!zwaveProperties.enabled()) {
			logger.info("Zwave alerter disabled");
			alerter = null;
		} else {
			alerter = createAlerter(zwaveProperties, factory);
		}
	}

	private ZWaveAlerter createAlerter(ZWaveProperties properties, ZWaveFactory factory) {
		logger.info("Creating zwave controller");
		ZWaveController controller = factory.createController(properties.host());
		logger.info("Creating zwave alerter");
		ZWaveAlerter.Builder builder = factory.builder(controller);
		builder.alertDevices(properties.alertDevices());
		builder.alertTimeMs(properties.alertTimeMs());
		for (String name : properties.names()) {
			Integer device = properties.device(name);
			builder.device(name, device);
		}
		return builder.build();
	}

}
