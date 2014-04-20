package ceri.ci.x10;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;
import ceri.x10.cm11a.Cm11aConnector;
import ceri.x10.cm11a.Cm11aController;
import ceri.x10.cm11a.Cm11aSerialConnector;
import ceri.x10.cm17a.Cm17aConnector;
import ceri.x10.cm17a.Cm17aController;
import ceri.x10.cm17a.Cm17aSerialConnector;
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
		X10AlerterProperties x10Properties = new X10AlerterProperties(properties, GROUP);
		if (!x10Properties.enabled()) {
			logger.info("X10 alerter disabled");
			connector = null;
			controller = null;
			alerter = null;
		} else if (x10Properties.controllerType() == X10ControllerType.cm11a) {
			logger.info("Creating CM11A connector");
			Cm11aConnector cm11aConnector = new Cm11aSerialConnector(x10Properties.commPort());
			connector = cm11aConnector;
			logger.info("Creating CM11A controller");
			controller = new Cm11aController(cm11aConnector, null);
			alerter = createAlerter(controller, x10Properties);
		} else {
			logger.info("Creating CM17A connector");
			Cm17aConnector cm17aConnector = new Cm17aSerialConnector(x10Properties.commPort());
			connector = cm17aConnector;
			logger.info("Creating CM17A controller");
			controller = new Cm17aController(cm17aConnector, null);
			alerter = createAlerter(controller, x10Properties);
		}
	}

	@Override
	public void close() throws IOException {
		if (controller != null) {
			logger.info("Closing X10 controller");
			IoUtil.close(controller);
		}
		if (connector != null) {
			logger.info("Closing X10 connector");
			IoUtil.close(connector);
		}
	}

	private X10Alerter createAlerter(X10Controller controller, X10AlerterProperties properties) {
		logger.debug("Creating X10 alerter");
		X10Alerter.Builder builder = X10Alerter.builder(controller);
		for (String name : properties.names()) {
			String address = properties.address(name);
			builder.address(name, address);
		}
		return builder.build();
	}

}
