package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.typedProperties;
import org.junit.Test;

public class RpcChannelBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(RpcChannel.Config.NULL.enabled());
		assertFalse(new RpcChannel.Config("localhost", null).enabled());
		assertTrue(new RpcChannel.Config("host", 12345).enabled());
	}

	@Test
	public void shouldBuildFromProperties() {
		var config = new RpcChannelProperties(typedProperties("rpc-client"), "rpc-client").config();
		assertEquals(config.host(), "127.0.0.1");
		assertEquals(config.port(), 12345);
	}

	@Test
	public void shouldCreateFromConfig() {
		try (var channel = RpcChannel.of(RpcChannel.Config.localhost(12345))) {
			assertTrue(channel.toString().contains("localhost:12345"));
		}
	}

	@Test
	public void shouldNotInterruptOnClose() {
		TestManagedChannel mc = TestManagedChannel.of();
		mc.awaitTermination.error.setFrom(INX);
		try (RpcChannel _ = RpcChannel.of(mc)) {}
	}
}
