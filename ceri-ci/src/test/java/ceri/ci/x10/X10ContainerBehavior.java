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
import ceri.x10.cm11a.Cm11aConnector;
import ceri.x10.cm11a.Cm11aController;
import ceri.x10.cm17a.Cm17aConnector;
import ceri.x10.cm17a.Cm17aController;

public class X10ContainerBehavior {
	@Mock private Cm17aConnector cm17aConnector;
	@Mock private Cm17aController cm17aController;
	@Mock private Cm11aConnector cm11aConnector;
	@Mock private Cm11aController cm11aController;
	@Mock private X10Alerter.Builder builder;
	@Mock private X10Factory factory;
	private Properties properties;

	@SuppressWarnings("resource")
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(factory.createCm11aConnector("com")).thenReturn(cm11aConnector);
		when(factory.createCm11aController(cm11aConnector)).thenReturn(cm11aController);
		when(factory.createCm17aConnector("com")).thenReturn(cm17aConnector);
		when(factory.createCm17aController(cm17aConnector)).thenReturn(cm17aController);
		when(factory.builder(cm17aController)).thenReturn(builder);
		when(factory.builder(cm11aController)).thenReturn(builder);
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
		verify(factory).builder(cm17aController);
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
		verify(cm17aConnector).close();
		verify(cm17aController).close();
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
		verify(factory).createCm17aConnector("com");
		verify(factory).createCm17aController(cm17aConnector);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCreateCm11aIfControllerPropertyIsSet() throws IOException {
		properties.put("x10.enabled", "true");
		properties.put("x10.comm.port", "com");
		properties.put("x10.controller", "cm11a");
		@SuppressWarnings("unused")
		X10Container container = new X10Container(baseProperties(), factory);
		verify(factory).createCm11aConnector("com");
		verify(factory).createCm11aController(cm11aConnector);
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}
	
}
