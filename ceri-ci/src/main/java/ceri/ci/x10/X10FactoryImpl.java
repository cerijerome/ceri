package ceri.ci.x10;

import java.io.IOException;
import ceri.ci.x10.X10Alerter.Builder;
import ceri.x10.cm11a.Cm11aConnector;
import ceri.x10.cm11a.Cm11aController;
import ceri.x10.cm11a.Cm11aSerialConnector;
import ceri.x10.cm17a.Cm17aConnector;
import ceri.x10.cm17a.Cm17aController;
import ceri.x10.cm17a.Cm17aSerialConnector;
import ceri.x10.util.X10Controller;

public class X10FactoryImpl implements X10Factory {

	@Override
	public Cm11aConnector createCm11aConnector(String commPort) throws IOException {
		return new Cm11aSerialConnector(commPort);
	}

	@Override
	public Cm11aController createCm11aController(Cm11aConnector connector) throws IOException {
		return new Cm11aController(connector, null);
	}

	@Override
	public Cm17aConnector createCm17aConnector(String commPort) throws IOException {
		return new Cm17aSerialConnector(commPort);
	}

	@Override
	public Cm17aController createCm17aController(Cm17aConnector connector) throws IOException {
		return new Cm17aController(connector, null);
	}

	@Override
	public Builder builder(X10Controller controller) {
		return X10Alerter.builder(controller);
	}

}
