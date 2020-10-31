package ceri.x10.cm11a.device;

import ceri.common.test.TestConnector;

public class Cm11aTestConnector extends TestConnector implements Cm11aConnector {

	public static Cm11aTestConnector of() {
		return new Cm11aTestConnector();
	}

	private Cm11aTestConnector() {}

}
