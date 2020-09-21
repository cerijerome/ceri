package ceri.common.io;

import static ceri.common.test.TestUtil.assertRead;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.data.ByteUtil;

public class PipedConnectorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideBytes() throws IOException {
		try (var con = PipedConnector.of()) {
			try (var exec = SimpleExecutor.run(() -> {
				con.to.writeAscii("test");
				con.to.writeBytes(1, 2, 3);
			})) {
				ConcurrentUtil.delay(5); // prevent PipedInputStream invoking wait(1000)
				assertRead(con.in(), 't', 'e', 's', 't', 1, 2, 3);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReceiveBytes() throws IOException {
		try (var con = PipedConnector.of()) {
			try (var exec = SimpleExecutor.run(() -> {
				con.out().write(ByteUtil.toAscii("test").copy(0));
				con.out().write(ArrayUtil.bytes(1, 2, 3));
			})) {
				ConcurrentUtil.delay(5); // prevent PipedInputStream invoking wait(1000)
				assertRead(con.from, 't', 'e', 's', 't', 1, 2, 3);
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldClearInputs() throws IOException {
		try (var con = PipedConnector.of()) {
			try (var exec0 = SimpleExecutor.run(() -> con.to.write(ArrayUtil.bytes(1, 2, 3)))) {
				try (var exec1 = SimpleExecutor.run(() -> con.out().write(ArrayUtil.bytes(4, 5)))) {
					ConcurrentUtil.delay(5); // prevent PipedInputStream invoking wait(1000)
					con.clear();
					assertThat(con.in().available(), is(0));
					assertThat(con.from.available(), is(0));
				}
			}
		}
	}

}
