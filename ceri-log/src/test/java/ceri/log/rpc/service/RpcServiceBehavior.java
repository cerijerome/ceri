package ceri.log.rpc.service;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.io.IOException;
import org.junit.Test;

public class RpcServiceBehavior {

	@Test
	public void shouldProvideDisabledContainer() throws IOException {
		try (var container = RpcService.start(null, RpcServer.Config.NULL)) {
			assertEquals(container.port(), RpcServer.NULL.port());
			assertFalse(container.enabled());
			assertFind(container, "null-service");
		}
	}

	@Test
	public void shouldStartService() throws IOException {
		try (var container = RpcService.start(() -> RpcService.NULL, RpcServer.Config.DEFAULT)) {
			assertTrue(container.enabled());
		}
	}

	@Test
	public void shouldCloseServiceIfUnableToStart() {
		assertThrown(() -> RpcService.start(() -> (RpcService.Null) throwRuntime(),
			RpcServer.Config.DEFAULT));
	}

}
