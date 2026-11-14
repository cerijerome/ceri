package ceri.log.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteProvider;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class SocketListenerBehavior {
	private SocketListener listener;

	@AfterEach
	public void afterEach() {
		listener = Testing.close(listener);
	}

	@Test
	public void shouldNotifyOnData() throws IOException, InterruptedException {
		ValueCondition<ByteProvider> sync = ValueCondition.of();
		listener = SocketListener.of(12345);
		listener.listeners().listen(sync::signal);
		send("test", listener.port());
		Assert.equal(sync.await().getAscii(0), "test");
	}

	@Test
	public void shouldExecuteOnNotification() throws IOException, InterruptedException {
		BoolCondition sync = BoolCondition.of();
		listener = SocketListener.of(12345, () -> sync.signal());
		send("test", listener.port());
		sync.await();
	}

	@Test
	public void shouldExecuteOnMatchingNotification() throws IOException, InterruptedException {
		ValueCondition<Integer> sync = ValueCondition.of();
		AtomicInteger value = new AtomicInteger(0);
		listener = SocketListener.of(12345, () -> sync.signal(value.addAndGet(1)),
			s -> s.startsWith("test"));
		send("Test1", listener.port());
		send("test2", listener.port());
		Assert.equal(sync.await(), 1);
	}

	private void send(String data, int port) throws IOException {
		try (var s = new Socket("localhost", port)) {
			@SuppressWarnings("resource")
			var out = s.getOutputStream();
			out.write(data.getBytes(StandardCharsets.ISO_8859_1));
			out.flush();
		}
	}
}
