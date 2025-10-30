package ceri.common.net;

import java.util.regex.Pattern;

/**
 * Email utilities.
 */
public class Emails {
	/** RFC822: http://www.faqs.org/rfcs/rfc822.html */
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^(([A-Za-z0-9-]+_+)|"
		+ "([A-Za-z0-9-]+\\-+)|" + "([A-Za-z0-9-]+\\.+)|" + "([A-Za-z0-9-]+\\++))*"
		+ "[A-Za-z0-9-_]+@" + "((\\w+\\-+)|(\\w+\\.))*" + "\\w{1,63}\\.[a-zA-Z]{2,6}$");

	private Emails() {}

	/**
	 * Perform all validations
	 */
	public static boolean isValid(String emailAddress) {
		return EMAIL_PATTERN.matcher(emailAddress).matches();
	}
}
