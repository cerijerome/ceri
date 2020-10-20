package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Test;
import io.grpc.ManagedChannel;

public class RpcChannelBehavior {

	@Test
	public void shouldCreateFromConfig() {
		try (RpcChannel channel = RpcChannel.of(RpcChannelConfig.localhost(12345))) {
			assertTrue(channel.toString().contains("localhost:12345"));
		}
	}

	@Test
	public void shouldNotInterruptOnClose() throws InterruptedException {
		ManagedChannel mc = mock(ManagedChannel.class);
		when(mc.awaitTermination(anyLong(), any())).thenThrow(new InterruptedException("test"));
		try (RpcChannel channel = RpcChannel.of(mc)) {}
	}

}
