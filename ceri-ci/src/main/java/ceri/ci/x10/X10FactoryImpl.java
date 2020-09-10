package ceri.ci.x10;

import java.io.IOException;
import ceri.ci.x10.X10Alerter.Builder;
import ceri.x10.cm11a.Cm11aConfig;
import ceri.x10.cm11a.Cm11aContainer;
import ceri.x10.cm17a.Cm17aConfig;
import ceri.x10.cm17a.Cm17aContainer;
import ceri.x10.util.X10Controller;

public class X10FactoryImpl implements X10Factory {

	@Override
	public Cm11aContainer createCm11aContainer(String commPort) throws IOException {
		Cm11aConfig config = Cm11aConfig.of(commPort);
		return Cm11aContainer.of(config);
	}

	@Override
	public Cm17aContainer createCm17aContainer(String commPort) throws IOException {
		Cm17aConfig config = Cm17aConfig.of(commPort);
		return Cm17aContainer.of(config);
	}

	@Override
	public Builder builder(X10Controller controller) {
		return X10Alerter.builder(controller);
	}

}
