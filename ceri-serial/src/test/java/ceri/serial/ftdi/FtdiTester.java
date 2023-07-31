package ceri.serial.ftdi;

import java.io.IOException;
import ceri.serial.ftdi.test.FtdiTester;
import ceri.serial.ftdi.test.TestFtdi;
import ceri.serial.ftdi.util.SelfHealingFtdiConfig;
import ceri.serial.ftdi.util.SelfHealingFtdi;

public class FtdiTester {

	public static void main(String[] args) throws IOException {
		runEchoTest();
		//run("0x0403");
	}

	public static void runEchoTest() throws IOException {
		try (TestFtdi connector = TestFtdi.echoPins()) {
			connector.open();
			FtdiTester.test(connector, connector::fixed);
		}
	}

	public static void run(String finder) throws IOException {
		var config = SelfHealingFtdiConfig.of(finder);
		try (var connector = SelfHealingFtdi.of(config)) {
			connector.open();
			FtdiTester.test(connector, null);
		}
	}

}
