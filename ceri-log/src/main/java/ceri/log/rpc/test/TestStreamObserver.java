package ceri.log.rpc.test;

import ceri.common.test.CallSync;
import io.grpc.stub.StreamObserver;

/**
 * Observer that signals conditions when methods are called.
 */
public class TestStreamObserver<T> implements StreamObserver<T> {
	public final CallSync.Consumer<T> next = CallSync.consumer(null, true);
	public final CallSync.Runnable completed = CallSync.runnable(true);
	public final CallSync.Consumer<Throwable> error = CallSync.consumer(null, true);

	public static <T> TestStreamObserver<T> of() {
		return new TestStreamObserver<>();
	}

	private TestStreamObserver() {}

	public void reset() {
		CallSync.resetAll(next, completed, error);
	}

	@Override
	public void onNext(T value) {
		next.accept(value);
	}

	@Override
	public void onCompleted() {
		completed.run();
	}

	@Override
	public void onError(Throwable t) {
		error.accept(t);
	}
}
