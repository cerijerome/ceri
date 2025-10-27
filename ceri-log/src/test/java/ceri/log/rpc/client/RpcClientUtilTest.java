package ceri.log.rpc.client;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RpcClientUtilTest {

	@Test
	public void testExecute() {
		RpcClientUtil.execute(() -> {});
		RpcClientUtil
			.execute(() -> Assert.throwIt(new IllegalStateException("already half-closed")));
		RpcClientUtil.execute(() -> Assert.throwIt(cancelException("channel shutdown")));
		Assert.thrown(
			() -> RpcClientUtil.execute(() -> Assert.throwIt(cancelException("channel error"))));
	}

	@Test
	public void testWrap() throws IOException {
		RpcClientUtil.wrap(() -> {});
		Assert.io(() -> RpcClientUtil.wrap(() -> Assert.throwIt(new IOException("test"))));
		Assert.io(() -> RpcClientUtil.wrap(() -> Assert.throwIt(cancelException("test"))));
	}

	@Test
	public void testWrapReturn() throws IOException {
		Assert.equal(RpcClientUtil.wrapReturn(() -> "test"), "test");
		Assert.io(() -> RpcClientUtil.wrapReturn(() -> Assert.throwIt(new IOException("test"))));
		Assert.io(() -> RpcClientUtil.wrapReturn(() -> Assert.throwIt(cancelException("test"))));
	}

	private static StatusRuntimeException cancelException(String message) {
		return Status.CANCELLED.withDescription(message).asRuntimeException();
	}
}
