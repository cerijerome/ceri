package ceri.log.rpc.client;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.test.TestUtil.properties;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class RpcChannelConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RpcChannelConfig t = RpcChannelConfig.of("127.0.1.1", 12345);
		RpcChannelConfig eq0 = RpcChannelConfig.of("127.0.1.1", 12345);
		RpcChannelConfig eq1 = RpcChannelConfig.builder().host("127.0.1.1").port(12345).build();
		RpcChannelConfig ne0 = RpcChannelConfig.of("127.0.0.1", 12345);
		RpcChannelConfig ne1 = RpcChannelConfig.of("127.0.1.1", 12344);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldDetermineIfEnabled() {
		assertThat(RpcChannelConfig.NULL.enabled(), is(false));
		assertThat(RpcChannelConfig.builder().host("localhost").build().enabled(), is(false));
		assertThat(RpcChannelConfig.of("host", 12345).enabled(), is(true));
	}

	@Test
	public void shouldBuildFromProperties() {
		RpcChannelConfig config =
			new RpcChannelProperties(properties("rpc-client"), "rpc-client").config();
		assertThat(config.host, is("127.0.0.1"));
		assertThat(config.port, is(12345));
	}

}
