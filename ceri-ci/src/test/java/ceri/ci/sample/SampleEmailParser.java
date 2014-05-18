package ceri.ci.sample;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.ci.build.Event.Type;
import ceri.ci.email.Email;
import ceri.ci.email.EmailEventParser;

/**
 * A sample email parser that accepts emails with subject of the form:
 * <pre>Sample: build-name job-name [broken|fixed]</pre>
 * and content with a comma-separated list of committers.
 */
public class SampleEmailParser implements EmailEventParser {
	private static final Pattern SUBJECT_REGEX = Pattern.compile("Sample: (\\w+) (\\w+) (\\w+)");
	private static final Pattern NAME_SPLIT_REGEX = Pattern.compile("\\s*,\\s*");
	private static final String BROKEN = "broken";
	private static final String FIXED = "fixed";

	@Override
	public boolean matches(Message message) throws MessagingException {
		String subject = message.getSubject();
		return subject != null && SUBJECT_REGEX.matcher(subject).matches();
	}

	@Override
	public BuildEvent parse(Email email) {
		if (email.subject == null) return null;
		Matcher m = SUBJECT_REGEX.matcher(email.subject);
		if (!m.find()) return null;
		String build = m.group(1);
		String job = m.group(2);
		String state = m.group(3);
		Type type = null;
		if (state.equalsIgnoreCase(BROKEN)) type = Type.failure;
		else if (state.equalsIgnoreCase(FIXED)) type = Type.success;
		else return null;
		String[] names =
			email.content == null ? new String[0] : NAME_SPLIT_REGEX.split(email.content.trim());
		return new BuildEvent(build, job, new Event(type, email.sentDateMs, names));
	}

}
