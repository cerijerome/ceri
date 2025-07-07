package ceri.log.rpc.service;

import static ceri.log.rpc.service.RpcServiceUtil.respond;
import static ceri.log.rpc.util.RpcUtil.EMPTY;
import com.google.protobuf.Empty;
import com.google.protobuf.UInt32Value;
import ceri.common.event.Listeners;
import ceri.common.function.Excepts.IntConsumer;
import ceri.common.function.Excepts.IntSupplier;
import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.RuntimeCloseable;
import ceri.log.rpc.TestGrpc;
import io.grpc.stub.StreamObserver;

public class TestRpcService extends TestGrpc.TestImplBase implements RuntimeCloseable {
	private final Listeners<Integer> listeners = Listeners.of();
	private final RpcServiceNotifier<Integer, UInt32Value> notifier;
	public Runnable<?> run = null;
	public IntConsumer<?> set = null;
	public IntSupplier<?> get = null;

	public static TestRpcService of() {
		return new TestRpcService();
	}

	private TestRpcService() {
		notifier = RpcServiceNotifier.of(listeners, UInt32Value::of);
		reset();
	}

	public void reset() {
		run = () -> {};
		set = _ -> {};
		get = () -> 0;
	}

	public void waitForClients(int count) throws InterruptedException {
		notifier.waitForListener(i -> i == count);
	}

	public void notify(int value) {
		listeners.accept(value);
	}

	@Override
	public StreamObserver<Empty> notify(StreamObserver<UInt32Value> observer) {
		return notifier.listen(observer);
	}

	@Override
	public void run(Empty request, StreamObserver<Empty> observer) {
		respond(observer, EMPTY, run);
	}

	@Override
	public void set(UInt32Value request, StreamObserver<Empty> observer) {
		respond(observer, EMPTY, () -> set.accept(request.getValue()));
	}

	@Override
	public void get(Empty request, StreamObserver<UInt32Value> observer) {
		respond(observer, () -> UInt32Value.of(get.getAsInt()));
	}

	@Override
	public void close() {
		notifier.close();
	}
}
