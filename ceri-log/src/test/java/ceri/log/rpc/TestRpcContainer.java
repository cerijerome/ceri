package ceri.log.rpc;

import java.io.IOException;
import ceri.log.rpc.client.TestRpcClient;
import ceri.log.rpc.service.RpcServer;
import ceri.log.rpc.service.TestRpcService;

public class TestRpcContainer implements AutoCloseable {
	private final RpcServer server;
	public final TestRpcService service;
	public final TestRpcClient client0;
	public final TestRpcClient client1;

	public TestRpcContainer(int port, int shutdownTimeoutMs, int notifierResetDelayMs)
		throws IOException {
		try {
			service = TestRpcService.of();
			server = server(service, port, shutdownTimeoutMs);
			client0 = new TestRpcClient(port, notifierResetDelayMs);
			client1 = new TestRpcClient(port, notifierResetDelayMs);
		} catch (RuntimeException | IOException e) {
			close();
			throw e;
		}
	}

	public void reset() {
		service.reset();
	}

	@Override
	public void close() throws IOException {
		client1.close();
		client0.close();
		server.close();
		service.close();
	}

	private static RpcServer server(TestRpcService service, int port, int shutdownTimeoutMs)
		throws IOException {
		var config =
			RpcServer.Config.builder().port(port).shutdownTimeoutMs(shutdownTimeoutMs).build();
		return RpcServer.start(service, config);
	}

}
