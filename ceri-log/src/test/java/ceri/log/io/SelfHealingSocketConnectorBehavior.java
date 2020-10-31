package ceri.log.io;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorProducer.IOX;
import static ceri.common.test.ErrorProducer.RIX;
import static ceri.common.test.ErrorProducer.RTX;
import static ceri.common.test.TestUtil.threadRun;
import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.ExceptionObjIntFunction;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;
import ceri.common.test.CallSync;
import ceri.common.test.TestSocket;
import ceri.log.test.LogModifier;

public class SelfHealingSocketConnectorBehavior {
	private static final CallSync.Accept<HostPort> open = CallSync.consumer(null, false);
	private SelfHealingSocketConfig config;
	private TestSocket socket;
	private SelfHealingSocketConnector con;

	@Before
	public void before() {
		open.reset();
		socket = TestSocket.of();
		// Broken if IOException is thrown, not RuntimeException
		config = SelfHealingSocketConfig.builder("test", 123)
			.brokenPredicate(e -> e instanceof IOException).fixRetryDelayMs(1).recoveryDelayMs(1)
			.factory(factory(socket)).build();
		con = SelfHealingSocketConnector.of(config);
		assertConnect(con, "test", 123);
	}

	@After
	public void after() {
		con.close();
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(con.toString(), "\\btest:123\\b");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		con.out().write(ArrayUtil.bytes(1, 2, 3));
		assertRead(socket.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		socket.in.to.writeBytes(1, 2, 3);
		assertRead(con.in(), 1, 2, 3);
	}

	@Test
	public void shouldRetryFixOnError() throws InterruptedException {
		LogModifier.run(() -> {
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var enc = con.listeners().enclose(sync::signal)) {
				open.error.setFrom(IOX, IOX, null);
				con.broken();
				assertEquals(sync.await(), StateChange.broken);
				open.assertCall(HostPort.of("test", 123));
				open.assertCall(HostPort.of("test", 123));
				open.assertCall(HostPort.of("test", 123));
				assertEquals(sync.await(), StateChange.fixed);
			}
		}, Level.OFF, SelfHealingSocketConnector.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDetermineIfBrokenFromPredicate() {
		try (var con = SelfHealingSocketConnector.of(config)) {
			assertConnect(con, "test", 123);
			socket.in.read.error.setFrom(IOX, IOX, RTX);
			socket.in.to.writeBytes(1, 2, 3);
			assertThrown(() -> con.in().read()); // RuntimeException = not broken
			assertThrown(() -> con.in().read()); // IOException = broken
			assertThrown(() -> con.in().read()); // IOException = still broken
			open.assertCall(HostPort.of("test", 123));
		}
	}

	@Test
	public void shouldFixIfConnectFails() {
		open.error.setFrom(IOX);
		try (var exec = threadRun(con::connect)) {
			open.assertCall(HostPort.of("test", 123));
			assertThrown(exec::get);
		}
		open.assertCall(HostPort.of("test", 123)); // fixed
	}

	@Test
	public void shouldLogOnListenerError() {
		LogModifier.run(() -> {
			CallSync.Accept<StateChange> sync = CallSync.consumer(null, false);
			try (var enc = con.listeners().enclose(sync::accept)) {
				sync.error.setFrom(IOX);
				try (var exec = threadRun(con::broken)) {
					sync.assertCall(StateChange.broken); // logs error
					exec.get();
				}
				open.assertCall(HostPort.of("test", 123)); // fixed
				sync.assertCall(StateChange.fixed);
			}
		}, Level.OFF, SelfHealingSocketConnector.class);
	}

	@Test
	public void shouldCloseOnListenerInterrupt() {
		try (var con = SelfHealingSocketConnector.of(config)) {
			CallSync.Accept<StateChange> sync = CallSync.consumer(null, false);
			con.listeners().listen(sync::accept);
			sync.error.setFrom(RIX);
			try (var exec = threadRun(con::broken)) {
				sync.assertCall(StateChange.broken); // interrupt
				assertThrown(exec::get);
			}
		}
	}

	private static void assertConnect(SelfHealingSocketConnector con, String host, int port) {
		try (var exec = threadRun(con::connect)) {
			open.assertCall(HostPort.of(host, port));
			exec.get();
		}
	}

	private static ExceptionObjIntFunction<IOException, String, Socket> factory(Socket socket) {
		return (host, port) -> {
			open.accept(HostPort.of(host, port), IO_ADAPTER);
			return socket;
		};
	}

}
