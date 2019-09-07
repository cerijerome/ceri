package ceri.ci.email;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Fetches email messages from a server. Uses a server-side limit query based on minimum sent date,
 * and a client side matcher to filter the retrieved messages. For matching messages content is
 * downloaded and the message is converted to an email object.
 */
public interface EmailRetriever {

	/**
	 * Interface for client-side filtering of email messages.
	 */
	interface Matcher {
		/**
		 * Returns true if the message is a match. Not recommended to check message content in this
		 * method as this will end up pulling the content one by one from the email server. Envelope
		 * data such as subject, date, from, etc is already populated. If content is required to
		 * check for a match, return true for this method to allow for further downstream checking.
		 */
		boolean matches(Message message) throws MessagingException;
	}

	/**
	 * Fetches a list of emails sent after a minimum date. Minimum date is a server-side filter, the
	 * matcher filters on client side.
	 */
	List<Email> retrieve(Date minDate, Matcher matcher) throws IOException;

}
