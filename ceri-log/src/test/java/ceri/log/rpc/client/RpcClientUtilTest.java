package ceri.log.rpc.client;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.throwIt;
import static org.hamcrest.CoreMatchers.is;
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
		assertThrown(IOException.class,
			() -> RpcClientUtil.wrap(() -> throwIt(new IOException("test"))));
		assertThrown(IOException.class,
			() -> RpcClientUtil.wrap(() -> throwIt(cancelException("test"))));
	}

	@Test
	public void testWrapReturn() throws IOException {
		assertThat(RpcClientUtil.wrapReturn(() -> "test"), is("test"));
		assertThrown(IOException.class,
			() -> RpcClientUtil.wrapReturn(() -> throwIt(new IOException("test"))));
		assertThrown(IOException.class,
			() -> RpcClientUtil.wrapReturn(() -> throwIt(cancelException("test"))));
	}

	private static StatusRuntimeException cancelException(String message) {
		return Status.CANCELLED.withDescription(message).asRuntimeException();
	}

}
