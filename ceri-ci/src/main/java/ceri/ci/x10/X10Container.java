package ceri.ci.x10;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.property.BaseProperties;
import ceri.log.util.LogUtil;
import ceri.x10.cm11a.Cm11aContainer;
import ceri.x10.cm17a.Cm17aContainer;
import ceri.x10.util.X10Controller;
import ceri.x10.util.X10ControllerType;

/**
 * Creates the x10 alerter and its required components.
 */
public class X10Container implements Closeable {
	private final Logger logger = LogManager.getLogger();
	private static final String GROUP = "x10";
	private final AutoCloseable container;
	private final X10Controller controller;
	public final X10Alerter alerter;

	public X10Container(BaseProperties properties) throws IOException {
		this(properties, new X10FactoryImpl());
	}

	public X10Container(BaseProperties properties, X10Factory factory) throws IOException {
		X10Properties x10Properties = new X10Properties(properties, GROUP);
		if (!x10Properties.enabled()) {
			logger.info("X10 alerter disabled");
			container = null;
			controller = null;
		} else if (x10Properties.controllerType() == X10ControllerType.cm11a) {
			logger.info("Creating CM11A container");
			Cm11aContainer cm11a = factory.createCm11aContainer(x10Properties.commPort());
			container = cm11a;
			controller = cm11a.cm11a();
		} else {
			logger.info("Creating CM17A container");
			Cm17aContainer cm17a = factory.createCm17aContainer(x10Properties.commPort());
			container = cm17a;
			controller = cm17a.cm17a();
		}
		alerter = controller == null ? null : createAlerter(x10Properties, factory, controller);
	}

	@Override
	public void close() {
		LogUtil.close(logger, container);
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
