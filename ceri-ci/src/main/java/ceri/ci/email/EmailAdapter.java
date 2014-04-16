package ceri.ci.email;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.BuildEventProcessor;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.BasicUtil;

public class EmailAdapter implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final EmailRetriever retriever;
	private final BuildEventProcessor processor;
	private final Collection<EmailEventMatcher> matchers;
	private final ScheduledExecutorService executor;
	private final long shutdownTimeoutMs;
	private final long maxLookBackMs;
	private final long sentDateBufferMs;
	private final Collection<Email> emailBuffer = new TreeSet<>(EmailComparators.SENT_DATE);
	private final EmailRetriever.Matcher messageMatcher;
	private Email lastEmail = null;

	public static void main(String[] args) throws Exception {
		EmailRetriever retriever =
			EmailRetriever.builder("imap.gmail.com", "ecg.sjc.ci.alert@gmail.com", "ecgsjccialert")
				.build();
		BoltEmailMatcher matcher = new BoltEmailMatcher();
		EmailAdapter adapter = builder(retriever, (events) -> {
			for (BuildEvent event : events) {
				logger.debug("{}", event);
			}
		}).matchers(matcher).build();
		BasicUtil.delay(300000);
		adapter.close();
	}

	public static class Builder {
		final EmailRetriever retriever;
		final BuildEventProcessor processor;
		final List<EmailEventMatcher> matchers = new ArrayList<>();
		long shutdownTimeoutMs = TimeUnit.SECONDS.toMillis(3);
		long pollMs = TimeUnit.SECONDS.toMillis(30);
		long maxLookBackMs = TimeUnit.HOURS.toMillis(20);
		long sentDateBufferMs = TimeUnit.MINUTES.toMillis(10);
		
		Builder(EmailRetriever retriever, BuildEventProcessor processor) {
			this.retriever = retriever;
			this.processor = processor;
		}

		public Builder matchers(EmailEventMatcher... matchers) {
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

		public Builder maxLookBackMs(long maxLookBackMs) {
			this.maxLookBackMs = maxLookBackMs;
			return this;
		}

		public Builder shutdownTimeoutMs(long shutdownTimeoutMs) {
			this.shutdownTimeoutMs = shutdownTimeoutMs;
			return this;
		}

		public Builder sentDateBufferMs(long sentDateBufferMs) {
			this.sentDateBufferMs = sentDateBufferMs;
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
		maxLookBackMs = builder.maxLookBackMs;
		shutdownTimeoutMs = builder.shutdownTimeoutMs;
		sentDateBufferMs = builder.sentDateBufferMs;
		messageMatcher = (message) -> matches(message);
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> run(), 0, builder.pollMs, TimeUnit.MILLISECONDS);
	}

	@Override
	public void close() {
		try {
			logger.info("Shutting down");
			executor.shutdownNow();
			logger.debug("Awaiting termination");
			boolean complete = executor.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
			if (!complete) logger.warn("Did not shut down in {}ms", shutdownTimeoutMs);
			else logger.debug("Shut down successfully");
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Use this filter to drop any messages we are not interested in.
	 */
	private boolean matches(Message message) throws MessagingException {
		for (EmailEventMatcher matcher : matchers)
			if (matcher.matches(message)) return true;
		return false;
	}
	
	private void run() {
		try {
			Date minDate = minDate(lastEmail);
			List<Email> emails = retriever.fetch(minDate, messageMatcher);
			filterAndUpdateBuffer(emails);
			if (emails.isEmpty()) return;
			lastEmail = emails.get(emails.size() - 1);
			logger.debug("Last email: {}", lastEmail.subject);
			Collection<BuildEvent> events = createEvents(emails);
			logger.debug("Processing {} event(s)", events.size());
			if (events.isEmpty()) return;
			processor.process(events);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Filter emails already in the buffer, then update the buffer.
	 */
	private void filterAndUpdateBuffer(List<Email> emails) {
		if (emails.isEmpty()) return;
		Collection<Email> copy = new HashSet<>(emails);
		Iterator<Email> i = emails.iterator();
		int n = 0;
		while (i.hasNext()) {
			Email email = i.next();
			if (emailBuffer.contains(email)) {
				i.remove();
				n++;
			}
		}
		logger.debug("Ignoring {} email(s) already in buffer", n);
		emailBuffer.clear();
		emailBuffer.addAll(copy);
	}
	
	private Date minDate(Email lastEmail) {
		long t = System.currentTimeMillis() - maxLookBackMs;
		if (lastEmail != null && lastEmail.sentDateMs > t) t = lastEmail.sentDateMs;
		// Use a buffer for late arriving emails
		t -= sentDateBufferMs;
		return new Date(t);
	}

	private Collection<BuildEvent> createEvents(Collection<Email> emails) {
		List<BuildEvent> events = new ArrayList<>();
		for (Email email : emails) {
			for (EmailEventMatcher matcher : matchers) {
				BuildEvent event = matcher.getEvent(email);
				if (event != null) events.add(event);
			}
		}
		return events;
	}

}
