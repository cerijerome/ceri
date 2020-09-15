package ceri.x10.cm17a.device;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.TestListener;
import ceri.serial.javax.test.SerialTestConnector;

public class Cm17aConnectorBehavior {

	@Test
	public void shouldProvideNoOpConnector() throws IOException {
		Cm17aConnector con = Cm17aConnector.NULL;
		con.setDtr(true);
		con.setRts(false);
		try (TestListener<StateChange> listener = TestListener.of(con.listeners())) {}
	}

	@Test
	public void shouldDelegateToSerialConnector() throws IOException, InterruptedException {
		try (SerialTestConnector serial = SerialTestConnector.of()) {
			Cm17aConnector.Serial con = Cm17aConnector.serial(serial);
			con.connect();
			serial.connectSync.await();
			con.setDtr(false);
			assertThat(serial.dtrSync.await(), is(false));
			con.setRts(true);
			assertThat(serial.rtsSync.await(), is(true));
			try (TestListener<StateChange> listener = TestListener.of(con.listeners())) {
				con.broken();
				assertThat(listener.await(), is(StateChange.broken));
			}
		}
	}

}
