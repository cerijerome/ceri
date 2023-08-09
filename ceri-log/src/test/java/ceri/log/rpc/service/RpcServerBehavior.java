package ceri.log.rpc.service;

import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.INX;
import java.io.IOException;
import org.junit.Test;

public class RpcServerBehavior {

	@Test
	public void shouldStartServer() throws IOException {
		try (var service = TestRpcService.of()) {
			try (RpcServer server = RpcServer.start(service, RpcServerConfig.DEFAULT)) {
				assertTrue(server.port() > 0);
				assertTrue(server.toString().contains("(" + server.port() + ")"));
			}
		}
	}

	@Test
	public void shouldNotThrowExceptionOnClose() {
		TestServer server = TestServer.of();
		server.awaitTermination.error.setFrom(INX);
		try (RpcServer rpcServer = new RpcServer(server, RpcServerConfig.DEFAULT)) {}
	}

}
