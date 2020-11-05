package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.INX;
import org.junit.Test;

public class RpcChannelBehavior {

	@Test
	public void shouldCreateFromConfig() {
		try (RpcChannel channel = RpcChannel.of(RpcChannelConfig.localhost(12345))) {
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
