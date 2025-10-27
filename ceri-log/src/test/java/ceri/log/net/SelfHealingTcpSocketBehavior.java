package ceri.log.net;

import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.typedProperties;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.Closeables;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.TestTcpSocket;
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
		Closeables.close(shs);
		shs = null;
		socket = null;
		config = null;
	}

	@Test
	public void shouldDetermineIfEnabled() {
		Assert.no(SelfHealingTcpSocket.Config.NULL.enabled());
		Assert.yes(SelfHealingTcpSocket.Config.of(hostPort).enabled());
	}

	@Test
	public void shouldCopySettings() {
		var config0 = SelfHealingTcpSocket.Config.builder(hostPort)
			.option(TcpSocketOption.soTimeout, 123).build();
		var config = SelfHealingTcpSocket.Config.builder(config0).build();
		Assert.equal(config.hostPort, hostPort);
		Assert.equal(config.options.get(TcpSocketOption.soTimeout), 123);
	}

	@Test
	public void shouldDetermineIfBroken() {
		var config = SelfHealingTcpSocket.Config.builder(hostPort).build();
		Assert.no(config.selfHealing.broken(null));
		Assert.no(config.selfHealing.broken(new RuntimeException()));
		Assert.no(config.selfHealing.broken(new IOException()));
		config = SelfHealingTcpSocket.Config.builder(hostPort)
			.selfHealing(b -> b.brokenPredicate(IOException.class::isInstance)).build();
		Assert.no(config.selfHealing.broken(null));
		Assert.no(config.selfHealing.broken(new RuntimeException()));
		Assert.yes(config.selfHealing.broken(new IOException()));
	}

	@Test
	public void shouldCreateFromProperties() {
		SelfHealingTcpSocket.Config config =
			new SelfHealingTcpSocket.Properties(typedProperties("self-healing-tcp-socket"),
				"socket").config();
		Assert.equal(config.hostPort, HostPort.of("test", 123));
		Assert.equal(config.options.get(TcpSocketOption.soTimeout), 111);
		Assert.equal(config.options.get(TcpSocketOption.soLinger), 222);
		Assert.equal(config.options.get(TcpSocketOption.soKeepAlive), true);
		Assert.equal(config.options.get(TcpSocketOption.soReuseAddr), false);
		Assert.equal(config.options.get(TcpSocketOption.soOobInline), true);
		Assert.equal(config.options.get(TcpSocketOption.tcpNoDelay), false);
		Assert.equal(config.options.get(TcpSocketOption.ipTos), 333);
		Assert.equal(config.options.get(TcpSocketOption.soSndBuf), 444);
		Assert.equal(config.options.get(TcpSocketOption.soRcvBuf), 555);
		Assert.equal(config.selfHealing.fixRetryDelayMs, 666);
		Assert.equal(config.selfHealing.recoveryDelayMs, 777);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		config = SelfHealingTcpSocket.Config.builder(hostPort)
			.option(TcpSocketOption.soTimeout, 123).build();
		Assert.find(config, "test:123.*SO_TIMEOUT=123");
		init();
		Assert.find(shs.toString(), "\\btest:123\\b");
	}

	@Test
	public void shouldProvideHostAndPort() {
		init();
		Assert.equal(shs.hostPort(), HostPort.of("test", 123));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		init();
		shs.open();
		shs.out().write(ArrayUtil.bytes.of(1, 2, 3));
		Assert.read(socket.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		init();
		shs.open();
		socket.in.to.writeBytes(1, 2, 3);
		Assert.read(shs.in(), 1, 2, 3);
	}

	@Test
	public void shouldNotifyListenersOnFix() throws InterruptedException {
		init();
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = shs.listeners().enclose(sync::signal)) {
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
			try (var _ = shs.listeners().enclose(sync::accept)) {
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
		try (var _ = shs.listeners().enclose(sync::accept)) {
			Assert.thrown(RuntimeInterruptedException.class, shs::broken);
			sync.awaitAuto();
		}
	}

	@Test
	public void shouldFailToOpenIfOptionFails() throws IOException {
		init();
		shs.options(TcpSocketOptions.of().set(TcpSocketOption.soKeepAlive, true));
		socket.optionSync.error.setFrom(IOX, null);
		Assert.thrown(shs::open);
		socket.open.await();
	}

	@Test
	public void shouldProvideOptionIfOpen() throws IOException {
		init();
		Assert.isNull(shs.option(TcpSocketOption.soLinger));
		shs.option(TcpSocketOption.soLinger, 123);
		Assert.isNull(shs.option(TcpSocketOption.soLinger));
		shs.open();
		Assert.equal(shs.option(TcpSocketOption.soLinger), 123);
		shs.option(TcpSocketOption.soLinger, 456);
		Assert.equal(shs.option(TcpSocketOption.soLinger), 456);
	}

	private void init() {
		socket = TestTcpSocket.of(hostPort, localPort);
		// Broken if IOException is thrown, not RuntimeException
		config = SelfHealingTcpSocket.Config.builder(hostPort).selfHealing(b -> b
			.brokenPredicate(IOException.class::isInstance).fixRetryDelayMs(1).recoveryDelayMs(1))
			.factory(_ -> open(socket)).build();
		shs = SelfHealingTcpSocket.of(config);
	}

	private static TestTcpSocket open(TestTcpSocket socket) throws IOException {
		socket.open();
		return socket;
	}
}
