package ceri.log.rpc.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.test.TestListener;
import ceri.log.rpc.TestObserver;
import ceri.log.test.LogModifier;
import io.grpc.stub.StreamObserver;

public class RpcClientNotifierBehavior {
	private static final RpcClientNotifierConfig config = RpcClientNotifierConfig.of(0);
	private static TestCall call;
	private static RpcClientNotifier<Integer, String> notifier;

	@BeforeClass
	public static void beforeClass() {
		call = new TestCall();
		notifier = RpcClientNotifier.of(call, Integer::parseInt, config);
	}

	@AfterClass
	public static void afterClass() {
		notifier.close();
	}

	@Before
	public void before() {
		notifier.clear();
		call.clear();
	}

	@Test
	public void shouldTransformListenerNotifications() throws InterruptedException {
		try (TestListener<Integer> listener = TestListener.of(notifier)) {
			call.next.await();
			call.callback.onNext("123");
			assertThat(listener.listen.await(), is(123));
		}
		call.completed.await();
	}

	@Test
	public void shouldNotifyMultipleListeners() throws InterruptedException {
		try (TestListener<Integer> listener1 = TestListener.of(notifier)) {
			try (TestListener<Integer> listener2 = TestListener.of(notifier)) {
				call.next.await();
				call.callback.onNext("123");
				assertThat(listener1.listen.await(), is(123));
				assertThat(listener2.listen.await(), is(123));
			}
		}
		call.completed.await();
	}

	@Test
	public void shouldOnlyRegisterOnce() throws InterruptedException {
		try (TestListener<Integer> listener = TestListener.of(notifier)) {
			call.next.await();
			assertThat(notifier.listen(listener.listener()), is(false));
			assertThat(notifier.unlisten(listener.listener()), is(true));
			call.completed.await();
			assertThat(notifier.unlisten(listener.listener()), is(false));
		}
	}

	@Test
	public void shouldClearListeners() throws InterruptedException {
		try (TestListener<Integer> listener = TestListener.of(notifier)) {
			call.next.await();
			notifier.clear();
			call.completed.await();
		}
	}

	@Test
	public void shouldResetServiceOnCompletion() throws InterruptedException {
		try (TestListener<Integer> listener = TestListener.of(notifier)) {
			call.next.await();
			call.callback.onCompleted();
			call.completed.await();
			call.next.await();
		}
	}

	@Test
	public void shouldResetServiceOnError() throws InterruptedException {
		LogModifier.run(RpcClientNotifier.class, Level.ERROR, () -> {
			try (TestListener<Integer> listener = TestListener.of(notifier)) {
				call.next.await();
				call.callback.onError(new IOException("test"));
				call.next.await();
			}
			call.completed.await();
		});
	}

	/**
	 * Encapsulates the service call. The Function interface is called when the client notifier
	 * wants to start listening. The StreamObserver interface is called by the client notifier to
	 * add and remove the client from the server-side notification list. The parent class conditions
	 * can be used to wait until the calls are made.
	 */
	private static class TestCall extends TestObserver<Empty>
		implements Function<StreamObserver<String>, StreamObserver<Empty>> {
		StreamObserver<String> callback = null; // service-side observer

		@Override
		public StreamObserver<Empty> apply(StreamObserver<String> callback) {
			this.callback = callback;
			return this;
		}

		@Override
		public TestObserver<Empty> clear() {
			callback = null;
			return super.clear();
		}
	}

}
