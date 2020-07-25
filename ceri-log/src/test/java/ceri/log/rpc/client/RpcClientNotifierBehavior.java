package ceri.log.rpc.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.TestListener;
import ceri.log.rpc.TestObserver;
import ceri.log.test.LogModifier;
import io.grpc.stub.StreamObserver;

public class RpcClientNotifierBehavior {
	private TestCall call;
	private RpcClientNotifier<Integer, String> notifier;

	@Before
	public void before() {
		call = new TestCall();
		RpcClientNotifierConfig config = RpcClientNotifierConfig.of(1);
		notifier = RpcClientNotifier.of(call, Integer::parseInt, config);
	}

	@After
	public void after() {
		notifier.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotifyListener() throws InterruptedException {
		TestListener<Integer> listener = TestListener.of(notifier);
		call.await();
		call.callback.onNext("123");
		assertThat(listener.await(false), is(123));
	}

	@Test
	public void shouldNotListenTwice() {
		ValueCondition<Integer> sync = ValueCondition.of();
		Consumer<Integer> fn1 = sync::signal;
		Consumer<Integer> fn2 = sync::signal;
		assertThat(notifier.listen(fn1), is(true));
		assertThat(notifier.listen(fn2), is(true));
		assertThat(notifier.listen(fn1), is(false));
		assertThat(notifier.unlisten(fn1), is(true));
		assertThat(notifier.unlisten(fn2), is(true));
		assertThat(notifier.unlisten(fn1), is(false));
	}

	@Test
	public void shouldStopListening() throws InterruptedException {
		try (TestListener<Integer> listener = TestListener.of(notifier)) {
			call.await();
		}
		call.response.completed.await();
	}

	@Test
	public void shouldClearListeners() throws InterruptedException {
		try (TestListener<Integer> listener = TestListener.of(notifier)) {
			call.await();
			notifier.clear();
			call.response.completed.await();
			notifier.clear();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldResetOnServiceError() throws InterruptedException {
		LogModifier.run(() -> {
			TestListener.of(notifier);
			call.await();
			call.callback.onError(new IllegalStateException("already half-closed"));
			call.callback.onError(new RuntimeException("test"));
			call.await();
		}, Level.ERROR, RpcClientNotifier.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldResetOnServiceCompletion() throws InterruptedException {
		TestListener.of(notifier);
		call.await();
		call.callback.onCompleted();
		call.await();
	}

	private static class TestCall
		implements Function<StreamObserver<String>, StreamObserver<Empty>> {
		BooleanCondition apply = BooleanCondition.of(); // signaled when apply is called
		volatile TestObserver<Empty> response = null;
		volatile StreamObserver<String> callback = null; // service-side observer

		@Override
		public StreamObserver<Empty> apply(StreamObserver<String> callback) {
			this.callback = callback;
			response = TestObserver.of();
			apply.signal();
			return response;
		}

		public void await() throws InterruptedException {
			apply.await();
		}
	}

}
