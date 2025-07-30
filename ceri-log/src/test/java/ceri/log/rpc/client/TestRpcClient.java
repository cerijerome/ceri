package ceri.log.rpc.client;

import static ceri.log.rpc.client.RpcClientUtil.wrap;
import static ceri.log.rpc.client.RpcClientUtil.wrapReturn;
import static ceri.log.rpc.util.RpcUtil.EMPTY;
import static ceri.log.rpc.util.RpcUtil.uint32;
import java.io.IOException;
import java.util.function.Consumer;
import com.google.protobuf.UInt32Value;
import ceri.common.function.Functions;
import ceri.common.util.Enclosure;
import ceri.log.rpc.TestGrpc;
import ceri.log.rpc.TestGrpc.TestStub;

/**
 * Client for the Test service.
 */
public class TestRpcClient implements Functions.Closeable {
	private final RpcChannel channel;
	private final TestGrpc.TestBlockingStub stub;
	private final RpcClientNotifier<Integer, UInt32Value> notifier;

	public TestRpcClient(int port, int notifierResetDelayMs) {
		channel = RpcChannel.localhost(port);
		stub = TestGrpc.newBlockingStub(channel.channel);
		notifier = createNotifier(channel, notifierResetDelayMs);
	}

	public Enclosure<?> listen(Consumer<Integer> consumer) {
		return notifier.enclose(consumer);
	}

	public void run() throws IOException {
		wrap(() -> stub.run(EMPTY));
	}

	public void set(int value) throws IOException {
		wrap(() -> stub.set(uint32(value)));
	}

	public int get() throws IOException {
		return wrapReturn(() -> stub.get(EMPTY)).getValue();
	}

	@Override
	public void close() {
		notifier.close();
		channel.close();
	}

	private RpcClientNotifier<Integer, UInt32Value> createNotifier(RpcChannel channel,
		int notifierResetDelayMs) {
		TestStub stub = TestGrpc.newStub(channel.channel);
		var config = new RpcClientNotifier.Config(notifierResetDelayMs);
		return RpcClientNotifier.of(stub::notify, UInt32Value::getValue, config);
	}

}
