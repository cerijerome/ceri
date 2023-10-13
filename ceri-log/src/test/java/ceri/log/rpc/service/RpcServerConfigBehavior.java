package ceri.log.rpc.service;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.log.rpc.client.RpcChannelConfig;

public class RpcServerConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RpcServerConfig t = RpcServerConfig.of(12345);
		RpcServerConfig eq0 = RpcServerConfig.builder().port(12345).build();
		RpcServerConfig ne0 = RpcServerConfig.of(12344);
		RpcServerConfig ne1 = RpcServerConfig.builder().port(12345).shutdownTimeoutMs(0).build();
		RpcServerConfig ne2 = RpcServerConfig.DEFAULT;
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldBuildFromProperties() {
		RpcServerConfig config =
			new RpcServerProperties(baseProperties("rpc-server"), "rpc-server").config();
		assertEquals(config.port, 12345);
		assertEquals(config.shutdownTimeoutMs, 1000);
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(RpcServerConfig.NULL.enabled());
		assertTrue(RpcServerConfig.DEFAULT.enabled());
	}

	@Test
	public void shouldDetermineIfLoop() {
		assertFalse(RpcServerConfig.NULL.isLoop(RpcChannelConfig.localhost(12345)));
		assertFalse(RpcServerConfig.of(12345).isLoop(null));
		assertFalse(RpcServerConfig.of(12345).isLoop(RpcChannelConfig.of("xxx", 12345)));
		assertTrue(RpcServerConfig.of(12345).isLoop(RpcChannelConfig.localhost(12345)));
	}

	@Test
	public void shouldFailIfNoLoopRequired() {
		RpcServerConfig.of(12345).requireNoLoop(RpcChannelConfig.localhost(12346));
		assertThrown(
			() -> RpcServerConfig.of(12345).requireNoLoop(RpcChannelConfig.localhost(12345)));
	}

}
