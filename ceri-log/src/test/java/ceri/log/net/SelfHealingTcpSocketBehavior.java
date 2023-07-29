package ceri.log.io;

import static ceri.common.test.AssertUtil.assertEquals;
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
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;
import ceri.common.test.CallSync;
import ceri.common.test.TestSocket;
import ceri.log.test.LogModifier;

public class SelfHealingSocketConnectorBehavior {
	private SelfHealingSocketConfig config;
	private TestSocket socket;
	private SelfHealingSocket con;

	@Before
	public void before() {
		socket = TestSocket.of();
		// Broken if IOException is thrown, not RuntimeException
		config = SelfHealingSocketConfig.builder("test", 123)
			.brokenPredicate(IOException.class::isInstance).fixRetryDelayMs(1).recoveryDelayMs(1)
			.factory(socket::connect).build();
		con = SelfHealingSocket.of(config);
	}

	@After
	public void after() {
		con.close();
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(con.toString(), "\\btest:123\\b");
	}

	@Test
	public void shouldProvideHostAndPort() {
		assertEquals(con.hostPort(), HostPort.of("test", 123));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		con.open();
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		assertRead(socket.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		con.open();
		socket.in.to.writeBytes(1, 2, 3);
		assertRead(con.in(), 1, 2, 3);
	}

	@Test
	public void shouldRetryOnConnectError() {
		LogModifier.run(() -> {
			socket.remote.error.setFrom(IOX);
			assertThrown(con::open);
			socket.remote.assertAuto(HostPort.of("test", 123));
			socket.remote.assertAuto(HostPort.of("test", 123));
			socket.remote.assertAuto(HostPort.of("test", 123));
		}, Level.OFF, SelfHealingSocket.class);
	}

	@Test
	public void shouldNotifyListenersOnFix() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = con.listeners().enclose(sync::signal)) {
			con.broken();
			sync.await(StateChange.fixed);
		}
	}

	@Test
	public void shouldLogOnNotifyError() {
		LogModifier.run(() -> {
			CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
			sync.error.setFrom(RTX);
			try (var enc = con.listeners().enclose(sync::accept)) {
				con.broken(); // error logged
				sync.assertAuto(StateChange.broken);
			}
		}, Level.OFF, SelfHealingSocket.class);
	}

	@Test
	public void shouldStopOnNotifyInterrupt() {
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
		sync.error.setFrom(RIX);
		try (var enc = con.listeners().enclose(sync::accept)) {
			assertThrown(RuntimeInterruptedException.class, con::broken);
			sync.assertAuto(StateChange.broken);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakOnIoError() throws InterruptedException, IOException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		con.open();
		socket.in.to.writeBytes(0, 0, 0);
		LogModifier.run(() -> {
			try (var enc = con.listeners().enclose(sync::signal)) {
				socket.in.read.error.setFrom(RTX);
				assertThrown(con.in()::read);
				socket.remote.error.setFrom(IOX);
				socket.in.read.error.setFrom(IOX);
				assertThrown(con.in()::read);
				sync.await(StateChange.broken);
				assertThrown(con.in()::read);
			}
			con.close(); // prevent logging error
		}, Level.OFF, SelfHealingSocket.class);
	}

}
