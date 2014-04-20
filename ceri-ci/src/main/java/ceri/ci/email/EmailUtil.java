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
import ceri.common.property.Key;

/**
 * Utility methods for processing emails.
 */
public class EmailUtil {
	private static final String SECURE_SUFFIX = "s";
	private static final String MAIL = "mail";
	private static final String TIMEOUT = "timeout";
	private static final String CONNECTION_TIMEOUT = "connectiontimeout";
	private static final String CONNECTION_POOL_TIMEOUT = "connectionpooltimeout";
	private static final String MAIL_STORE_PROTOCOL_KEY = "mail.store.protocol";

	private EmailUtil() {}

	/**
	 * Creates a session with the email provider with given protocol and timeout settings. Sets the
	 * timeout settings for secure and non-secure versions of the protocol, in case IMAP server
	 * reads the wrong ones.
	 */
	public static Session createSession(String protocol, long timeoutMs) {
		Properties properties = new Properties();
		properties.setProperty(MAIL_STORE_PROTOCOL_KEY, protocol);
		setSessionProperties(properties, protocol, timeoutMs);
		if (!protocol.endsWith(SECURE_SUFFIX)) setSessionProperties(properties, protocol +
			SECURE_SUFFIX, timeoutMs);
		else setSessionProperties(properties, protocol.substring(0, protocol.length() - 1),
			timeoutMs);
		return Session.getInstance(properties);
	}

	/**
	 * Sets protocol timeout properties. 
	 */
	private static void
		setSessionProperties(Properties properties, String protocol, long timeoutMs) {
		String timeoutKey = Key.create(MAIL, protocol, TIMEOUT).value;
		String connectionTimeoutKey = Key.create(MAIL, protocol, CONNECTION_TIMEOUT).value;
		String connectionPoolTimeoutKey = Key.create(MAIL, protocol, CONNECTION_POOL_TIMEOUT).value;
		properties.setProperty(timeoutKey, String.valueOf(timeoutMs));
		properties.setProperty(connectionTimeoutKey, String.valueOf(timeoutMs));
		properties.setProperty(connectionPoolTimeoutKey, String.valueOf(timeoutMs));
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
	 * Get the raw first email address.
	 */
	public static String firstAddress(Address[] from) {
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
