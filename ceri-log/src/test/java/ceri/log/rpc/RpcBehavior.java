package ceri.log.rpc;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.test.Captor;

public class RpcBehavior {
	private static final int PORT = 12345;
	private static final int SHUTDOWN_TIMEOUT_MS = 30000;
	private static final int NOTIFIER_RESET_DELAY_MS = 0;
	private TestRpcContainer rpc;

	@Before
	public void before() throws IOException {
		rpc = new TestRpcContainer(PORT, SHUTDOWN_TIMEOUT_MS, NOTIFIER_RESET_DELAY_MS);
	}

	@After
	public void afterClass() throws IOException {
		rpc.close();
	}

	@Test
	public void shouldHandleNotifications() throws InterruptedException {
		var sync = ValueCondition.of();
		try (var _ = rpc.client0.listen(i -> sync.signal(i))) {
			rpc.service.waitForClients(1);
			rpc.service.notify(1);
			assertEquals(sync.await(), 1);
			rpc.service.notify(2);
			assertEquals(sync.await(), 2);
			rpc.service.notify(3);
			assertEquals(sync.await(), 3);
		}
		rpc.service.waitForClients(0);
	}

	@Test
	public void shouldHandleNoArgumentsWithNoReturnValue() throws IOException {
		var captor = Captor.ofInt();
		rpc.service.run = () -> captor.accept(captor.values.size() + 1);
		rpc.client0.run();
		rpc.client0.run();
		rpc.client0.run();
		captor.verifyInt(1, 2, 3);
	}

	@Test
	public void shouldHandleArgumentsWithNoReturnValue() throws IOException {
		var captor = Captor.ofInt();
		rpc.service.set = i -> captor.accept(i);
		rpc.client0.set(1);
		rpc.client0.set(2);
		rpc.client0.set(3);
		captor.verifyInt(1, 2, 3);
	}

	@Test
	public void shouldHandleNoArgumentsWithReturnValue() throws IOException {
		var captor = Captor.ofInt();
		rpc.service.get = () -> captor.values.size() + 1;
		captor.accept(rpc.client0.get());
		captor.accept(rpc.client0.get());
		captor.accept(rpc.client0.get());
		captor.verifyInt(1, 2, 3);
	}

}
