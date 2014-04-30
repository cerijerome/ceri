package ceri.ci.x10;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;
import ceri.x10.cm11a.Cm11aConnector;
import ceri.x10.cm17a.Cm17aConnector;
import ceri.x10.util.X10Controller;
import ceri.x10.util.X10ControllerType;

/**
 * Creates the x10 alerter and its required components.
 */
public class X10Container implements Closeable {
	private final Logger logger = LogManager.getLogger();
	private static final String GROUP = "x10";
	private final Closeable connector;
	private final X10Controller controller;
	public final X10Alerter alerter;

	public X10Container(BaseProperties properties) throws IOException {
		this(properties, new X10FactoryImpl());
	}

	public X10Container(BaseProperties properties, X10Factory factory) throws IOException {
		X10Properties x10Properties = new X10Properties(properties, GROUP);
		if (!x10Properties.enabled()) {
			logger.info("X10 alerter disabled");
			connector = null;
			controller = null;
			alerter = null;
		} else if (x10Properties.controllerType() == X10ControllerType.cm11a) {
			logger.info("Creating CM11A connector");
			Cm11aConnector cm11aConnector = factory.createCm11aConnector(x10Properties.commPort());
			connector = cm11aConnector;
			logger.info("Creating CM11A controller");
			controller = factory.createCm11aController(cm11aConnector);
			alerter = createAlerter(x10Properties, factory, controller);
		} else {
			logger.info("Creating CM17A connector");
			Cm17aConnector cm17aConnector = factory.createCm17aConnector(x10Properties.commPort());
			connector = cm17aConnector;
			logger.info("Creating CM17A controller");
			controller = factory.createCm17aController(cm17aConnector);
			alerter = createAlerter(x10Properties, factory, controller);
		}
	}

	@Override
	public void close() {
		if (controller != null) {
			logger.info("Closing X10 controller");
			IoUtil.close(controller);
		}
		if (connector != null) {
			logger.info("Closing X10 connector");
			IoUtil.close(connector);
		}
	}

	private X10Alerter createAlerter(X10Properties properties, X10Factory factory,
		X10Controller controller) {
		logger.debug("Creating X10 alerter");
		X10Alerter.Builder builder = factory.builder(controller);
		for (String name : properties.names()) {
			String address = properties.address(name);
			builder.address(name, address);
		}
		return builder.build();
	}

}
