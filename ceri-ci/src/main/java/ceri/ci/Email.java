package ceri.ci;

import java.io.Closeable;
import java.util.Properties;
import ceri.ci.build.BuildEventProcessor;
import ceri.ci.email.BoltEmailMatcher;
import ceri.ci.email.EmailAdapter;
import ceri.ci.email.EmailAdapterProperties;
import ceri.ci.email.EmailEventMatcher;
import ceri.ci.email.EmailRetriever;

public class Email implements Closeable {
	public final EmailAdapter adapter;

	public Email(BuildEventProcessor eventProcessor, Properties properties, String prefix) {
		EmailAdapterProperties emailProperties =
			new EmailAdapterProperties(properties, prefix, "email");
		if (!emailProperties.enabled()) {
			adapter = null;
		} else {
			adapter = createAdapter(eventProcessor, emailProperties);
		}
	}

	@Override
	public void close() {
		if (adapter != null) adapter.close();
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
		Long maxLookBackMs = properties.maxLookBackMs();
		if (maxLookBackMs != null) builder.maxLookBackMs(maxLookBackMs);
		return builder.build();
	}

}
