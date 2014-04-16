package ceri.ci.email;

import ceri.ci.build.BuildEvent;

public interface EmailEventMatcher extends EmailRetriever.Matcher {

	/**
	 * Processes an email into an event. Returns null if the email
	 * cannot be parsed as an event.
	 */
	BuildEvent getEvent(Email email);
	
}
