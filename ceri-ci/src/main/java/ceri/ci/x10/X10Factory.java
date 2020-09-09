package ceri.ci.x10;

import java.io.IOException;
import ceri.x10.cm11a.device.Cm11aConnector;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm17a.device.Cm17aConnector;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.util.X10Controller;

/**
 * Creates the components for the X10 alerter.
 */
public interface X10Factory {

	Cm11aConnector createCm11aConnector(String commPort) throws IOException;
	Cm11aDevice createCm11aController(Cm11aConnector connector);
	Cm17aConnector createCm17aConnector(String commPort) throws IOException;
	Cm17aDevice createCm17aController(Cm17aConnector connector) throws IOException;
	X10Alerter.Builder builder(X10Controller controller);
	
}
