package ceri.common.io;

import static ceri.common.test.Assert.assertRead;
import java.io.IOException;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
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
			assertRead(w.in(), 1, 2, 3);
			w.out().write(ArrayUtil.bytes.of(4, 5, 6));
			assertRead(con.out.from, 4, 5, 6);
		}
	}

}
