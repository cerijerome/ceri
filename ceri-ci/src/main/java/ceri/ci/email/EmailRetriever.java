package ceri.ci.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SentDateTerm;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.util.BasicUtil;
import ceri.common.util.ToStringHelper;

public class EmailRetriever {
	private static final Logger logger = LogManager.getLogger();
	private final String protocol;
	private final String host;
	private final int port;
	private final String name;
	private final String password;
	private final String folder;
	private final long maxLookBackMs;
	private Email lastEmail = null;

	public static void main(String[] args) throws IOException {
		EmailRetriever ret =
			builder("imap.gmail.com", "ecg.sjc.ci.alert@gmail.com", "ecgsjccialert").maxLookBackMs(
				TimeUnit.DAYS.toMillis(5)).build();
		while (true) {
			Collection<Email> emails = ret.fetch();
			for (Email email : emails) System.out.println(email);
			System.out.println("----------------------------------------------");
			BasicUtil.delay(20000);
		}
	}

	public static class Builder {
		String host;
		String name;
		String password;
		String protocol = "imaps";
		int port = -1; // Default port
		String folder = "inbox";
		long maxLookBackMs = TimeUnit.HOURS.toMillis(24); // Look back max 24 hours

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

		public Builder folder(String folder) {
			this.folder = folder;
			return this;
		}

		public Builder maxLookBackMs(long maxLookBackMs) {
			this.maxLookBackMs = maxLookBackMs;
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
		name = builder.name;
		password = builder.password;
		folder = builder.folder;
		maxLookBackMs = builder.maxLookBackMs;
	}

	@Override
	public String toString() {
		return ToStringHelper
			.createByClass(this, protocol, host, port, name, folder, maxLookBackMs).toString();
	}

	public Collection<Email> fetch() throws IOException {
		try {
			List<Email> emails = fetchEmail();
			Collections.sort(emails, EmailComparators.SENT_DATE);
			if (!emails.isEmpty()) lastEmail = emails.get(emails.size() - 1);
			return emails;
		} catch (MessagingException e) {
			throw new IOException(e);
		}
	}

	private List<Email> fetchEmail() throws MessagingException {
		Store store = null;
		try {
			store = openStore();
			Folder folder = openFolder(store);
			Date minSentDate = minSentDate();
			logger.debug("Fetching emails sent after {}", minSentDate);
			Message[] messages = search(folder, minSentDate);
			return createFromMessages(minSentDate, messages);
		} finally {
			if (store != null) store.close();
		}
	}

	private List<Email> createFromMessages(Date minSentDate, Message...messages) {
		List<Email> emails = new ArrayList<>();
		long minSentMs = minSentDate.getTime();
		for (Message message : messages) {
			try {
				Email email = Email.createFrom(message);
				if (email.sentDateMs <= minSentMs) continue;
				emails.add(email);
			} catch (MessagingException|IOException e) {
				logger.catching(Level.WARN, e);
			}
		}
		return emails;
	}
	
	private Store openStore() throws MessagingException {
		Session session = EmailUtil.createSession(protocol);
		Store store = session.getStore();
		store.connect(host, port, name, password);
		return store;
	}

	private Folder openFolder(Store store) throws MessagingException {
		Folder f = folder == null ? store.getDefaultFolder() : store.getFolder(folder);
		f.open(Folder.READ_ONLY);
		return f;
	}

	private Date minSentDate() {
		long t = System.currentTimeMillis() - maxLookBackMs;
		if (lastEmail != null && lastEmail.sentDateMs > t) t = lastEmail.sentDateMs;
		return new Date(t);
	}

	private Message[] search(Folder folder, Date minSentDate) throws MessagingException {
		return folder.search(new SentDateTerm(ComparisonTerm.GT, minSentDate));
	}

}
