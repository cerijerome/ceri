package ceri.ci.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.text.ToString;

/**
 * Fetches email messages from a server. Uses a server-side limit query based on minimum sent date,
 * and a client side matcher to filter the retrieved messages. For matching messages content is
 * downloaded and the message is converted to an email object.
 */
public class EmailRetrieverImpl implements EmailRetriever {
	private static final Logger logger = LogManager.getLogger();
	private static final Matcher DEFAULT_MATCHER = (message) -> true;
	private final String protocol;
	private final String host;
	private final int port;
	private final long timeoutMs;
	private final String name;
	private final String password;
	private final String folder;

	/**
	 * Builder class to set optional parameters.
	 */
	public static class Builder {
		final String host;
		final String name;
		final String password;
		String protocol = "imaps";
		int port = -1; // Default port
		String folder = "inbox";
		long timeoutMs = 5000;

		Builder(String host, String name, String password) {
			this.host = host;
			this.name = name;
			this.password = password;
		}

		/**
		 * Set the protocol used to fetch emails. Defaults to imaps.
		 */
		public Builder protocol(String protocol) {
			this.protocol = protocol;
			return this;
		}

		/**
		 * Set the server-side port. Default protocol port is otherwise used.
		 */
		public Builder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Sets the timeout in milliseconds for socket, connection, and connection-pool. Default is
		 * 5 seconds.
		 */
		public Builder timeoutMs(long timeoutMs) {
			this.timeoutMs = timeoutMs;
			return this;
		}

		/**
		 * Sets the name of the email folder. Defaults to inbox.
		 */
		public Builder folder(String folder) {
			this.folder = folder;
			return this;
		}

		/**
		 * Builds the retriever object.
		 */
		public EmailRetriever build() {
			return new EmailRetrieverImpl(this);
		}
	}

	/**
	 * Creates the builder with mandatory parameters.
	 */
	public static Builder builder(String host, String name, String password) {
		return new Builder(host, name, password);
	}

	EmailRetrieverImpl(Builder builder) {
		protocol = builder.protocol;
		host = builder.host;
		port = builder.port;
		timeoutMs = builder.timeoutMs;
		name = builder.name;
		password = builder.password;
		folder = builder.folder;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, protocol, host, port, name, folder);
	}

	/**
	 * Fetches a list of emails sent after a minimum date. Minimum date is a server-side filter, the
	 * matcher filters on client side.
	 */
	@Override
	public List<Email> retrieve(Date minDate, Matcher matcher) throws IOException {
		if (minDate == null) throw new IllegalArgumentException("Minimum date must be specified");
		if (matcher == null) matcher = DEFAULT_MATCHER;
		try {
			List<Email> emails = fetchEmail(minDate, matcher);
			emails.sort(EmailComparators.SENT_DATE);
			return emails;
		} catch (MessagingException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Fetch messages from the server, filter them client-side, and convert them to email objects.
	 */
	private List<Email> fetchEmail(Date minDate, Matcher matcher) throws MessagingException {
		Store store = null;
		try {
			logger.debug("Opening store");
			store = openStore();
			return fetchFromStore(minDate, matcher, store);
		} finally {
			if (store != null) store.close();
		}
	}

	/**
	 * Fetches messages from the server, filters them, and converts to email objects.
	 */
	private List<Email> fetchFromStore(Date minDate, Matcher matcher, Store store)
		throws MessagingException {
		Folder folder = null;
		try {
			logger.debug("Opening folder");
			folder = openFolder(store);
			logger.info("Fetching emails sent from {}", minDate);
			Collection<Message> messages = fetchMessages(folder, minDate, matcher);
			logger.debug("{} email(s) to process", messages.size());
			List<Email> emails = createFromMessages(messages);
			logger.debug("{} email(s) converted", messages.size());
			return emails;
		} finally {
			if (folder != null) folder.close(false);
		}
	}

	/**
	 * Fetches messages from the open email server folder.
	 */
	private Collection<Message> fetchMessages(Folder folder, Date minDate, Matcher matcher)
		throws MessagingException {
		// Search term not fully implemented on mail servers, but can prevent fetching all email
		SearchTerm searchTerm = new SentDateTerm(ComparisonTerm.GT, minDate);
		Message[] messages = folder.search(searchTerm);
		logger.info("{} email(s) returned", messages.length);
		bulkFetchMessageEnvelopes(folder, messages);
		List<Message> filteredMessages = new ArrayList<>();
		for (Message message : messages) {
			Date sentDate = message.getSentDate();
			if (minDate.after(sentDate)) continue;
			if (!matcher.matches(message)) continue;
			filteredMessages.add(message);
		}
		return filteredMessages;
	}

	/**
	 * Optimize fetching of header info by downloading envelopes for all messages.
	 */
	private void bulkFetchMessageEnvelopes(Folder folder, Message[] messages)
		throws MessagingException {
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		folder.fetch(messages, fp);
	}

	/**
	 * Creates email objects from messages. Headers are already loaded, but content will be
	 * downloaded one by one.
	 */
	private List<Email> createFromMessages(Collection<Message> messages) {
		List<Email> emails = new ArrayList<>();
		for (Message message : messages) {
			ConcurrentUtil.checkRuntimeInterrupted();
			try {
				Email email = Email.createFrom(message);
				emails.add(email);
			} catch (MessagingException | IOException e) {
				logger.catching(Level.WARN, e);
			}
		}
		return emails;
	}

	/**
	 * Creates a session with protocol and timeout properties, connects to a store, and returns the
	 * store object.
	 */
	private Store openStore() throws MessagingException {
		Store store = getStore();
		store.connect(host, port, name, password);
		return store;
	}

	/**
	 * Creates a session with protocol and timeout properties.
	 */
	protected Store getStore() throws MessagingException {
		Session session = EmailUtil.createSession(protocol, timeoutMs);
		return session.getStore();
	}

	/**
	 * Opens the folder of the given store in read only mode.
	 */
	private Folder openFolder(Store store) throws MessagingException {
		Folder f = folder == null ? store.getDefaultFolder() : store.getFolder(folder);
		f.open(Folder.READ_ONLY);
		return f;
	}

}
