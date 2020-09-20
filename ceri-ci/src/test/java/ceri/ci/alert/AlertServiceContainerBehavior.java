package ceri.ci.alert;

import static org.junit.Assert.assertNotNull;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.common.Alerter;
import ceri.common.property.BaseProperties;

public class AlertServiceContainerBehavior {
	@Mock
	private Alerter alerter0;
	@Mock
	private Alerter alerter1;
	private Properties properties;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		properties = new Properties();
	}

	@Test
	public void shouldCreateAlertService() {
		try (AlertServiceContainer con =
			new AlertServiceContainer(baseProperties(), alerter0, alerter1)) {
			assertNotNull(con.service());
		}
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}

}
