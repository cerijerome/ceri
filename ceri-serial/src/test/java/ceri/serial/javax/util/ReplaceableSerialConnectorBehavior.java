package ceri.serial.javax.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrowable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.RTX;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.test.TestSerialConnector;

public class ReplaceableSerialConnectorBehavior {
	private TestSerialConnector serial;
	private ReplaceableSerialConnector con;

	@Before
	public void before() {
		serial = TestSerialConnector.echo();
		con = ReplaceableSerialConnector.of();
	}

	@After
	public void after() throws IOException {
		con.close();
		serial.close();
	}

	@Test
	public void shouldSetConnector() throws IOException {
		try (var serial1 = TestSerialConnector.of()) {
			assertThrown(con::connect);
			con.setConnector(serial);
			con.connect();
			serial.connect.assertAuto(true);
			con.setConnector(serial1);
			con.connect();
			serial.close.assertNoCall();
			serial1.connect.assertAuto(true);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideAccessToSerialConnector() throws IOException {
		con.setConnector(serial);
		con.connect();
		con.out().write(bytes(1, 2, 3));
		assertRead(con.in(), 1, 2, 3);
		con.breakBit(true);
		serial.breakBit.assertAuto(true);
		con.dtr(false);
		serial.dtr.assertAuto(false);
		con.rts(true);
		serial.rts.assertAuto(true);
		con.flowControl(FlowControl.xonXoffIn);
		serial.flowControl.assertAuto(FlowControl.xonXoffIn);
	}

	@Test
	public void shouldListenForStateChange() {
		CallSync.Accept<StateChange> sync = CallSync.consumer(null, true);
		try (var enc = con.listeners().enclose(sync::accept)) {
			con.setConnector(serial);
			con.broken();
			sync.assertAuto(StateChange.broken);
		}
	}

	@Test
	public void shouldListenForConnectorErrors() {
		CallSync.Accept<Exception> sync = CallSync.consumer(null, true);
		try (var enc = con.errorListeners().enclose(sync::accept)) {
			con.broken();
			assertThrowable(sync.awaitAuto(), IOException.class);
			con.setConnector(serial);
			serial.broken.error.setFrom(RTX);
			assertThrown(con::broken);
			assertThrowable(sync.awaitAuto(), RuntimeException.class);
		}
	}

	@Test
	public void shouldFailIfNotConnected() {
		assertThrown(() -> con.connect());
		assertThrown(() -> con.in().read());
		assertThrown(() -> con.out().write(0));
		assertThrown(() -> con.dtr(true));
		assertThrown(() -> con.rts(false));
	}

}
