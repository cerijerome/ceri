package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwIt;
import java.io.IOException;
import org.junit.Test;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RpcClientUtilTest {

	@Test
	public void testExecute() {
		RpcClientUtil.execute(() -> {});
		RpcClientUtil.execute(() -> throwIt(new IllegalStateException("already half-closed")));
		RpcClientUtil.execute(() -> throwIt(cancelException("channel shutdown")));
		assertThrown(() -> RpcClientUtil.execute(() -> throwIt(cancelException("channel error"))));
	}

	@Test
	public void testWrap() throws IOException {
		RpcClientUtil.wrap(() -> {});
		assertIoe(() -> RpcClientUtil.wrap(() -> throwIt(new IOException("test"))));
		assertIoe(() -> RpcClientUtil.wrap(() -> throwIt(cancelException("test"))));
	}

	@Test
	public void testWrapReturn() throws IOException {
		assertEquals(RpcClientUtil.wrapReturn(() -> "test"), "test");
		assertIoe(() -> RpcClientUtil.wrapReturn(() -> throwIt(new IOException("test"))));
		assertIoe(() -> RpcClientUtil.wrapReturn(() -> throwIt(cancelException("test"))));
	}

	private static StatusRuntimeException cancelException(String message) {
		return Status.CANCELLED.withDescription(message).asRuntimeException();
	}

}
