package ceri.log.rpc.client;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RpcClientsTest {

	@Test
	public void testExecute() {
		RpcClients.execute(() -> {});
		RpcClients
			.execute(() -> Assert.throwIt(new IllegalStateException("already half-closed")));
		RpcClients.execute(() -> Assert.throwIt(cancelException("channel shutdown")));
		Assert.thrown(
			() -> RpcClients.execute(() -> Assert.throwIt(cancelException("channel error"))));
	}

	@Test
	public void testWrap() throws IOException {
		RpcClients.wrap(() -> {});
		Assert.io(() -> RpcClients.wrap(() -> Assert.throwIt(new IOException("test"))));
		Assert.io(() -> RpcClients.wrap(() -> Assert.throwIt(cancelException("test"))));
	}

	@Test
	public void testWrapReturn() throws IOException {
		Assert.equal(RpcClients.wrapReturn(() -> "test"), "test");
		Assert.io(() -> RpcClients.wrapReturn(() -> Assert.throwIt(new IOException("test"))));
		Assert.io(() -> RpcClients.wrapReturn(() -> Assert.throwIt(cancelException("test"))));
	}

	private static StatusRuntimeException cancelException(String message) {
		return Status.CANCELLED.withDescription(message).asRuntimeException();
	}
}
