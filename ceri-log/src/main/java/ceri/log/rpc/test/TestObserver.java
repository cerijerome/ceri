package ceri.log.rpc.test;

import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ValueCondition;
import io.grpc.stub.StreamObserver;

/**
 * Observer that signals conditions when methods are called.
 */
public class TestObserver<T> implements StreamObserver<T> {
	public final ValueCondition<T> next = ValueCondition.of();
	public final BooleanCondition completed = BooleanCondition.of();
	public final ValueCondition<Throwable> error = ValueCondition.of();

	public static <T> TestObserver<T> of() {
		return new TestObserver<>();
	}

	@Override
	public void onNext(T value) {
		next.signal(value);
	}

	@Override
	public void onCompleted() {
		completed.signal();
	}

	@Override
	public void onError(Throwable t) {
		error.signal(t);
	}

	public TestObserver<T> clear() {
		next.clear();
		completed.clear();
		error.clear();
		return this;
	}
}
