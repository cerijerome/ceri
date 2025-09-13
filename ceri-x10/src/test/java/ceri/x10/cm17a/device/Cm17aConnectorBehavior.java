package ceri.x10.cm17a.device;

import java.io.IOException;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.serial.comm.test.TestSerial;

public class Cm17aConnectorBehavior {

	@Test
	public void shouldProvideNoOpConnector() throws IOException {
		Cm17aConnector con = Cm17aConnector.NULL;
		con.dtr(true);
		con.rts(false);
		try (var _ = con.listeners().enclose(_ -> {})) {}
	}

	@Test
	public void shouldDelegateToSerialConnector() throws IOException {
		try (TestSerial serial = TestSerial.of(); Cm17aConnector con = Cm17aConnector.of(serial)) {
			con.open();
			serial.open.awaitAuto();
			con.dtr(false);
			serial.dtr.assertAuto(false);
			con.rts(true);
			serial.rts.assertAuto(true);
			CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
			try (var _ = con.listeners().enclose(sync::accept)) {
				con.broken();
				sync.assertCall(StateChange.broken);
			}
		}
	}
}
