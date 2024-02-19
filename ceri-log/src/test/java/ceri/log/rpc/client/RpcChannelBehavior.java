package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class RpcChannelBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = RpcChannel.Config.of("127.0.1.1", 12345);
		var eq0 = RpcChannel.Config.of("127.0.1.1", 12345);
		var eq1 = RpcChannel.Config.builder().host("127.0.1.1").port(12345).build();
		var ne0 = RpcChannel.Config.of("127.0.0.1", 12345);
		var ne1 = RpcChannel.Config.of("127.0.1.1", 12344);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(RpcChannel.Config.NULL.enabled());
		assertFalse(RpcChannel.Config.builder().host("localhost").build().enabled());
		assertTrue(RpcChannel.Config.of("host", 12345).enabled());
	}

	@Test
	public void shouldBuildFromProperties() {
		var config = new RpcChannelProperties(baseProperties("rpc-client"), "rpc-client").config();
		assertEquals(config.host, "127.0.0.1");
		assertEquals(config.port, 12345);
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
		try (RpcChannel channel = RpcChannel.of(mc)) {}
	}
}
