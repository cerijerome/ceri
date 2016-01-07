package ceri.log.concurrent;

import static ceri.common.test.TestUtil.assertArray;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.concurrent.BooleanCondition;

public class SocketListenerBehavior {
	private static final int PORT = 9999;
	private static final int BUFFER_SIZE = 20;
	private BooleanCondition lastSync = BooleanCondition.create();
	private ImmutableByteArray lastData;

	@Test
	public void shouldOnlyNotifyOnSuccessfulTestInput() throws IOException, InterruptedException {
		BooleanCondition sync = BooleanCondition.create();
		try (SocketListener sl =
			SocketListener.create(PORT, () -> sync.signal(), s -> s.length() > 1)) {
			send("\0");
			send("xx");
			assertTrue(sync.await(1000));
			send("x");
			send("\0\0");
			assertTrue(sync.await(1000));
		}
	}

	@Test
	public void shouldNotifyOnInput() throws IOException, InterruptedException {
		BooleanCondition sync = BooleanCondition.create();
		try (SocketListener sl = SocketListener.create(PORT, () -> sync.signal())) {
			send("\0");
			assertTrue(sync.await(1000));
			send("xxxxxxxxxxxxxxxxxxxx");
			assertTrue(sync.await(1000));
		}
	}

	@Test
	public void shouldCaptureInput() throws IOException, InterruptedException {
		try (SocketListener sl = SocketListener.create(PORT)) {
			sl.listen(this::setLastData);
			send("\0");
			verifyLastData(new byte[] { 0 });
			send("xxxxxxxxxxxxxxxxxxxx");
			verifyLastData("xxxxxxxxxxxxxxxxxxxx");
		}
	}

	@Test
	public void shouldAllowToListenAndUnlisten() throws IOException, InterruptedException {
		Consumer<ImmutableByteArray> listener = this::setLastData;
		try (SocketListener sl = SocketListener.create(PORT)) {
			sl.listen(listener);
			send("\0");
			verifyLastData(new byte[] { 0 });
			sl.unlisten(listener);
			send("xxxxxxxxxxxxxxxxxxxx");
		}
		assertFalse(lastSync.isSet());
	}

	@Test
	public void shouldBeAbleToCloseItself() throws IOException, InterruptedException {
		BooleanCondition closeSync = BooleanCondition.create();
		try (SocketListener sl = createWithCloseSync(closeSync)) {
			sl.listen(data -> sl.close());
			send("\0");
		}
		assertTrue(closeSync.await(1000));
	}

	private void verifyLastData(String expected) throws InterruptedException {
		verifyLastData(expected.getBytes());
	}

	private void verifyLastData(byte[] expectedData) throws InterruptedException {
		assertTrue(lastSync.await(1000));
		assertArray(lastData.copy(), expectedData);
		clearLastData();
	}

	private void setLastData(ImmutableByteArray data) {
		assertFalse(lastSync.isSet());
		assertNull(lastData);
		lastData = data;
		lastSync.signal();
	}

	private void clearLastData() {
		lastSync.clear();
		lastData = null;
	}

	private void send(String data) throws IOException {
		try (Socket s = new Socket("localhost", PORT)) {
			s.getOutputStream().write(data.getBytes());
		}
	}

	private SocketListener createWithCloseSync(BooleanCondition closeSync) throws IOException {
		return new SocketListener(PORT, BUFFER_SIZE) {
			@Override
			public void close() {
				super.close();
				closeSync.signal();
			}
		};
	}

}
