package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.test.TestConnector;

public class ReplaceableConnectorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldReplaceConnector() throws IOException {
		try (var con = new ReplaceableConnector.Fixable<TestConnector>()) {
			assertThrown(con::open);
			con.replace(TestConnector.of());
			con.open();
			assertEquals(con.in().available(), 0);
			con.out().write(0);
		}
	}

	@Test
	public void shouldListenForErrors() throws IOException {
		var captor = Captor.of();
		try (var rep = new ReplaceableConnector.Fixable<TestConnector>("test");
			var con = TestConnector.of(); var enc = rep.listeners().enclose(captor::accept)) {
			rep.replace(con);
			rep.broken();
			captor.verify(StateChange.broken);
		}
	}

}
