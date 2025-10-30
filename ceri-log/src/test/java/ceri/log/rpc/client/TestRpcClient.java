package ceri.log.rpc.client;

import java.io.IOException;
import com.google.protobuf.UInt32Value;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.log.rpc.TestGrpc;
import ceri.log.rpc.util.Rpc;

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

	public Enclosure<?> listen(Functions.Consumer<Integer> consumer) {
		return notifier.enclose(consumer);
	}

	public void run() throws IOException {
		RpcClients.wrap(() -> stub.run(Rpc.EMPTY));
	}

	public void set(int value) throws IOException {
		RpcClients.wrap(() -> stub.set(Rpc.uint32(value)));
	}

	public int get() throws IOException {
		return RpcClients.wrapReturn(() -> stub.get(Rpc.EMPTY)).getValue();
	}

	@Override
	public void close() {
		notifier.close();
		channel.close();
	}

	private RpcClientNotifier<Integer, UInt32Value> createNotifier(RpcChannel channel,
		int notifierResetDelayMs) {
		var stub = TestGrpc.newStub(channel.channel);
		var config = new RpcClientNotifier.Config(notifierResetDelayMs);
		return RpcClientNotifier.of(stub::notify, UInt32Value::getValue, config);
	}
}
