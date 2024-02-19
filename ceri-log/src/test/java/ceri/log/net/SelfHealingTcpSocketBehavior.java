package ceri.log.net;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.baseProperties;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
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
import ceri.common.util.CloseableUtil;
import ceri.log.io.SelfHealing;
import ceri.log.test.LogModifier;

public class SelfHealingTcpSocketBehavior {
	private static final HostPort hostPort = HostPort.of("test", 123);
	private static final int localPort = 321;
	private SelfHealingTcpSocket.Config config = null;
	private TestTcpSocket socket = null;
	private SelfHealingTcpSocket shs = null;

	@After
	public void after() {
		CloseableUtil.close(shs);
		shs = null;
		socket = null;
		config = null;
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(SelfHealingTcpSocket.Config.NULL.enabled());
		assertTrue(SelfHealingTcpSocket.Config.of(hostPort).enabled());
	}

	@Test
	public void shouldCopySettings() {
		var config0 = SelfHealingTcpSocket.Config.builder(hostPort)
			.option(TcpSocketOption.soTimeout, 123).build();
		var config = SelfHealingTcpSocket.Config.builder(config0).build();
		assertEquals(config.hostPort, hostPort);
		assertEquals(config.options.get(TcpSocketOption.soTimeout), 123);
	}

	@Test
	public void shouldDetermineIfBroken() {
		var config = SelfHealingTcpSocket.Config.builder(hostPort).build();
		assertFalse(config.selfHealing.broken(null));
		assertFalse(config.selfHealing.broken(new RuntimeException()));
		assertFalse(config.selfHealing.broken(new IOException()));
		config = SelfHealingTcpSocket.Config.builder(hostPort)
			.selfHealing(b -> b.brokenPredicate(IOException.class::isInstance)).build();
		assertFalse(config.selfHealing.broken(null));
		assertFalse(config.selfHealing.broken(new RuntimeException()));
		assertTrue(config.selfHealing.broken(new IOException()));
	}

	@Test
	public void shouldCreateFromProperties() {
		SelfHealingTcpSocket.Config config =
			new SelfHealingTcpSocketProperties(baseProperties("self-healing-tcp-socket"), "socket")
				.config();
		assertEquals(config.hostPort, HostPort.of("test", 123));
		assertEquals(config.options.get(TcpSocketOption.soTimeout), 111);
		assertEquals(config.options.get(TcpSocketOption.soLinger), 222);
		assertEquals(config.options.get(TcpSocketOption.soKeepAlive), true);
		assertEquals(config.options.get(TcpSocketOption.soReuseAddr), false);
		assertEquals(config.options.get(TcpSocketOption.soOobInline), true);
		assertEquals(config.options.get(TcpSocketOption.tcpNoDelay), false);
		assertEquals(config.options.get(TcpSocketOption.ipTos), 333);
		assertEquals(config.options.get(TcpSocketOption.soSndBuf), 444);
		assertEquals(config.options.get(TcpSocketOption.soRcvBuf), 555);
		assertEquals(config.selfHealing.fixRetryDelayMs, 666);
		assertEquals(config.selfHealing.recoveryDelayMs, 777);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		config = SelfHealingTcpSocket.Config.builder(hostPort)
			.option(TcpSocketOption.soTimeout, 123).build();
		assertFind(config, "test:123.*SO_TIMEOUT=123");
		init();
		assertFind(shs.toString(), "\\btest:123\\b");
	}

	@Test
	public void shouldProvideHostAndPort() {
		init();
		assertEquals(shs.hostPort(), HostPort.of("test", 123));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		init();
		shs.open();
		shs.out().write(ArrayUtil.bytes(1, 2, 3));
		assertRead(socket.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		init();
		shs.open();
		socket.in.to.writeBytes(1, 2, 3);
		assertRead(shs.in(), 1, 2, 3);
	}

	@Test
	public void shouldNotifyListenersOnFix() throws InterruptedException {
		init();
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = shs.listeners().enclose(sync::signal)) {
			shs.broken();
			sync.await(StateChange.fixed);
		}
	}

	@Test
	public void shouldLogOnNotifyError() {
		init();
		LogModifier.run(() -> {
			CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
			sync.error.setFrom(RTX);
			try (var enc = shs.listeners().enclose(sync::accept)) {
				shs.broken(); // error logged
				sync.awaitAuto();
			}
		}, Level.OFF, SelfHealing.class);
	}

	@Test
	public void shouldStopOnNotifyInterrupt() {
		init();
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
		sync.error.setFrom(RIX);
		try (var enc = shs.listeners().enclose(sync::accept)) {
			assertThrown(RuntimeInterruptedException.class, shs::broken);
			sync.awaitAuto();
		}
	}

	@Test
	public void shouldFailToOpenIfOptionFails() throws IOException {
		init();
		shs.options(TcpSocketOptions.of().set(TcpSocketOption.soKeepAlive, true));
		socket.optionSync.error.setFrom(IOX, null);
		assertThrown(shs::open);
		socket.open.await();
	}

	@Test
	public void shouldProvideOptionIfOpen() throws IOException {
		init();
		assertNull(shs.option(TcpSocketOption.soLinger));
		shs.option(TcpSocketOption.soLinger, 123);
		assertNull(shs.option(TcpSocketOption.soLinger));
		shs.open();
		assertEquals(shs.option(TcpSocketOption.soLinger), 123);
		shs.option(TcpSocketOption.soLinger, 456);
		assertEquals(shs.option(TcpSocketOption.soLinger), 456);
	}

	private void init() {
		socket = TestTcpSocket.of(hostPort, localPort);
		// Broken if IOException is thrown, not RuntimeException
		config =
			SelfHealingTcpSocket.Config.builder(hostPort)
				.selfHealing(b -> b.brokenPredicate(IOException.class::isInstance)
					.fixRetryDelayMs(1).recoveryDelayMs(1))
				.factory(hostPort -> open(socket)).build();
		shs = SelfHealingTcpSocket.of(config);
	}

	private static TestTcpSocket open(TestTcpSocket socket) throws IOException {
		socket.open();
		return socket;
	}
}
