package ceri.log.rpc.service;

import static ceri.common.test.TestUtil.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import io.grpc.Server;

public class RpcServerBehavior {

	@Test
	public void shouldStartServer() throws IOException {
		try (var service = new TestRpcService()) {
			try (RpcServer server = RpcServer.start(service, RpcServerConfig.of())) {
				assertTrue(server.port() > 0);
				assertTrue(server.toString().contains("(" + server.port() + ")"));
			}
		}
	}

	@Test
	public void shouldNotThrowExceptionOnClose() throws InterruptedException {
		Server server = Mockito.mock(Server.class);
		when(server.awaitTermination(anyLong(), any())).thenThrow(InterruptedException.class);
		try (RpcServer rpcServer = new RpcServer(server, RpcServerConfig.of())) {}
	}

}
