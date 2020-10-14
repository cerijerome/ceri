package ceri.log.rpc;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.Capturer;

public class RpcBehavior {
	private static final int PORT = 12345;
	private static final int SHUTDOWN_TIMEOUT_MS = 30000;
	private static final int NOTIFIER_RESET_DELAY_MS = 0;
	private static TestRpcContainer rpc;

	@BeforeClass
	public static void beforeClass() throws IOException {
		rpc = new TestRpcContainer(PORT, SHUTDOWN_TIMEOUT_MS, NOTIFIER_RESET_DELAY_MS);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		rpc.close();
	}

	@Before
	public void before() {
		rpc.reset();
	}

	@Test
	public void shouldHandleNotifications() throws InterruptedException {
		var sync = ValueCondition.of();
		try (var listener = rpc.client0.listen(i -> sync.signal(i))) {
			rpc.service.waitForClients(1);
			rpc.service.notify(1);
			assertThat(sync.await(), is(1));
			rpc.service.notify(2);
			assertThat(sync.await(), is(2));
			rpc.service.notify(3);
			assertThat(sync.await(), is(3));
		}
		rpc.service.waitForClients(0);
	}

	@Test
	public void shouldHandleNoArgumentsWithNoReturnValue() throws IOException {
		var captor = Capturer.ofInt();
		rpc.service.run = () -> captor.accept(captor.values.size() + 1);
		rpc.client0.run();
		rpc.client0.run();
		rpc.client0.run();
		captor.verifyInt(1, 2, 3);
	}

	@Test
	public void shouldHandleArgumentsWithNoReturnValue() throws IOException {
		var captor = Capturer.ofInt();
		rpc.service.set = i -> captor.accept(i);
		rpc.client0.set(1);
		rpc.client0.set(2);
		rpc.client0.set(3);
		captor.verifyInt(1, 2, 3);
	}

	@Test
	public void shouldHandleNoArgumentsWithReturnValue() throws IOException {
		var captor = Capturer.ofInt();
		rpc.service.get = () -> captor.values.size() + 1;
		captor.accept(rpc.client0.get());
		captor.accept(rpc.client0.get());
		captor.accept(rpc.client0.get());
		captor.verifyInt(1, 2, 3);
	}

}
