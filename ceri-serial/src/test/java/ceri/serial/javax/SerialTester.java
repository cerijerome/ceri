package ceri.serial.javax;

import java.io.IOException;
import ceri.common.test.ResponseStream;
import ceri.serial.javax.test.SerialConnectorTester;
import ceri.serial.javax.test.SerialTestConnector;

public class SerialTester {

	public static void main(String[] args) throws IOException {
		try (SerialTestConnector connector = SerialTestConnector.of()) {
			connector.response(ResponseStream.echo());
			connector.connect();
			SerialConnectorTester.test(connector, connector::fixed);
		}
	}

}
