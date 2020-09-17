package ceri.log.rpc.client;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class RpcClientNotifierConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RpcClientNotifierConfig t = RpcClientNotifierConfig.of(999);
		RpcClientNotifierConfig eq0 = RpcClientNotifierConfig.of(999);
		RpcClientNotifierConfig eq1 = RpcClientNotifierConfig.builder().resetDelayMs(999).build();
		RpcClientNotifierConfig ne0 = RpcClientNotifierConfig.of(998);
		RpcClientNotifierConfig ne1 = RpcClientNotifierConfig.of();
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldBuildFromProperties() {
		RpcClientNotifierConfig config =
			new RpcClientNotifierProperties(baseProperties("rpc-client"), "rpc-client.notifier")
				.config();
		assertThat(config.resetDelayMs, is(1000));
	}

}
