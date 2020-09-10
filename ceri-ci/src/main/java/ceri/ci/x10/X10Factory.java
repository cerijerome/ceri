package ceri.ci.x10;

import java.io.IOException;
import ceri.x10.cm11a.Cm11aContainer;
import ceri.x10.cm17a.Cm17aContainer;
import ceri.x10.util.X10Controller;

/**
 * Creates the components for the X10 alerter.
 */
public interface X10Factory {

	Cm11aContainer createCm11aContainer(String commPort) throws IOException;

	Cm17aContainer createCm17aContainer(String commPort) throws IOException;

	X10Alerter.Builder builder(X10Controller controller);

}
