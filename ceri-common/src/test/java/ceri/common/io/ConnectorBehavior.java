package ceri.common.io;

import java.io.IOException;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.test.Assert;
import ceri.common.test.TestConnector;

public class ConnectorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		try (var con = new Connector.Null() {}) {
			con.listeners().listen(_ -> {});
			con.broken();
			con.open();
			con.name();
			con.in().read();
			con.out().write(0);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWrap() throws IOException {
		try (var con = TestConnector.of()) {
			con.in.to.writeBytes(1, 2, 3);
			var w = new Connector.Wrapper<>(con);
			w.open();
			Assert.read(w.in(), 1, 2, 3);
			w.out().write(Array.bytes.of(4, 5, 6));
			Assert.read(con.out.from, 4, 5, 6);
		}
	}
}
