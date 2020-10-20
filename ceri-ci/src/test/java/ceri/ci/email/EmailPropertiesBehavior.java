package ceri.ci.email;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
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
		assertTrue(email.enabled());
		assertEquals(email.host(), "host");
		assertEquals(email.account(), "account");
		assertEquals(email.folder(), "folder");
		assertEquals(email.maxLookBackMs(), 1000L);
		assertEquals(email.password(), "password");
		assertEquals(email.pollMs(), 777L);
		assertEquals(email.protocol(), "protocol");
		assertEquals(email.sentDateBufferMs(), 999L);
		assertEquals(email.shutdownTimeoutMs(), 111L);
	}

}
