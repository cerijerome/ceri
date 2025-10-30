package ceri.log.rpc.client;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.Testing;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.rpc.util.Rpc;
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
		notifier = Testing.close(notifier);
		transform = null;
		serverCall = null;
		serverControl = null;
	}

	@Test
	public void shouldBuildFromProperties() {
		var config = new RpcClientNotifier.Properties(Testing.properties("rpc-client"),
			"rpc-client.notifier").config();
		Assert.equal(config.resetDelayMs(), 1000);
	}

	@Test
	public void shouldReconnectOnServerCompletion() {
		init();
		StreamObserver<String> clientControl = null;
		var sync = CallSync.<Integer>consumer(null, true);
		try (var _ = notifier.enclose(sync::accept)) {
			// starts listening
			clientControl = serverCall.awaitAuto(); // start streaming
			serverControl.next.assertAuto(Rpc.EMPTY); // start listening
			// stop from server
			clientControl.onCompleted();
			serverControl.completed.awaitAuto(); // stop listening
			clientControl = serverCall.awaitAuto(); // start streaming
			serverControl.next.assertAuto(Rpc.EMPTY); // start listening
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
				serverControl.next.assertAuto(Rpc.EMPTY); // start listening
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
		Assert.yes(notifier.listen(listener0));
		Assert.no(notifier.listen(listener0));
		Assert.no(notifier.unlisten(listener1));
		Assert.yes(notifier.listen(listener1));
		Assert.yes(notifier.unlisten(listener0));
		Assert.yes(notifier.unlisten(listener1));
	}

	private void init() {
		serverControl = TestStreamObserver.of(); // controls server calls
		serverCall = CallSync.function(null, serverControl); // acts as server call
		transform = CallSync.function(null, 0); // converts rpc type to notify type
		notifier = RpcClientNotifier.of(serverCall::apply, transform::apply, config);
	}
}
