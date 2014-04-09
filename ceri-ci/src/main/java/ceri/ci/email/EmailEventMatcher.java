package ceri.ci.email;

import ceri.ci.build.BuildEvent;

public interface EmailEventMatcher {

	BuildEvent getEvent(Email email);
	
}
