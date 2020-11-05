package ceri.log.rpc.util;

import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.RTX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.test.LogModifier;
import io.grpc.stub.StreamObserver;

public class RpcStreamerBehavior {

	@Test
	public void shouldPassThrough() {
		List<String> next = new ArrayList<>();
		StreamObserver<String> observer = RpcUtil.observer(next::add, t -> {});
		try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {
			streamer.next("test1");
			streamer.next("test2");
		}
		assertIterable(next, "test1", "test2");
	}

	@Test
	public void shouldNotAddNextAfterClose() {
		StreamObserver<String> observer = RpcUtil.observer(s -> {}, t -> {});
		@SuppressWarnings("resource")
		RpcStreamer<String> streamer = RpcStreamer.of(observer);
		streamer.close();
		assertTrue(streamer.closed());
		assertThrown(() -> streamer.next("test"));
	}

	@Test
	public void shouldNotAddNextAfterError() {
		StreamObserver<String> observer = RpcUtil.observer(s -> {}, t -> {});
		try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {
			streamer.error(new IOException());
			assertThrown(() -> streamer.next("test"));
		}
	}

	@Test
	public void shouldNotLogIgnoredErrorOnClose() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		observer.completed.error.set(new IllegalStateException("already half-closed"));
		try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {}
	}

	@Test
	public void shouldLogErrorOnClose() {
		LogModifier.run(() -> {
			TestStreamObserver<String> observer = TestStreamObserver.of();
			observer.completed.error.setFrom(RTX);
			try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {}
		}, Level.ERROR, RpcStreamer.class);
	}
}
