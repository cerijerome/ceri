package ceri.ci.email;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import ceri.ci.build.BuildEventProcessor;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;

/**
 * Creates the service to fetch emails and parse them into build events.
 */
public class EmailContainer implements Closeable {
	public static final String GROUP = "email";
	public final EmailService service;

	public EmailContainer(BaseProperties properties, BuildEventProcessor processor,
		Collection<EmailEventParser> parsers) {
		this(properties, new EmailFactoryImpl(), processor, parsers);
	}

	public EmailContainer(BaseProperties properties, EmailFactory factory,
		BuildEventProcessor processor, EmailEventParser... parsers) {
		this(properties, factory, processor, Arrays.asList(parsers));
	}

	public EmailContainer(BaseProperties properties, EmailFactory factory,
		BuildEventProcessor processor, Collection<EmailEventParser> parsers) {
		EmailProperties emailProperties = new EmailProperties(properties, GROUP);
		if (!emailProperties.enabled()) {
			service = null;
		} else {
			service = createService(emailProperties, processor, factory, parsers);
		}
	}

	@Override
	public void close() {
		if (service != null) IoUtil.close(service);
	}

	private EmailService createService(EmailProperties properties, BuildEventProcessor processor,
		EmailFactory factory, Collection<EmailEventParser> parsers) {
		EmailRetriever retriever = createRetriever(properties, factory);
		EmailService.Builder builder = factory.serviceBuilder(retriever, processor);
		for (EmailEventParser parser : parsers)
			if (parser != null) builder.parsers(parser);
		setProperties(builder, properties);
		return builder.build();
	}

	private void setProperties(EmailService.Builder builder, EmailProperties properties) {
		Long pollMs = properties.pollMs();
		if (pollMs != null) builder.pollMs(pollMs);
		Long shutdownTimeoutMs = properties.shutdownTimeoutMs();
		if (shutdownTimeoutMs != null) builder.shutdownTimeoutMs(shutdownTimeoutMs);
		Long maxLookBackMs = properties.maxLookBackMs();
		if (maxLookBackMs != null) builder.maxLookBackMs(maxLookBackMs);
		Long sentDateBufferMs = properties.sentDateBufferMs();
		if (sentDateBufferMs != null) builder.sentDateBufferMs(sentDateBufferMs);
	}

	private EmailRetriever createRetriever(EmailProperties properties, EmailFactory factory) {
		EmailRetrieverImpl.Builder builder = factory.retrieverBuilder(properties.host(),
			properties.account(), properties.password());
		String protocol = properties.protocol();
		if (protocol != null) builder.protocol(protocol);
		String folder = properties.folder();
		if (folder != null) builder.folder(folder);
		return builder.build();
	}

}
