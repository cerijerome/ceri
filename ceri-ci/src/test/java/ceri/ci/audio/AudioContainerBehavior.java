package ceri.ci.audio;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.common.property.BaseProperties;

public class AudioContainerBehavior {
	@Mock private AudioMessages messages;
	@Mock private AudioAlerter alerter;
	@Mock private AudioFactory factory;
	private Properties properties;

	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		when(factory.createMessages(any(), anyFloat())).thenReturn(messages);
		when(factory.createAlerter(messages, null)).thenReturn(alerter);
		properties = new Properties();
	}

	@Test
	public void shouldCreateAudioAlerterIfPropertyIsEnabled() throws IOException {
		properties.put("audio.enabled", "true");
		@SuppressWarnings({ "unused", "resource" })
		AudioContainer container = new AudioContainer(baseProperties(), factory, null);
		verify(factory).createMessages(any(), anyFloat());
		verify(factory).createAlerter(messages, null);
	}

	@Test
	public void shouldCloseResources() throws IOException {
		try (AudioContainer container = new AudioContainer(baseProperties(), factory, null)) {}
		properties.put("audio.enabled", "true");
		try (AudioContainer container = new AudioContainer(baseProperties(), factory, null)) {}
		verify(alerter).close();
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}
	
}
