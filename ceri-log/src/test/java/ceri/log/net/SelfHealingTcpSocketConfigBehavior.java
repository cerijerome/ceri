package ceri.log.net;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.baseProperties;
import java.io.IOException;
import org.junit.Test;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;

public class SelfHealingTcpSocketConfigBehavior {
	private static final HostPort hostPort = HostPort.of("test", 123);

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(SelfHealingTcpSocketConfig.NULL.enabled());
		assertTrue(SelfHealingTcpSocketConfig.of(hostPort).enabled());
	}

	@Test
	public void shouldCopySettings() {
		var config0 = SelfHealingTcpSocketConfig.builder(hostPort)
			.option(TcpSocketOption.soTimeout, 123).build();
		var config = SelfHealingTcpSocketConfig.builder(config0).build();
		assertEquals(config.hostPort, hostPort);
		assertEquals(config.options.get(TcpSocketOption.soTimeout), 123);
	}

	@Test
	public void shouldDetermineIfBroken() {
		var config = SelfHealingTcpSocketConfig.builder(hostPort).build();
		assertFalse(config.selfHealing.broken(null));
		assertFalse(config.selfHealing.broken(new RuntimeException()));
		assertFalse(config.selfHealing.broken(new IOException()));
		config = SelfHealingTcpSocketConfig.builder(hostPort)
			.selfHealing(b -> b.brokenPredicate(IOException.class::isInstance)).build();
		assertFalse(config.selfHealing.broken(null));
		assertFalse(config.selfHealing.broken(new RuntimeException()));
		assertTrue(config.selfHealing.broken(new IOException()));
	}

	@Test
	public void shouldCreateFromProperties() {
		SelfHealingTcpSocketConfig config =
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
		var config = SelfHealingTcpSocketConfig.builder(hostPort)
			.option(TcpSocketOption.soTimeout, 123).build();
		assertFind(config, "test:123.*SO_TIMEOUT=123");
	}

}
