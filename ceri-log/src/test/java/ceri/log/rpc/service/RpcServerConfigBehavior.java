package ceri.log.rpc.service;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.log.rpc.client.RpcChannelConfig;

public class RpcServerConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RpcServerConfig t = RpcServerConfig.of(12345);
		RpcServerConfig eq0 = RpcServerConfig.builder().port(12345).build();
		RpcServerConfig ne0 = RpcServerConfig.of(12344);
		RpcServerConfig ne1 = RpcServerConfig.builder().port(12345).shutdownTimeoutMs(0).build();
		RpcServerConfig ne2 = RpcServerConfig.of();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldBuildFromProperties() {
		RpcServerConfig config =
			new RpcServerProperties(baseProperties("rpc-server"), "rpc-server").config();
		assertThat(config.port, is(12345));
		assertThat(config.shutdownTimeoutMs, is(1000));
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertThat(RpcServerConfig.NULL.enabled(), is(false));
		assertThat(RpcServerConfig.of().enabled(), is(true));
	}

	@Test
	public void shouldDetermineIfLoop() {
		assertThat(RpcServerConfig.NULL.isLoop(RpcChannelConfig.localhost(12345)), is(false));
		assertThat(RpcServerConfig.of(12345).isLoop(null), is(false));
		assertThat(RpcServerConfig.of(12345).isLoop(RpcChannelConfig.of("xxx", 12345)), is(false));
		assertThat(RpcServerConfig.of(12345).isLoop(RpcChannelConfig.localhost(12345)), is(true));
	}

}
