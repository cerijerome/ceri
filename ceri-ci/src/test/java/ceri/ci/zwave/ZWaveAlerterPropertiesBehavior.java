package ceri.ci.zwave;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class ZWaveAlerterPropertiesBehavior {
	private static Properties props = new Properties();

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.host", "aaa");
		props.put("x.device.A", "1");
		props.put("x.device.B", "2");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		ZWaveAlerterProperties zwave = new ZWaveAlerterProperties(props, "x");
		assertThat(zwave.enabled(), is(true));
		assertThat(zwave.host(), is("aaa"));
		assertThat(zwave.device("A"), is(1));
		assertThat(zwave.device("B"), is(2));
		assertCollection(zwave.names(), "A", "B");
	}

}