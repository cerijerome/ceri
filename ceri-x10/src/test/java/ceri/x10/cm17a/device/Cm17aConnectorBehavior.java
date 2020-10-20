package ceri.x10.cm17a.device;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.TestListener;
import ceri.serial.javax.test.TestSerialConnector;

public class Cm17aConnectorBehavior {

	@Test
	public void shouldProvideNoOpConnector() throws IOException {
		Cm17aConnector con = Cm17aConnector.NULL;
		con.setDtr(true);
		con.setRts(false);
		try (var enc = con.listeners().enclose(x -> {})) {}
	}

	@Test
	public void shouldDelegateToSerialConnector() throws IOException, InterruptedException {
		try (TestSerialConnector serial = TestSerialConnector.of()) {
			Cm17aConnector.Serial con = Cm17aConnector.serial(serial);
			con.connect();
			serial.connect.awaitAuto();
			con.setDtr(false);
			serial.dtr.assertAuto(false);
			con.setRts(true);
			serial.rts.assertAuto(true);
			try (TestListener<StateChange> listener = TestListener.of(con.listeners())) {
				con.broken();
				assertEquals(listener.await(), StateChange.broken);
			}
		}
	}

}
