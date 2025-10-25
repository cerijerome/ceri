package ceri.log.concurrent;

import static ceri.common.test.Assert.assertEquals;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteProvider;

public class SocketListenerBehavior {

	@Test
	public void shouldNotifyOnData() throws IOException, InterruptedException {
		ValueCondition<ByteProvider> sync = ValueCondition.of();
		try (SocketListener sl = SocketListener.of(12345)) {
			sl.listeners().listen(sync::signal);
			send("test", sl.port());
			assertEquals(sync.await().getAscii(0), "test");
		}
	}

	@Test
	public void shouldExecuteOnNotification() throws IOException, InterruptedException {
		BoolCondition sync = BoolCondition.of();
		try (SocketListener sl = SocketListener.of(12345, () -> sync.signal())) {
			send("test", sl.port());
			sync.await();
		}
	}

	@Test
	public void shouldExecuteOnMatchingNotification() throws IOException, InterruptedException {
		ValueCondition<Integer> sync = ValueCondition.of();
		AtomicInteger value = new AtomicInteger(0);
		try (SocketListener sl = SocketListener.of(12345, () -> sync.signal(value.addAndGet(1)),
			s -> s.startsWith("test"))) {
			send("Test1", sl.port());
			send("test2", sl.port());
			assertEquals(sync.await(), 1);
		}
	}

	private void send(String data, int port) throws IOException {
		try (Socket s = new Socket("localhost", port)) {
			@SuppressWarnings("resource")
			OutputStream out = s.getOutputStream();
			out.write(data.getBytes(StandardCharsets.ISO_8859_1));
			out.flush();
		}
	}
}
