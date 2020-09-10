package ceri.ci.x10;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.property.BaseProperties;
import ceri.x10.cm11a.Cm11aContainer;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm17a.Cm17aContainer;
import ceri.x10.cm17a.device.Cm17aDevice;

public class X10ContainerBehavior {
	@Mock
	private Cm11aContainer cm11aContainer;
	@Mock
	private Cm11aDevice cm11a;
	@Mock
	private Cm17aContainer cm17aContainer;
	@Mock
	private Cm17aDevice cm17a;
	@Mock
	private X10Alerter.Builder builder;
	@Mock
	private X10Factory factory;
	private Properties properties;

	@SuppressWarnings("resource")
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(factory.createCm11aContainer("com")).thenReturn(cm11aContainer);
		when(cm11aContainer.cm11a()).thenReturn(cm11a);
		when(factory.createCm17aContainer("com")).thenReturn(cm17aContainer);
		when(cm17aContainer.cm17a()).thenReturn(cm17a);
		when(factory.builder(cm17a)).thenReturn(builder);
		when(factory.builder(cm11a)).thenReturn(builder);
		properties = new Properties();
	}

	@Test
	public void shouldRegisterKeysToX10Addresses() throws IOException {
		properties.put("x10.enabled", "true");
		properties.put("x10.comm.port", "com");
		properties.put("x10.controller", "cm17a");
		properties.put("x10.address.key0", "P1");
		properties.put("x10.address.key1", "A16");
		@SuppressWarnings("unused")
		X10Container container = new X10Container(baseProperties(), factory);
		verify(factory).builder(cm17a);
		verify(builder).address("key0", "P1");
		verify(builder).address("key1", "A16");
	}

	@Test
	public void shouldCloseResources() throws IOException {
		try (X10Container container = new X10Container(baseProperties(), factory)) {}
		properties.put("x10.enabled", "true");
		properties.put("x10.comm.port", "com");
		properties.put("x10.controller", "cm17a");
		try (X10Container container = new X10Container(baseProperties(), factory)) {}
		verify(cm17aContainer).close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldNotCreateAlerterIfPropertyIsMissingOrDisabled() throws IOException {
		X10Container container = new X10Container(baseProperties(), factory);
		assertNull(container.alerter);
		properties.put("x10.enabled", "false");
		container = new X10Container(baseProperties(), factory);
		assertNull(container.alerter);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateCm17aIfControllerPropertyIsSet() throws IOException {
		properties.put("x10.enabled", "true");
		properties.put("x10.comm.port", "com");
		properties.put("x10.controller", "cm17a");
		@SuppressWarnings("unused")
		X10Container container = new X10Container(baseProperties(), factory);
		verify(factory).createCm17aContainer("com");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateCm11aIfControllerPropertyIsSet() throws IOException {
		properties.put("x10.enabled", "true");
		properties.put("x10.comm.port", "com");
		properties.put("x10.controller", "cm11a");
		@SuppressWarnings("unused")
		X10Container container = new X10Container(baseProperties(), factory);
		verify(factory).createCm11aContainer("com");
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}

}
