package ceri.ci.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import ceri.common.io.IoUtil;

/**
 * Utility methods for processing emails.
 */
public class EmailUtil {
	public static final String MAIL_STORE_PROTOCOL_KEY = "mail.store.protocol";

	private EmailUtil() {}

	/**
	 * Creates a session with the email provider
	 */
	public static Session createSession(String protocol) {
		Properties properties = new Properties();
		properties.setProperty(MAIL_STORE_PROTOCOL_KEY, protocol);
		return Session.getInstance(properties, null);
	}

	/**
	 * Get string content from a message.
	 */
	public static String content(Message message) throws MessagingException, IOException {
		Object content = message.getContent();
		if (content instanceof Multipart) {
			Multipart mp = (Multipart) message.getContent();
			BodyPart bp = mp.getBodyPart(0);
			content = bp.getContent();
		} else if (content instanceof InputStream) {
			content = IoUtil.getContent((InputStream) content, 0);
		}
		return String.valueOf(content);
	}

	/**
	 * Get the raw from email address.
	 */
	public static String from(Address[] from) {
		Collection<String> fr = addressesFrom(from);
		if (fr.isEmpty()) return null;
		return fr.iterator().next();
	}

	/**
	 * Get the raw email addresses from an array of general addresses.
	 */
	public static Collection<String> addressesFrom(Address[] addresses) {
		Set<String> addrs = new TreeSet<>();
		for (Address address : addresses) {
			String addr = addressFrom(address);
			if (addr == null) continue;
			addrs.add(addr);
		}
		return addrs;
	}

	/**
	 * Get the raw email addresses from a general address.
	 */
	public static String addressFrom(Address address) {
		if (!(address instanceof InternetAddress)) return null;
		return ((InternetAddress) address).getAddress();
	}

}