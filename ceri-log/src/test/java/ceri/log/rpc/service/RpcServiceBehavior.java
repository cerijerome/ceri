package ceri.log.rpc.service;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertFind;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.throwRuntime;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

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
		Assert.thrown(() -> RpcService.start(() -> (RpcService.Null) throwRuntime(),
			RpcServer.Config.DEFAULT));
	}
}
