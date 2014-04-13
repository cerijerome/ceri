package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.x10.X10Alerter;
import ceri.ci.x10.X10AlerterProperties;
import ceri.common.io.IoUtil;
import ceri.x10.cm11a.Cm11aConnector;
import ceri.x10.cm11a.Cm11aController;
import ceri.x10.cm11a.Cm11aSerialConnector;
import ceri.x10.cm17a.Cm17aConnector;
import ceri.x10.cm17a.Cm17aController;
import ceri.x10.cm17a.Cm17aSerialConnector;
import ceri.x10.util.X10Controller;
import ceri.x10.util.X10ControllerType;

/**
 * Creates x10 alerter.
 */
public class X10 implements Closeable {
	private final Logger logger = LogManager.getLogger();
	private final Closeable connector;
	private final X10Controller controller;
	public final X10Alerter alerter;

	public X10(Properties properties, String prefix) throws IOException {
		X10AlerterProperties x10Properties = new X10AlerterProperties(properties, prefix, "x10");
		if (!x10Properties.enabled()) {
			connector = null;
			controller = null;
			alerter = null;
		} else if (x10Properties.controllerType() == X10ControllerType.cm11a) {
			Cm11aConnector cm11aConnector = new Cm11aSerialConnector(x10Properties.commPort());
			connector = cm11aConnector;
			controller = new Cm11aController(cm11aConnector, null);
			alerter = createAlerter(controller, x10Properties);
		} else {
			Cm17aConnector cm17aConnector = new Cm17aSerialConnector(x10Properties.commPort());
			connector = cm17aConnector;
			controller = new Cm17aController(cm17aConnector, null);
			alerter = createAlerter(controller, x10Properties);
		}
	}

	@Override
	public void close() throws IOException {
		if (controller != null) {
			logger.info("Closing x10Controller");
			IoUtil.close(controller);
		}
		if (connector != null) {
			logger.info("Closing x10Connector");
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
