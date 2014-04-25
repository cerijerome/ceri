package ceri.ci.email;

import ceri.ci.build.BuildEvent;

/**
 * Interface for parsing email content and creating a build event from it. Also extends the
 * EmailRetriver matcher interface to optimize filtering of messages from the server.
 */
public interface EmailEventParser extends EmailRetriever.Matcher {

	/**
	 * Processes an email into an event. Returns null if the email cannot be parsed as an event.
	 */
	BuildEvent parse(Email email);

}
