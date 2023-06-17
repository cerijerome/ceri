package ceri.common.io;

import java.io.IOException;
import org.junit.Test;

public class ConnectorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		try (var con = new Connector.Null()) {
			con.listeners().listen(x -> {});
			con.broken();
			con.open();
			con.name();
			con.in().read();
			con.out().write(0);
		}
	}

}
