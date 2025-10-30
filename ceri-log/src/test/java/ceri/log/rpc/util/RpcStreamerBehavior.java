package ceri.log.rpc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.log.rpc.test.TestStreamObserver;
import ceri.log.test.LogModifier;
import io.grpc.stub.StreamObserver;

public class RpcStreamerBehavior {

	@Test
	public void shouldPassThrough() {
		List<String> next = new ArrayList<>();
		StreamObserver<String> observer = Rpc.observer(next::add, _ -> {});
		try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {
			streamer.next("test1");
			streamer.next("test2");
		}
		Assert.ordered(next, "test1", "test2");
	}

	@Test
	public void shouldNotAddNextAfterClose() {
		StreamObserver<String> observer = Rpc.observer(_ -> {}, _ -> {});
		@SuppressWarnings("resource")
		RpcStreamer<String> streamer = RpcStreamer.of(observer);
		streamer.close();
		Assert.yes(streamer.closed());
		Assert.thrown(() -> streamer.next("test"));
	}

	@Test
	public void shouldNotAddNextAfterError() {
		StreamObserver<String> observer = Rpc.observer(_ -> {}, _ -> {});
		try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {
			streamer.error(new IOException());
			Assert.thrown(() -> streamer.next("test"));
		}
	}

	@Test
	public void shouldNotLogIgnoredErrorOnClose() {
		TestStreamObserver<String> observer = TestStreamObserver.of();
		observer.completed.error.set(new IllegalStateException("already half-closed"));
		try (RpcStreamer<String> _ = RpcStreamer.of(observer)) {}
	}

	@Test
	public void shouldLogErrorOnClose() {
		LogModifier.run(() -> {
			TestStreamObserver<String> observer = TestStreamObserver.of();
			observer.completed.error.setFrom(ErrorGen.RTX);
			try (RpcStreamer<String> _ = RpcStreamer.of(observer)) {}
		}, Level.ERROR, RpcStreamer.class);
	}
}
