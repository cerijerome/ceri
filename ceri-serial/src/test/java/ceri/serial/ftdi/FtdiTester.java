package ceri.serial.ftdi;

import java.io.IOException;
import ceri.serial.ftdi.test.FtdiConnectorTester;
import ceri.serial.ftdi.test.TestFtdiConnector;
import ceri.serial.ftdi.util.SelfHealingFtdiConfig;
import ceri.serial.ftdi.util.SelfHealingFtdiConnector;

public class FtdiTester {

	public static void main(String[] args) throws IOException {
		//runEchoTest();
		run("0x0403");
	}

	public static void runEchoTest() throws IOException {
		try (TestFtdiConnector connector = TestFtdiConnector.echoPins()) {
			connector.connect();
			FtdiConnectorTester.test(connector, connector::fixed);
		}
	}

	public static void run(String finder) throws IOException {
		var config = SelfHealingFtdiConfig.of(finder);
		try (var connector = SelfHealingFtdiConnector.of(config)) {
			connector.connect();
			FtdiConnectorTester.test(connector, null);
		}
	}

}
