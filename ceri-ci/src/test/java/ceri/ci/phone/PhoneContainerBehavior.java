package ceri.ci.phone;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.property.BaseProperties;

public class PhoneContainerBehavior {
	@Mock private PhoneClient client;
	@Mock private PhoneAlerter.Builder builder;
	@Mock private PhoneFactory factory;
	private Properties properties;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(factory.createClient(any(), any(), any())).thenReturn(client);
		when(factory.builder(any())).thenReturn(builder);
		when(builder.number(any(), any())).thenReturn(builder);
		properties = new Properties();
	}

	@Test
	public void shouldRegisterKeysToDevices() {
		properties.put("phone.enabled", "true");
		properties.put("phone.number.A", "aaa");
		properties.put("phone.number.BB", "bb");
		properties.put("phone.number.CCC", "c");
		@SuppressWarnings({ "unused" })
		PhoneContainer container = new PhoneContainer(baseProperties(), factory);
		verify(factory).builder(client);
		verify(builder).number("A", "aaa");
		verify(builder).number("BB", "bb");
		verify(builder).number("CCC", "c");
	}

	@Test
	public void shouldNotCreateAlerterIfPropertyIsMissingOrDisabled() {
		PhoneContainer container = new PhoneContainer(baseProperties(), factory);
		assertNull(container.alerter);
		properties.put("phone.enabled", "false");
		container = new PhoneContainer(baseProperties(), factory);
		assertNull(container.alerter);
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}
	
}
