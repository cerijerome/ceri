package ceri.ci.zwave;

import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.property.BaseProperties;

/**
 * Creates the zwave alerter and zwave device group to sync with audio.
 */
public class ZWaveContainer {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "zwave";
	public final ZWaveAlerter alerter;
	public final ZWaveGroup group;

	public ZWaveContainer(BaseProperties properties) {
		this(properties, new ZWaveFactoryImpl());
	}

	public ZWaveContainer(BaseProperties properties, ZWaveFactory factory) {
		ZWaveProperties zwaveProperties = new ZWaveProperties(properties, GROUP);
		if (!zwaveProperties.enabled()) {
			logger.info("Zwave alerter disabled");
			alerter = null;
			group = null;
		} else {
			logger.info("Creating zwave controller");
			ZWaveController controller = factory.createController(zwaveProperties.host(),
				zwaveProperties.callDelayMs(), zwaveProperties.testMode());
			alerter = createAlerter(zwaveProperties, controller, factory);
			group = createGroup(zwaveProperties, controller, factory);
		}
	}

	private ZWaveGroup createGroup(ZWaveProperties properties, ZWaveController controller,
		ZWaveFactory factory) {
		Collection<Integer> devices = properties.groupDevices();
		if (devices == null) return null;
		logger.info("Creating zwave group");
		return factory.createGroup(controller, devices);
	}

	private ZWaveAlerter createAlerter(ZWaveProperties properties, ZWaveController controller,
		ZWaveFactory factory) {
		logger.info("Creating zwave alerter");
		ZWaveAlerter.Builder builder = factory.builder(controller);
		for (String name : properties.names()) {
			Integer device = properties.device(name);
			builder.device(name, device);
		}
		builder.randomize(properties.randomizeDelayMs(), properties.randomizePeriodMs());
		return builder.build();
	}

}
