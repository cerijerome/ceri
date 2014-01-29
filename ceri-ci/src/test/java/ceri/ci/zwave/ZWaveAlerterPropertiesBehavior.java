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
		props.put("x.host", "aaa");
		props.put("x.device.A", "1");
		props.put("x.device.B", "2");
		props.put("host", "xxx");
		props.put("device.x", "10");
		props.put("device.y", "11");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		ZWaveAlerterProperties zwave = new ZWaveAlerterProperties(props, "x");
		assertThat(zwave.host(), is("aaa"));
		assertThat(zwave.device("A"), is(1));
		assertThat(zwave.device("B"), is(2));
		assertCollection(zwave.names(), "A", "B");
	}

	@Test
	public void shouldReadValuesWithoutPrefix() {
		ZWaveAlerterProperties zwave = new ZWaveAlerterProperties(props);
		assertThat(zwave.host(), is("xxx"));
		assertThat(zwave.device("x"), is(10));
		assertThat(zwave.device("y"), is(11));
		assertCollection(zwave.names(), "x", "y");
	}

}
