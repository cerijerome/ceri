package ceri.x10.cm11a.device;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.StateChange;
import ceri.serial.javax.test.TestSerialConnector;

public class Cm11aConnectorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideNullConnector() throws IOException {
		Cm11aConnector con = Cm11aConnector.ofNull();
		assertEquals(con.in().read(), 0);
		con.out().write(0);
		try (var enclosed = con.listeners().enclose(sc -> {})) {}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadFromSerialConnector() throws IOException {
		try (var serial = TestSerialConnector.of()) {
			var con = Cm11aConnector.serial(serial);
			con.connect();
			serial.to.writeBytes(1, 2, 3).flush();
			assertRead(con.in(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteToSerialConnector() throws IOException {
		try (var serial = TestSerialConnector.of()) {
			var con = Cm11aConnector.serial(serial);
			con.connect();
			con.out().write(bytes(1, 2, 3));
			con.out().flush();
			assertRead(serial.from, 1, 2, 3);
		}
	}

	@Test
	public void shouldFailToReadFromBrokenSerialConnector() throws IOException {
		try (var serial = TestSerialConnector.of()) {
			var con = Cm11aConnector.serial(serial);
			con.connect();
			con.broken();
			serial.to.write(1);
			assertThrown(() -> con.in().read());
		}
	}

	@Test
	public void shouldListenForSerialConnectorState() throws IOException, InterruptedException {
		try (var serial = TestSerialConnector.of()) {
			var con = Cm11aConnector.serial(serial);
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var enclosed = con.listeners().enclose(sync::signal)) {
				con.connect();
				con.broken();
				assertEquals(sync.await(), StateChange.broken);
			}
		}
	}

}
