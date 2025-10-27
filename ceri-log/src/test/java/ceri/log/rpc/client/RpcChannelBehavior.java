package ceri.log.rpc.client;

import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.typedProperties;
import org.junit.Test;
import ceri.common.test.Assert;

public class RpcChannelBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		Assert.no(RpcChannel.Config.NULL.enabled());
		Assert.no(new RpcChannel.Config("localhost", null).enabled());
		Assert.yes(new RpcChannel.Config("host", 12345).enabled());
	}

	@Test
	public void shouldBuildFromProperties() {
		var config =
			new RpcChannel.Properties(typedProperties("rpc-client"), "rpc-client").config();
		Assert.equal(config.host(), "127.0.0.1");
		Assert.equal(config.port(), 12345);
	}

	@Test
	public void shouldCreateFromConfig() {
		try (var channel = RpcChannel.of(RpcChannel.Config.localhost(12345))) {
			Assert.yes(channel.toString().contains("localhost:12345"));
		}
	}

	@Test
	public void shouldNotInterruptOnClose() {
		TestManagedChannel mc = TestManagedChannel.of();
		mc.awaitTermination.error.setFrom(INX);
		try (RpcChannel _ = RpcChannel.of(mc)) {}
	}
}
