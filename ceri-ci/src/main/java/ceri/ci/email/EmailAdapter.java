package ceri.ci.email;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.BuildEvent;
import ceri.common.collection.ImmutableUtil;
import ceri.common.ee.ExecutorUtil;

public class EmailAdapter implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final EmailRetriever retriever;
	private final BuildEventProcessor processor;
	private final Collection<EmailEventMatcher> matchers;
	private final ScheduledExecutorService executor;
	private final long shutdownTimeoutMs;

	public static class Builder {
		final EmailRetriever retriever;
		final BuildEventProcessor processor;
		final List<EmailEventMatcher> matchers = new ArrayList<>();
		long shutdownTimeoutMs = TimeUnit.SECONDS.toMillis(3);
		long pollMs = TimeUnit.SECONDS.toMillis(30);
		
		Builder(EmailRetriever retriever, BuildEventProcessor processor) {
			this.retriever = retriever;
			this.processor = processor;
		}
		
		public Builder matchers(EmailEventMatcher...matchers) {
			return matchers(Arrays.asList(matchers));
		}
		
		public Builder matchers(Collection<EmailEventMatcher> matchers) {
			this.matchers.addAll(matchers);
			return this;
		}
		
		public Builder pollMs(long pollMs) {
			this.pollMs = pollMs;
			return this;
		}
		
		public Builder shutdownTimeoutMs(long shutdownTimeoutMs) {
			this.shutdownTimeoutMs = shutdownTimeoutMs;
			return this;
		}
		
		public EmailAdapter build() {
			return new EmailAdapter(this);
		}
	}
	
	public static Builder builder(EmailRetriever retriever, BuildEventProcessor processor) {
		return new Builder(retriever, processor);
	}
	
	EmailAdapter(Builder builder) {
		retriever = builder.retriever;
		processor = builder.processor;
		matchers = ImmutableUtil.copyAsList(builder.matchers);
		shutdownTimeoutMs = builder.shutdownTimeoutMs;
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> run(), 0, builder.pollMs, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void close() {
		ExecutorUtil.close(executor, shutdownTimeoutMs);
	}

	private void run() {
		try {
			Collection<Email> emails = retriever.fetch();
			for (Email email : emails) {
				for (EmailEventMatcher matcher : matchers) {
					BuildEvent event = matcher.getEvent(email);
					if (event == null) continue;
					process(event);
				}
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	private void process(BuildEvent event) {
		try {
			processor.process(event);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

}
