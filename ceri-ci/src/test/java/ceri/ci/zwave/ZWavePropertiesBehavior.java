package ceri.ci.zwave;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class ZWavePropertiesBehavior {
	private static Properties props = new Properties();

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.host", "aaa");
		props.put("x.call.delay.ms", "111");
		props.put("x.group.devices", "1,3, 100");
		props.put("x.device.A", "1");
		props.put("x.device.B", "2");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		ZWaveProperties zwave = new ZWaveProperties(new BaseProperties(props) {}, "x");
		assertTrue(zwave.enabled());
		assertEquals(zwave.host(), "aaa");
		assertEquals(zwave.callDelayMs(), 111);
		assertCollection(zwave.groupDevices(), 1, 3, 100);
		assertEquals(zwave.device("A"), 1);
		assertEquals(zwave.device("B"), 2);
		assertCollection(zwave.names(), "A", "B");
	}

}
