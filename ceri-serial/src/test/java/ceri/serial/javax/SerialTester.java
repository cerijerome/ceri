package ceri.serial.javax;

import java.io.IOException;
import ceri.serial.javax.test.SerialConnectorTester;
import ceri.serial.javax.test.TestSerialConnector;

public class SerialTester {

	public static void main(String[] args) throws IOException {
		try (TestSerialConnector serial = TestSerialConnector.echo()) {
			serial.open();
			SerialConnectorTester.test(serial, serial::fixed);
		}
	}

}
