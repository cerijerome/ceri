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

/**
 * Polls the email server for emails. Uses the last received email's sent date for the minimum sent
 * date on the next server query. A maximum look back time is used to limit the server query for the
 * first time the code runs, and when the last email is too old. Matchers should be registered to
 * optimize email filtering, and to convert emails to build events. A buffer is used to store emails
 * from the previous run to check for late-arrivals.
 */
public class EmailService implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final EmailRetriever retriever;
	private final BuildEventProcessor processor;
	private final Collection<EmailEventParser> parsers;
	private final ScheduledExecutorService executor;
	private final long shutdownTimeoutMs;
	private final long maxLookBackMs;
	private final long sentDateBufferMs;
	final EmailRetriever.Matcher messageMatcher;
	private final Collection<Email> emailBuffer = new TreeSet<>(EmailComparators.SENT_DATE);
	private Email lastEmail = null;

	/**
	 * Builds the email service with optional parameters.
	 */
	public static class Builder {
		final EmailRetriever retriever;
		final BuildEventProcessor processor;
		final List<EmailEventParser> parsers = new ArrayList<>();
		long shutdownTimeoutMs = TimeUnit.SECONDS.toMillis(3);
		long pollMs = TimeUnit.SECONDS.toMillis(30);
		long maxLookBackMs = TimeUnit.HOURS.toMillis(20);
		long sentDateBufferMs = TimeUnit.MINUTES.toMillis(10);

		Builder(EmailRetriever retriever, BuildEventProcessor processor) {
			this.retriever = retriever;
			this.processor = processor;
		}

		/**
		 * Registers matchers to optimize email filtering, and convert emails to build events.
		 */
		public Builder parsers(EmailEventParser... matchers) {
			return parsers(Arrays.asList(matchers));
		}

		/**
		 * Registers matchers to optimize email filtering, and convert emails to build events.
		 */
		public Builder parsers(Collection<EmailEventParser> parsers) {
			this.parsers.addAll(parsers);
			return this;
		}

		/**
		 * Sets the polling interval for fetching emails from the server.
		 */
		public Builder pollMs(long pollMs) {
			this.pollMs = pollMs;
			return this;
		}

		/**
		 * Set the maximum look back time from the current time for the server query.
		 */
		public Builder maxLookBackMs(long maxLookBackMs) {
			this.maxLookBackMs = maxLookBackMs;
			return this;
		}

		/**
		 * Sets the timeout in milliseconds to wait for shutdown on close.
		 */
		public Builder shutdownTimeoutMs(long shutdownTimeoutMs) {
			this.shutdownTimeoutMs = shutdownTimeoutMs;
			return this;
		}

		/**
		 * Sets the additional look back buffer in milliseconds for emails arriving late since the
		 * last fetch.
		 */
		public Builder sentDateBufferMs(long sentDateBufferMs) {
			this.sentDateBufferMs = sentDateBufferMs;
			return this;
		}

		/**
		 * Builds the email service.
		 */
		public EmailService build() {
			return new EmailService(this);
		}
	}

	/**
	 * Creates the builder with mandatory parameters.
	 */
	public static Builder builder(EmailRetriever retriever, BuildEventProcessor processor) {
		return new Builder(retriever, processor);
	}

	EmailService(Builder builder) {
		retriever = builder.retriever;
		processor = builder.processor;
		parsers = ImmutableUtil.copyAsList(builder.parsers);
		maxLookBackMs = builder.maxLookBackMs;
		shutdownTimeoutMs = builder.shutdownTimeoutMs;
		sentDateBufferMs = builder.sentDateBufferMs;
		messageMatcher = this::matches;
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(this::run, 0, builder.pollMs, TimeUnit.MILLISECONDS);
	}

	/**
	 * Shuts down the execution thread and waits for it to finish. Throws
	 * RuntimeInterruptedException if interrupted during shutdown.
	 */
	@Override
	public void close() {
		try {
			logger.info("Shutting down");
			executor.shutdownNow();
			logger.debug("Awaiting termination");
			boolean complete = executor.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
			if (!complete) logger.warn("Did not shut down in {}ms", shutdownTimeoutMs);
			else logger.info("Shut down successfully");
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Uses the filter to drop any messages we are not interested in. This is called by the email
	 * retriever.
	 */
	private boolean matches(Message message) throws MessagingException {
		for (EmailEventParser parser : parsers)
			if (parser.matches(message)) return true;
		return false;
	}

	/**
	 * Execution thread method. A single thread executes this method continuously after a fixed
	 * delay.
	 */
	private void run() {
		try {
			logger.debug("Thread started");
			Date minDate = minDate(lastEmail);
			List<Email> emails = retriever.retrieve(minDate, messageMatcher);
			filterAndUpdateBuffer(emails);
			if (emails.isEmpty()) return;
			lastEmail = emails.get(emails.size() - 1);
			logger.debug("Last email: {}", lastEmail.subject);
			Collection<BuildEvent> events = createEvents(emails);
			logger.info("Processing {} event(s)", events.size());
			if (events.isEmpty()) return;
			for (BuildEvent event : events)
				logger.debug("{}", event);
			if (processor != null) processor.process(events);
		} catch (IOException e) {
			logger.catching(e);
		} finally {
			logger.debug("Thread complete");
		}
	}

	/**
	 * Filters emails already in the buffer, then updates the buffer.
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

	/**
	 * Determines the minimum sent date for the query. Uses the last email date or maximum look back
	 * time, depending which is later. Adds an additional look back buffer in case emails turned up
	 * late since the last request.
	 */
	private Date minDate(Email lastEmail) {
		long t = System.currentTimeMillis() - maxLookBackMs;
		if (lastEmail != null && lastEmail.sentDateMs > t) t = lastEmail.sentDateMs;
		// Use a buffer for late arriving emails
		t -= sentDateBufferMs;
		return new Date(t);
	}

	/**
	 * Converts emails to build events, using registered matcher objects.
	 */
	private Collection<BuildEvent> createEvents(Collection<Email> emails) {
		List<BuildEvent> events = new ArrayList<>();
		for (Email email : emails) {
			for (EmailEventParser parser : parsers) {
				BuildEvent event = parser.parse(email);
				if (event != null) events.add(event);
			}
		}
		return events;
	}

}
