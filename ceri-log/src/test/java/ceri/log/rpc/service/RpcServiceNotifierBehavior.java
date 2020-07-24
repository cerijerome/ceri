package ceri.log.rpc.service;

import static ceri.log.rpc.util.RpcUtil.EMPTY;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import com.google.protobuf.Empty;
import ceri.common.event.Listeners;
import ceri.common.util.BasicUtil;
import ceri.log.test.LogModifier;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class RpcServiceNotifierBehavior {

	@Test
	public void shouldStopNotifyingOnClientError() throws InterruptedException {
		Listeners<String> listeners = new Listeners<>();
		try (var notifier = RpcServiceNotifier.of(listeners, Integer::parseInt)) {
			StreamObserver<Integer> client = BasicUtil.uncheckedCast(mock(StreamObserver.class));
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
