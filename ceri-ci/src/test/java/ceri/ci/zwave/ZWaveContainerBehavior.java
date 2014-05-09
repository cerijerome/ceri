package ceri.ci.zwave;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.collection.CollectionUtil;
import ceri.common.property.BaseProperties;

public class ZWaveContainerBehavior {
	@Mock private ZWaveController controller;
	@Mock private ZWaveAlerter.Builder builder;
	@Mock private ZWaveFactory factory;
	private Properties properties;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(factory.createController(any())).thenReturn(controller);
		when(factory.builder(any())).thenReturn(builder);
		when(builder.device(any(), anyInt())).thenReturn(builder);
		properties = new Properties();
	}

	@Test
	public void shouldRegisterKeysToDevices() {
		properties.put("zwave.enabled", "true");
		properties.put("zwave.host", "host");
		properties.put("zwave.device.key0", "7");
		properties.put("zwave.device.key1", "33");
		@SuppressWarnings({ "unused" })
		ZWaveContainer container = new ZWaveContainer(baseProperties(), factory);
		verify(factory).builder(controller);
		verify(builder).device("key0", 7);
		verify(builder).device("key1", 33);
	}

	@Test
	public void shouldRegisterGroupDevices() {
		properties.put("zwave.enabled", "true");
		properties.put("zwave.host", "host");
		properties.put("zwave.group.devices", "7,33");
		@SuppressWarnings({ "unused" })
		ZWaveContainer container = new ZWaveContainer(baseProperties(), factory);
		Set<Integer> devices = CollectionUtil.addAll(new HashSet<Integer>(), 7, 33);
		verify(factory).createGroup(controller, devices);
	}

	@Test
	public void shouldNotCreateAlerterIfPropertyIsMissingOrDisabled() {
		ZWaveContainer container = new ZWaveContainer(baseProperties(), factory);
		assertNull(container.alerter);
		properties.put("zwave.enabled", "false");
		container = new ZWaveContainer(baseProperties(), factory);
		assertNull(container.alerter);
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}
	
}
