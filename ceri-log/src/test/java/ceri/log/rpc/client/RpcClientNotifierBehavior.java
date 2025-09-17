package ceri.log.rpc.client;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.function.Functions;
import ceri.common.test.CallSync;
import ceri.common.test.TestUtil;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.rpc.util.RpcUtil;
import ceri.log.test.LogModifier;
import io.grpc.stub.StreamObserver;

public class RpcClientNotifierBehavior {
	private final RpcClientNotifier.Config config = new RpcClientNotifier.Config(1);
	private TestStreamObserver<Empty> serverControl;
	private CallSync.Function<StreamObserver<String>, StreamObserver<Empty>> serverCall;
	private CallSync.Function<String, Integer> transform;
	private RpcClientNotifier<Integer, String> notifier;

	@After
	public void after() {
		notifier = TestUtil.close(notifier);
		transform = null;
		serverCall = null;
		serverControl = null;
	}

	@Test
	public void shouldBuildFromProperties() {
		var config = new RpcClientNotifier.Properties(TestUtil.typedProperties("rpc-client"),
			"rpc-client.notifier").config();
		assertEquals(config.resetDelayMs(), 1000);
	}

	@Test
	public void shouldReconnectOnServerCompletion() {
		init();
		StreamObserver<String> clientControl = null;
		var sync = CallSync.<Integer>consumer(null, true);
		try (var _ = notifier.enclose(sync::accept)) {
			// starts listening
			clientControl = serverCall.awaitAuto(); // start streaming
			serverControl.next.assertAuto(RpcUtil.EMPTY); // start listening
			// stop from server
			clientControl.onCompleted();
			serverControl.completed.awaitAuto(); // stop listening
			clientControl = serverCall.awaitAuto(); // start streaming
			serverControl.next.assertAuto(RpcUtil.EMPTY); // start listening
		}
		serverControl.completed.awaitAuto(); // stop (makes sure reset delay happens)
	}

	@Test
	public void shouldReconnectOnServerError() {
		init();
		var sync = CallSync.<Integer>consumer(null, true);
		LogModifier.run(() -> {
			try (var _ = notifier.enclose(sync::accept)) {
				// starts listening
				var clientControl = serverCall.awaitAuto(); // start streaming
				serverControl.next.assertAuto(RpcUtil.EMPTY); // start listening
				// error from server
				clientControl.onError(new IllegalStateException("already half-closed"));
				clientControl.onError(new IOException("test")); // logged
				clientControl = serverCall.awaitAuto(); // start streaming
			}
		}, Level.OFF, RpcClientNotifier.class);
	}

	@Test
	public void shouldClearListeners() {
		init();
		notifier.clear(); // does nothing
		CallSync.Consumer<Integer> sync = CallSync.consumer(null, true);
		try (var _ = notifier.enclose(sync::accept)) {
			notifier.clear();
		}
	}

	@Test
	public void shouldListenAndUnlisten() {
		init();
		CallSync.Consumer<Integer> sync0 = CallSync.consumer(null, true);
		CallSync.Consumer<Integer> sync1 = CallSync.consumer(null, true);
		Functions.Consumer<Integer> listener0 = sync0::accept;
		Functions.Consumer<Integer> listener1 = sync1::accept;
		assertTrue(notifier.listen(listener0));
		assertFalse(notifier.listen(listener0));
		assertFalse(notifier.unlisten(listener1));
		assertTrue(notifier.listen(listener1));
		assertTrue(notifier.unlisten(listener0));
		assertTrue(notifier.unlisten(listener1));
	}

	private void init() {
		serverControl = TestStreamObserver.of(); // controls server calls
		serverCall = CallSync.function(null, serverControl); // acts as server call
		transform = CallSync.function(null, 0); // converts rpc type to notify type
		notifier = RpcClientNotifier.of(serverCall::apply, transform::apply, config);
	}
}
