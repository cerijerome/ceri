package ceri.log.rpc.service;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.INX;
import static ceri.common.test.TestUtil.typedProperties;
import java.io.IOException;
import org.junit.Test;
import ceri.log.rpc.client.RpcChannel;

public class RpcServerBehavior {

	@Test
	public void shouldBuildFromProperties() {
		var config = new RpcServerProperties(typedProperties("rpc-server"), "rpc-server").config();
		assertEquals(config.port(), 12345);
		assertEquals(config.shutdownTimeoutMs(), 1000);
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertFalse(RpcServer.Config.NULL.enabled());
		assertTrue(RpcServer.Config.DEFAULT.enabled());
	}

	@Test
	public void shouldDetermineIfLoop() {
		assertFalse(RpcServer.Config.NULL.isLoop(RpcChannel.Config.localhost(12345)));
		assertFalse(RpcServer.Config.of(12345).isLoop(null));
		assertFalse(RpcServer.Config.of(12345).isLoop(new RpcChannel.Config("xxx", 12345)));
		assertTrue(RpcServer.Config.of(12345).isLoop(RpcChannel.Config.localhost(12345)));
	}

	@Test
	public void shouldFailIfNoLoopRequired() {
		RpcServer.Config.of(12345).requireNoLoop(RpcChannel.Config.localhost(12346));
		assertThrown(
			() -> RpcServer.Config.of(12345).requireNoLoop(RpcChannel.Config.localhost(12345)));
	}

	@Test
	public void shouldStartServer() throws IOException {
		try (var service = TestRpcService.of()) {
			try (RpcServer server = RpcServer.start(service, RpcServer.Config.DEFAULT)) {
				assertTrue(server.port() > 0);
				assertTrue(server.toString().contains("(" + server.port() + ")"));
			}
		}
	}

	@Test
	public void shouldNotThrowExceptionOnClose() {
		var server = TestServer.of();
		server.awaitTermination.error.setFrom(INX);
		try (RpcServer _ = new RpcServer(server, RpcServer.Config.DEFAULT)) {}
	}

}
