package ceri.serial.javax;

import java.io.IOException;
import ceri.serial.javax.util.ResponseSerialConnector;
import ceri.serial.javax.util.SerialConnectorTester;

public class SerialTester {

	public static void main(String[] args) throws IOException {
		try (SerialConnector connector = ResponseSerialConnector.echo()) {
			connector.connect();
			SerialConnectorTester.test(connector);
		}
	}
	
}
