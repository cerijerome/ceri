package ceri.serial.ftdi;

import java.io.IOException;
import ceri.serial.ftdi.test.FtdiConnectorTester;
import ceri.serial.ftdi.test.TestFtdiConnector;

public class FtdiTester {

	public static void main(String[] args) throws IOException {
		try (TestFtdiConnector connector = TestFtdiConnector.echoPins()) {
			connector.connect();
			FtdiConnectorTester.test(connector, connector::fixed);
		}
	}

}
