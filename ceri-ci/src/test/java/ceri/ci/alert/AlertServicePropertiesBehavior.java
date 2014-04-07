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
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		AlertServiceProperties serviceProps = new AlertServiceProperties(props, "x");
		assertThat(serviceProps.reminderMs(), is(999999L));
	}

}
