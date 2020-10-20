package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class RpcChannelConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RpcChannelConfig t = RpcChannelConfig.of("127.0.1.1", 12345);
		RpcChannelConfig eq0 = RpcChannelConfig.of("127.0.1.1", 12345);
		RpcChannelConfig eq1 = RpcChannelConfig.builder().host("127.0.1.1").port(12345).build();
		RpcChannelConfig ne0 = RpcChannelConfig.of("127.0.0.1", 12345);
		RpcChannelConfig ne1 = RpcChannelConfig.of("127.0.1.1", 12344);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(RpcChannelConfig.NULL.enabled());
		assertFalse(RpcChannelConfig.builder().host("localhost").build().enabled());
		assertTrue(RpcChannelConfig.of("host", 12345).enabled());
	}

	@Test
	public void shouldBuildFromProperties() {
		RpcChannelConfig config =
			new RpcChannelProperties(baseProperties("rpc-client"), "rpc-client").config();
		assertEquals(config.host, "127.0.0.1");
		assertEquals(config.port, 12345);
	}

}
