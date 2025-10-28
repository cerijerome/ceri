package ceri.log.rpc.client;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.Testing;

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
			new RpcChannel.Properties(Testing.properties("rpc-client"), "rpc-client").config();
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
		var mc = TestManagedChannel.of();
		mc.awaitTermination.error.setFrom(ErrorGen.INX);
		try (RpcChannel _ = RpcChannel.of(mc)) {}
	}
}
