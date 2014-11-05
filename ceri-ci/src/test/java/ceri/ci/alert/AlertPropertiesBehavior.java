package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
		assertThat(properties.reminderMs(), is(999999L));
		assertThat(properties.shutdownTimeoutMs(), is(777L));
		assertThat(properties.purgeDelayMs(), is(44444L));
	}

}
