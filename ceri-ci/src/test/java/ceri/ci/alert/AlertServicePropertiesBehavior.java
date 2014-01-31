package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class AlertServicePropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.reminder.ms", "999999");
		props.put("x.shutdown.timeout.ms", "999");
		props.put("reminder.ms", "777777");
		props.put("shutdown.timeout.ms", "777");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		AlertServiceProperties serviceProps = new AlertServiceProperties(props, "x");
		assertThat(serviceProps.reminderMs(), is(999999L));
		assertThat(serviceProps.shutdownTimeoutMs(), is(999L));
	}

	@Test
	public void shouldReadValuesWithoutPrefix() {
		AlertServiceProperties serviceProps = new AlertServiceProperties(props);
		assertThat(serviceProps.reminderMs(), is(777777L));
		assertThat(serviceProps.shutdownTimeoutMs(), is(777L));
	}

}
