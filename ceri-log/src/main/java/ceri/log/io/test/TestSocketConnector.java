package ceri.log.io.test;

import java.io.IOException;
import java.io.OutputStream;
import ceri.common.test.TestConnector;
import ceri.log.io.SocketConnector;

/**
 * A connector for testing logic against socket connectors.
 */
public class TestSocketConnector extends TestConnector implements SocketConnector {

	/**
	 * Provide a test connector that echoes output to input.
	 */
	public static TestSocketConnector echo() {
		return new TestSocketConnector() {
			@Override
			protected void write(OutputStream out, byte[] b, int offset, int length)
				throws IOException {
				TestConnector.echo(this, b, offset, length);
			}
		};
	}

	public static TestSocketConnector of() {
		return new TestSocketConnector();
	}

	protected TestSocketConnector() {}

}
