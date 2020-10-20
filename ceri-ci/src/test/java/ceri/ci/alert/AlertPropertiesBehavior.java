package ceri.ci.alert;

import static ceri.common.test.AssertUtil.assertEquals;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class AlertPropertiesBehavior {
	private static Properties props = new Properties();

	@BeforeClass
	public static void init() {
		props.put("x.reminder.ms", "999999");
		props.put("x.shutdown.timeout.ms", "777");
		props.put("x.purge.delay.ms", "44444");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		AlertProperties properties = new AlertProperties(new BaseProperties(props) {}, "x");
		assertEquals(properties.reminderMs(), 999999L);
		assertEquals(properties.shutdownTimeoutMs(), 777L);
		assertEquals(properties.purgeDelayMs(), 44444L);
	}

}
