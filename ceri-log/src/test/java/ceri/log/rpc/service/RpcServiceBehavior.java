package ceri.log.rpc.service;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class RpcServiceBehavior {

	@Test
	public void shouldProvideDisabledContainer() throws IOException {
		try (var container = RpcService.start(null, RpcServer.Config.NULL)) {
			Assert.equal(container.port(), RpcServer.NULL.port());
			Assert.no(container.enabled());
			Assert.find(container, "null-service");
		}
	}

	@Test
	public void shouldStartService() throws IOException {
		try (var container = RpcService.start(() -> RpcService.NULL, RpcServer.Config.DEFAULT)) {
			Assert.yes(container.enabled());
		}
	}

	@Test
	public void shouldCloseServiceIfUnableToStart() {
		Assert.thrown(() -> RpcService.start(() -> (RpcService.Null) Assert.throwRuntime(),
			RpcServer.Config.DEFAULT));
	}
}
