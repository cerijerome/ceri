package ceri.log.rpc.client;

import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RpcClientUtilTest {

	@Test
	public void testExecute() {
		RpcClientUtil.execute(() -> {});
		RpcClientUtil.execute(() -> {
			throw new IllegalStateException("already half-closed");
		});
		RpcClientUtil.execute(() -> {
			throw cancelException("channel shutdown");
		});
		assertThrown(() -> RpcClientUtil.execute(() -> {
			throw cancelException("channel error");
		}));
	}

	@Test
	public void testWrap() throws IOException {
		RpcClientUtil.wrap(() -> {});
		assertThrown(IOException.class, () -> RpcClientUtil.wrap(() -> {
			throw new IOException("test");
		}));
		assertThrown(IOException.class, () -> RpcClientUtil.wrap(() -> {
			throw cancelException("test");
		}));
	}

	@Test
	public void testWrapReturn() throws IOException {
		assertThat(RpcClientUtil.wrapReturn(() -> "test"), is("test"));
		assertThrown(IOException.class, () -> RpcClientUtil.wrapReturn(() -> {
			throw new IOException("test");
		}));
		assertThrown(IOException.class, () -> RpcClientUtil.wrapReturn(() -> {
			throw cancelException("test");
		}));
	}

	private static StatusRuntimeException cancelException(String message) {
		return Status.CANCELLED.withDescription(message).asRuntimeException();
	}

}
