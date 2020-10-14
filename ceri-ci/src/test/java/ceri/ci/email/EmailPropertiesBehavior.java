package ceri.ci.email;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class EmailPropertiesBehavior {
	private static Properties props = new Properties();

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.host", "host");
		props.put("x.account", "account");
		props.put("x.folder", "folder");
		props.put("x.max.look.back.ms", "1000");
		props.put("x.password", "password");
		props.put("x.poll.ms", "777");
		props.put("x.protocol", "protocol");
		props.put("x.sent.date.buffer.ms", "999");
		props.put("x.shutdown.timeout.ms", "111");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		EmailProperties email = new EmailProperties(new BaseProperties(props) {}, "x");
		assertThat(email.enabled(), is(true));
		assertThat(email.host(), is("host"));
		assertThat(email.account(), is("account"));
		assertThat(email.folder(), is("folder"));
		assertThat(email.maxLookBackMs(), is(1000L));
		assertThat(email.password(), is("password"));
		assertThat(email.pollMs(), is(777L));
		assertThat(email.protocol(), is("protocol"));
		assertThat(email.sentDateBufferMs(), is(999L));
		assertThat(email.shutdownTimeoutMs(), is(111L));
	}

}
