package ceri.log.rpc.service;

import static ceri.log.rpc.util.RpcUtil.EMPTY;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.event.Listeners;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.test.LogModifier;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcServiceNotifierBehavior {

	@Test
	public void shouldStopNotifyingOnClientError() throws InterruptedException {
		Listeners<String> listeners = Listeners.of();
		try (var notifier = RpcServiceNotifier.of(listeners, Integer::parseInt)) {
			TestStreamObserver<Integer> client = TestStreamObserver.of();
			StreamObserver<Empty> response = notifier.listen(client);
			response.onNext(EMPTY);
			notifier.waitForListener(i -> i == 1);
			response.onError(halfCloseException()); // not logged
			notifier.waitForListener(i -> i == 0);
			LogModifier.run(() -> {
				response.onError(new IOException());
			}, Level.ERROR, RpcServiceNotifier.class);
		}
	}

	private static StatusRuntimeException halfCloseException() {
		return Status.CANCELLED.withDescription("cancelled before receiving half close")
			.asRuntimeException();
	}
}
