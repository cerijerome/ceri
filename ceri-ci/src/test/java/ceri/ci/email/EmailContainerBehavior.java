package ceri.ci.email;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.build.BuildEventProcessor;
import ceri.ci.phone.PhoneAlerter;
import ceri.ci.phone.PhoneFactory;
import ceri.common.property.BaseProperties;

public class EmailContainerBehavior {
	@Mock private BuildEventProcessor processor;
	@Mock private PhoneAlerter.Builder builder;
	@Mock private PhoneFactory factory;
	private Properties properties;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		properties = new Properties();
	}

	@Test
	public void should() {
		EmailEventParser parser0 = null;
		EmailEventParser parser1 = null;
		EmailContainer container = new EmailContainer(baseProperties(), processor, parser0, parser1);
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}
	
}
