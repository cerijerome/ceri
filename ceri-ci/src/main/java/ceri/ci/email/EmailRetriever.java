package ceri.ci.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
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
import ceri.common.util.ToStringHelper;

public class EmailRetriever {
	private static final Logger logger = LogManager.getLogger();
	private static final Matcher DEFAULT_MATCHER = (message) -> true;
	private final String protocol;
	private final String host;
	private final int port;
	private final long timeoutMs;
	private final String name;
	private final String password;
	private final String folder;

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.setProperty("mail.store.protocol", "imaps");
		properties.setProperty("mail.imaps.timeout", "5000");
		properties.setProperty("mail.imaps.connectiontimeout", "5000");
		Session session = Session.getInstance(properties, null);
		Store store = session.getStore();
		store.connect("imap.gmail.com", "ecg.sjc.ci.alert@gmail.com", "ecgsjccialert");
		Folder folder = store.getFolder("inbox");
		folder.open(Folder.READ_ONLY);
		Date date = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(22));
		SearchTerm search = new SentDateTerm(ComparisonTerm.GT, date);
		Message[] messages = folder.search(search);
		logger.debug("{} messages", messages.length);

		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		folder.fetch(messages, fp);
		logger.debug("Fetch complete");

		for (Message message : messages) {
			logger.debug("Sent: {}", message.getSentDate());
		}
	}

	/**
	 * Interface for client-side filtering of email messages.
	 */
	public static interface Matcher {
		/**
		 * Returns true if the message is a match. Not recommended to check message content in this
		 * method as this will end up pulling the content one by one from the email server. Envelope
		 * data such as subject, date, from, etc is already populated.
		 */
		boolean matches(Message message) throws MessagingException;
	}

	public static class Builder {
		String host;
		String name;
		String password;
		String protocol = "imaps";
		int port = -1; // Default port
		String folder = "inbox";
		long timeoutMs = 5000;

		Builder(String host, String name, String password) {
			this.host = host;
			this.name = name;
			this.password = password;
		}

		public Builder protocol(String protocol) {
			this.protocol = protocol;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder timeoutMs(long timeoutMs) {
			this.timeoutMs = timeoutMs;
			return this;
		}

		public Builder folder(String folder) {
			this.folder = folder;
			return this;
		}

		public EmailRetriever build() {
			return new EmailRetriever(this);
		}
	}

	public static Builder builder(String host, String name, String password) {
		return new Builder(host, name, password);
	}

	EmailRetriever(Builder builder) {
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
		return ToStringHelper.createByClass(this, protocol, host, port, name, folder).toString();
	}

	/**
	 * Fetches a list of emails sent after a minimum date. Minimum date is a server-side filter, the
	 * matcher filters on client side.
	 */
	public List<Email> fetch(Date minDate, Matcher matcher) throws IOException {
		if (minDate == null) throw new IllegalArgumentException("Minimum date must be specified");
		if (matcher == null) matcher = DEFAULT_MATCHER;
		try {
			List<Email> emails = fetchEmail(minDate, matcher);
			Collections.sort(emails, EmailComparators.SENT_DATE);
			return emails;
		} catch (MessagingException e) {
			throw new IOException(e);
		}
	}

	private List<Email> fetchEmail(Date minDate, Matcher matcher) throws MessagingException {
		Store store = null;
		try {
			store = openStore();
			Folder folder = openFolder(store);
			logger.debug("Fetching emails sent from {}", minDate);
			Collection<Message> messages = fetchMessages(folder, minDate, matcher);
			logger.debug("{} email(s) to process", messages.size());
			List<Email> emails = createFromMessages(messages);
			return emails;
		} finally {
			if (store != null) store.close();
		}
	}

	private Collection<Message> fetchMessages(Folder folder, Date minDate, Matcher matcher)
		throws MessagingException {
		// Search term not fully implemented on mail servers, but can prevent fetching all email
		SearchTerm searchTerm = new SentDateTerm(ComparisonTerm.GT, minDate);
		Message[] messages = folder.search(searchTerm);
		logger.debug("{} email(s) returned", messages.length);
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

	private Store openStore() throws MessagingException {
		Session session = EmailUtil.createSession(protocol, timeoutMs);
		Store store = session.getStore();
		store.connect(host, port, name, password);
		return store;
	}

	private Folder openFolder(Store store) throws MessagingException {
		Folder f = folder == null ? store.getDefaultFolder() : store.getFolder(folder);
		f.open(Folder.READ_ONLY);
		return f;
	}

}
