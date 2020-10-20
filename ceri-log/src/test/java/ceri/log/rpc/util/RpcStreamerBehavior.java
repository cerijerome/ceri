package ceri.log.rpc.util;

import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertThrown;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.common.util.BasicUtil;
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
		StreamObserver<String> observer = BasicUtil.uncheckedCast(mock(StreamObserver.class));
		doThrow(new IllegalStateException("already half-closed")).when(observer).onCompleted();
		try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {}
	}

	@Test
	public void shouldLogErrorOnClose() {
		LogModifier.run(() -> {
			StreamObserver<String> observer = BasicUtil.uncheckedCast(mock(StreamObserver.class));
			doThrow(new RuntimeException()).when(observer).onCompleted();
			try (RpcStreamer<String> streamer = RpcStreamer.of(observer)) {}
		}, Level.ERROR, RpcStreamer.class);
	}
}
