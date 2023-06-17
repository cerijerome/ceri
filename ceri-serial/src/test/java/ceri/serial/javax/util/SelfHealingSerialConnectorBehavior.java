package ceri.serial.javax.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.log.test.LogModifier;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.TestSerialPort;

public class SelfHealingSerialConnectorBehavior {
	private TestSerialPort serial;
	private LogModifier logMod;
	private SelfHealingSerialConnector con;

	@Before
	public void before() {
		serial = TestSerialPort.of();
		var config = SelfHealingSerialConfig.builder("com0")
			.brokenPredicate(IOException.class::isInstance).factory((p, n, t) -> serial.openPort(p))
			.fixRetryDelayMs(1).recoveryDelayMs(1).build();
		logMod = LogModifier.of(Level.OFF, SelfHealingSerialConnector.class);
		con = SelfHealingSerialConnector.of(config);
		assertFind(con.toString(), "\\bcom0\\b");
	}

	@After
	public void after() {
		con.close();
		logMod.close();
		serial.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadAndWriteBytes() throws IOException {
		con.connect();
		serial.in.to.writeBytes(1, 2, 3);
		assertRead(con.in(), 1, 2, 3);
		con.out().write(bytes(4, 5, 6));
		assertRead(serial.out.from, 4, 5, 6);
	}

	@Test
	public void shouldControlSerialSignals() throws IOException {
		assertThrown(() -> con.dtr(true)); // not connected
		con.connect();
		con.dtr(true);
		con.rts(false);
		con.flowControl(FlowControl.xonXoffIn);
		con.breakBit(true);
		con.breakBit(false);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakIfErrorMatch() throws IOException {
		con.connect();
		serial.open.assertAuto("com0");
		serial.in.read.error.setFrom(RTX);
		serial.in.to.writeBytes(0, 0, 0);
		assertThrown(RuntimeException.class, con.in()::read); // doesn't match
		serial.in.read.error.setFrom(IOX);
		assertThrown(IOException.class, con.in()::read); // match - broken
		assertThrown(IOException.class, con.in()::read); // match - already broken
	}

	@Test
	public void shouldRetryConnectingOnFailure() {
		serial.open.error.setFrom(IOX);
		assertThrown(con::connect);
		serial.open.assertAuto("com0");
		serial.open.assertAuto("com0"); // retry
		serial.open.assertAuto("com0"); // retry
	}

	@Test
	public void shouldNotifyWhenFixed() throws IOException {
		con.connect();
		serial.open.error.setFrom(IOX);
		con.broken();
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, false);
		try (var enc = con.listeners().enclose(sync::accept)) {
			serial.open.error.clear();
			sync.assertCall(StateChange.fixed);
		}
	}

	@Test
	public void shouldHandleListenerErrors() {
		serial.open.error.setFrom(IOX);
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
		try (var enc = con.listeners().enclose(sync::accept)) {
			sync.error.setFrom(RTX);
			con.broken(); // logs error
			sync.assertAuto(StateChange.broken);
			sync.error.setFrom(RIX);
			assertThrown(RuntimeInterruptedException.class, con::broken);
			sync.assertCall(StateChange.broken);
		}
	}

}
