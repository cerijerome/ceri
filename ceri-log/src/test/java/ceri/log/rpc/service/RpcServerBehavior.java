package ceri.log.rpc.service;

import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.typedProperties;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.log.rpc.client.RpcChannel;

public class RpcServerBehavior {

	@Test
	public void shouldBuildFromProperties() {
		var config = new RpcServer.Properties(typedProperties("rpc-server"), "rpc-server").config();
		Assert.equal(config.port(), 12345);
		Assert.equal(config.shutdownTimeoutMs(), 1000);
	}

	@Test
	public void shouldDetermineIfEnabled() {
		Assert.no(RpcServer.Config.NULL.enabled());
		Assert.yes(RpcServer.Config.DEFAULT.enabled());
	}

	@Test
	public void shouldDetermineIfLoop() {
		Assert.no(RpcServer.Config.NULL.isLoop(RpcChannel.Config.localhost(12345)));
		Assert.no(RpcServer.Config.of(12345).isLoop(null));
		Assert.no(RpcServer.Config.of(12345).isLoop(new RpcChannel.Config("xxx", 12345)));
		Assert.yes(RpcServer.Config.of(12345).isLoop(RpcChannel.Config.localhost(12345)));
	}

	@Test
	public void shouldFailIfNoLoopRequired() {
		RpcServer.Config.of(12345).requireNoLoop(RpcChannel.Config.localhost(12346));
		Assert.thrown(
			() -> RpcServer.Config.of(12345).requireNoLoop(RpcChannel.Config.localhost(12345)));
	}

	@Test
	public void shouldStartServer() throws IOException {
		try (var service = TestRpcService.of();
			var server = RpcServer.start(service, RpcServer.Config.DEFAULT)) {
			Assert.yes(server.port() > 0);
			Assert.yes(server.toString().contains("(" + server.port() + ")"));
		}
	}

	@Test
	public void shouldNotThrowExceptionOnClose() {
		var server = TestServer.of();
		server.awaitTermination.error.setFrom(INX);
		try (RpcServer _ = new RpcServer(server, RpcServer.Config.DEFAULT)) {}
	}
}
