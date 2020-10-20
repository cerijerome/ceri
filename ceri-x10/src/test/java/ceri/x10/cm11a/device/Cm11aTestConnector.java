package ceri.x10.cm11a.device;

import ceri.common.test.TestPipedConnector;

public class Cm11aTestConnector extends TestPipedConnector implements Cm11aConnector {

	public static Cm11aTestConnector of() {
		return new Cm11aTestConnector();
	}

	private Cm11aTestConnector() {}

}
