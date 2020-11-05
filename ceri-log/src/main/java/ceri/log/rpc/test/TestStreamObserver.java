package ceri.log.rpc.test;

import ceri.common.test.CallSync;
import io.grpc.stub.StreamObserver;

/**
 * Observer that signals conditions when methods are called.
 */
public class TestStreamObserver<T> implements StreamObserver<T> {
	public final CallSync.Accept<T> next = CallSync.consumer(null, true);
	public final CallSync.Run completed = CallSync.runnable(true);
	public final CallSync.Accept<Throwable> error = CallSync.consumer(null, true);

	public static <T> TestStreamObserver<T> of() {
		return new TestStreamObserver<>();
	}

	private TestStreamObserver() {}

	public void reset() {
		next.reset();
		completed.reset();
		error.reset();
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
