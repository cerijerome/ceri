package ceri.ci.alert;

import static ceri.common.test.TestUtil.assertNotNull;
import static ceri.common.test.TestUtil.assertNull;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.common.Alerter;
import ceri.ci.email.EmailEventParser;

public class AlertContainerBehavior {
	@Mock
	private Alerter alerter0;
	@Mock
	private Alerter alerter1;
	@Mock
	private EmailEventParser parser0;
	private Properties properties;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		properties = new Properties();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotCreateEmailServiceIfDisabled() throws IOException {
		properties.setProperty("node0.email.enabled", "false");
		AlertContainer.Builder builder = AlertContainer.builder(properties);
		builder.parsers(parser0);
		try (AlertContainer con = builder.build()) {
			assertNull(con.email());
		}
	}

	@Test
	public void shouldCreateAlertService() throws IOException {
		AlertContainer.Builder builder = AlertContainer.builder(properties);
		builder.alerters(alerter0, alerter1);
		try (AlertContainer con = builder.build()) {
			assertNotNull(con.alert());
		}
	}

}
