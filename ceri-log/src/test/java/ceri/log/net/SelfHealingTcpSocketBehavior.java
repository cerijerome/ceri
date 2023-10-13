package ceri.log.net;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNull;
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
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.test.CallSync;
import ceri.common.test.TestTcpSocket;
import ceri.log.io.SelfHealingDevice;
import ceri.log.test.LogModifier;

public class SelfHealingTcpSocketBehavior {
	private static final HostPort hostPort = HostPort.of("test", 123);
	private static final int localPort = 321;
	private SelfHealingTcpSocketConfig config;
	private TestTcpSocket socket;
	private SelfHealingTcpSocket shs;

	@Before
	public void before() {
		socket = TestTcpSocket.of(hostPort, localPort);
		// Broken if IOException is thrown, not RuntimeException
		config =
			SelfHealingTcpSocketConfig.builder(hostPort)
				.selfHealing(b -> b.brokenPredicate(IOException.class::isInstance)
					.fixRetryDelayMs(1).recoveryDelayMs(1))
				.factory(hostPort -> open(socket)).build();
		shs = SelfHealingTcpSocket.of(config);
	}

	@After
	public void after() {
		shs.close();
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(shs.toString(), "\\btest:123\\b");
	}

	@Test
	public void shouldProvideHostAndPort() {
		assertEquals(shs.hostPort(), HostPort.of("test", 123));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		shs.open();
		shs.out().write(ArrayUtil.bytes(1, 2, 3));
		assertRead(socket.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		shs.open();
		socket.in.to.writeBytes(1, 2, 3);
		assertRead(shs.in(), 1, 2, 3);
	}

	@Test
	public void shouldNotifyListenersOnFix() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = shs.listeners().enclose(sync::signal)) {
			shs.broken();
			sync.await(StateChange.fixed);
		}
	}

	@Test
	public void shouldLogOnNotifyError() {
		LogModifier.run(() -> {
			CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
			sync.error.setFrom(RTX);
			try (var enc = shs.listeners().enclose(sync::accept)) {
				shs.broken(); // error logged
				sync.awaitAuto();
			}
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldStopOnNotifyInterrupt() {
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
		sync.error.setFrom(RIX);
		try (var enc = shs.listeners().enclose(sync::accept)) {
			assertThrown(RuntimeInterruptedException.class, shs::broken);
			sync.awaitAuto();
		}
	}

	@Test
	public void shouldFailToOpenIfOptionFails() throws IOException {
		shs.options(TcpSocketOptions.of().set(TcpSocketOption.soKeepAlive, true));
		socket.optionSync.error.setFrom(IOX, null);
		assertThrown(shs::open);
		socket.open.await();
	}

	@Test
	public void shouldProvideOptionIfOpen() throws IOException {
		assertNull(shs.option(TcpSocketOption.soLinger));
		shs.option(TcpSocketOption.soLinger, 123);
		assertNull(shs.option(TcpSocketOption.soLinger));
		shs.open();
		assertEquals(shs.option(TcpSocketOption.soLinger), 123);
		shs.option(TcpSocketOption.soLinger, 456);
		assertEquals(shs.option(TcpSocketOption.soLinger), 456);
	}

	private static TestTcpSocket open(TestTcpSocket socket) throws IOException {
		socket.open();
		return socket;
	}

}
