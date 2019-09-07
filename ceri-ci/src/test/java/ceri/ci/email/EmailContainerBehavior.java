package ceri.ci.email;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.build.BuildEventProcessor;
import ceri.common.property.BaseProperties;

public class EmailContainerBehavior {
	@Mock private BuildEventProcessor processor;
	@Mock private EmailService.Builder serviceBuilder;
	@Mock private EmailService service;
	@Mock private EmailRetrieverImpl.Builder retrieverBuilder;
	@Mock private EmailRetriever retriever;
	@Mock private EmailFactory factory;
	@Mock private EmailEventParser parser0;
	@Mock private EmailEventParser parser1;
	private Properties properties;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(factory.retrieverBuilder(any(), any(), any())).thenReturn(retrieverBuilder);
		when(factory.serviceBuilder(any(), any())).thenReturn(serviceBuilder);
		when(retrieverBuilder.protocol(any())).thenReturn(retrieverBuilder);
		when(retrieverBuilder.folder(any())).thenReturn(retrieverBuilder);
		when(retrieverBuilder.build()).thenReturn(retriever);
		when(serviceBuilder.parsers(anyCollection())).thenReturn(serviceBuilder);
		when(serviceBuilder.maxLookBackMs(anyLong())).thenReturn(serviceBuilder);
		when(serviceBuilder.pollMs(anyLong())).thenReturn(serviceBuilder);
		when(serviceBuilder.sentDateBufferMs(anyLong())).thenReturn(serviceBuilder);
		when(serviceBuilder.shutdownTimeoutMs(anyLong())).thenReturn(serviceBuilder);
		when(serviceBuilder.build()).thenReturn(service);
		properties = new Properties();
	}

	@Test
	public void shouldCreateRetrieverWithPropertySettings() {
		properties.put("email.enabled", "true");
		properties.put("email.host", "host");
		properties.put("email.account", "account");
		properties.put("email.password", "password");
		properties.put("email.protocol", "protocol");
		properties.put("email.folder", "folder");
		try (EmailContainer container = new EmailContainer(baseProperties(), factory, processor)) {
			verify(factory).retrieverBuilder("host", "account", "password");
			verify(retrieverBuilder).protocol("protocol");
			verify(retrieverBuilder).folder("folder");
		}
	}

	@Test
	public void shouldCreateServiceWithPropertySettings() {
		properties.put("email.enabled", "true");
		properties.put("email.poll.ms", "111");
		properties.put("email.sent.date.buffer.ms", "2222");
		properties.put("email.shutdown.timeout.ms", "33333");
		properties.put("email.max.look.back.ms", "444444");
		try (EmailContainer container = new EmailContainer(baseProperties(), factory, processor)) {
			verify(factory).serviceBuilder(retriever, processor);
			verify(serviceBuilder).pollMs(111L);
			verify(serviceBuilder).sentDateBufferMs(2222L);
			verify(serviceBuilder).shutdownTimeoutMs(33333L);
			verify(serviceBuilder).maxLookBackMs(444444L);
		}
	}

	@Test
	public void shouldRegisterParsersWithTheService() {
		properties.put("email.enabled", "true");
		try (EmailContainer container =
			new EmailContainer(baseProperties(), factory, processor, parser0, parser1)) {
			verify(serviceBuilder).parsers(parser0);
			verify(serviceBuilder).parsers(parser1);
		}
	}

	@Test
	public void shouldIgnoreNullParsers() {
		properties.put("email.enabled", "true");
		try (EmailContainer container =
			new EmailContainer(baseProperties(), factory, processor, parser0, null)) {
			verify(serviceBuilder).parsers(parser0);
			verify(serviceBuilder, never()).parsers((EmailEventParser)null);
		}
	}

	@Test
	public void shouldNotCreateServiceIfPropertyIsMissingOrDisabled() {
		try (EmailContainer container = new EmailContainer(baseProperties(), factory, processor)) {
			assertNull(container.service);
		}
		properties.put("email.enabled", "false");
		try (EmailContainer container = new EmailContainer(baseProperties(), factory, processor)) {
			assertNull(container.service);
		}
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}

}
