package ceri.ci.email;

import java.io.Closeable;
import ceri.ci.build.BuildEventProcessor;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;

public class EmailContainer implements Closeable {
	private static final String GROUP = "email";
	public final EmailAdapter adapter;

	public EmailContainer(BuildEventProcessor eventProcessor, BaseProperties properties) {
		EmailAdapterProperties emailProperties = new EmailAdapterProperties(properties, GROUP);
		if (!emailProperties.enabled()) {
			adapter = null;
		} else {
			adapter = createAdapter(eventProcessor, emailProperties);
		}
	}

	@Override
	public void close() {
		if (adapter != null) IoUtil.close(adapter);
	}

	private EmailAdapter createAdapter(BuildEventProcessor eventProcessor,
		EmailAdapterProperties properties) {
		EmailRetriever retriever = createRetriever(properties);
		EmailEventMatcher matcher = createEventMatcher();
		EmailAdapter.Builder builder =
			EmailAdapter.builder(retriever, eventProcessor).matchers(matcher);
		Long pollMs = properties.pollMs();
		if (pollMs != null) builder.pollMs(pollMs);
		Long shutdownTimeoutMs = properties.shutdownTimeoutMs();
		if (shutdownTimeoutMs != null) builder.shutdownTimeoutMs(shutdownTimeoutMs);
		Long maxLookBackMs = properties.maxLookBackMs();
		if (maxLookBackMs != null) builder.maxLookBackMs(maxLookBackMs);
		Long sentDateBufferMs = properties.sentDateBufferMs();
		if (sentDateBufferMs != null) builder.sentDateBufferMs(sentDateBufferMs);
		return builder.build();
	}

	private EmailEventMatcher createEventMatcher() {
		return new BoltEmailMatcher();
	}

	private EmailRetriever createRetriever(EmailAdapterProperties properties) {
		EmailRetriever.Builder builder =
			EmailRetriever.builder(properties.host(), properties.account(), properties.password());
		String protocol = properties.protocol();
		if (protocol != null) builder.protocol(protocol);
		String folder = properties.folder();
		if (folder != null) builder.folder(folder);
		return builder.build();
	}

}
