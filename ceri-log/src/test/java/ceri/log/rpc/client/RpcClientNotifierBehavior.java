package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.typedProperties;
import static ceri.log.rpc.util.RpcUtil.EMPTY;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.test.CallSync;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.test.LogModifier;
import io.grpc.stub.StreamObserver;

public class RpcClientNotifierBehavior {
	private final RpcClientNotifier.Config config = new RpcClientNotifier.Config(1);
	private TestStreamObserver<Empty> serverControl;
	private CallSync.Function<StreamObserver<String>, StreamObserver<Empty>> serverCall;
	private CallSync.Function<String, Integer> transform;
	private RpcClientNotifier<Integer, String> notifier;

	@Before
	public void before() {
		serverControl = TestStreamObserver.of(); // controls server calls
		serverCall = CallSync.function(null, serverControl); // acts as server call
		transform = CallSync.function(null, 0); // converts rpc type to notify type
		notifier = RpcClientNotifier.of(serverCall::apply, transform::apply, config);
	}

	@After
	public void after() {
		notifier.close();
	}

	@Test
	public void shouldBuildFromProperties() {
		var config =
			new RpcClientNotifier.Properties(typedProperties("rpc-client"), "rpc-client.notifier")
				.config();
		assertEquals(config.resetDelayMs(), 1000);
	}

	@Test
	public void shouldReconnectOnServerCompletion() {
		StreamObserver<String> clientControl = null;
		CallSync.Consumer<Integer> sync = CallSync.consumer(null, true);
		try (var _ = notifier.enclose(sync::accept)) {
			// starts listening
			clientControl = serverCall.awaitAuto(); // start streaming
			serverControl.next.assertAuto(EMPTY); // start listening
			// stop from server
			clientControl.onCompleted();
			serverControl.completed.awaitAuto(); // stop listening
			clientControl = serverCall.awaitAuto(); // start streaming
			serverControl.next.assertAuto(EMPTY); // start listening
		}
		serverControl.completed.awaitAuto(); // stop (makes sure reset delay happens)
	}

	@Test
	public void shouldReconnectOnServerError() {
		CallSync.Consumer<Integer> sync = CallSync.consumer(null, true);
		LogModifier.run(() -> {
			try (var _ = notifier.enclose(sync::accept)) {
				// starts listening
				var clientControl = serverCall.awaitAuto(); // start streaming
				serverControl.next.assertAuto(EMPTY); // start listening
				// error from server
				clientControl.onError(new IllegalStateException("already half-closed"));
				clientControl.onError(new IOException("test")); // logged
				clientControl = serverCall.awaitAuto(); // start streaming
			}
		}, Level.OFF, RpcClientNotifier.class);
	}

	@Test
	public void shouldClearListeners() {
		notifier.clear(); // does nothing
		CallSync.Consumer<Integer> sync = CallSync.consumer(null, true);
		try (var _ = notifier.enclose(sync::accept)) {
			notifier.clear();
		}
	}

	@Test
	public void shouldListenAndUnlisten() {
		CallSync.Consumer<Integer> sync0 = CallSync.consumer(null, true);
		CallSync.Consumer<Integer> sync1 = CallSync.consumer(null, true);
		Consumer<Integer> listener0 = sync0::accept;
		Consumer<Integer> listener1 = sync1::accept;
		assertTrue(notifier.listen(listener0));
		assertFalse(notifier.listen(listener0));
		assertFalse(notifier.unlisten(listener1));
		assertTrue(notifier.listen(listener1));
		assertTrue(notifier.unlisten(listener0));
		assertTrue(notifier.unlisten(listener1));
	}

}
