package ceri.ci.email;

import ceri.ci.build.Event;

public interface EmailEventMatcher {

	Event getEvent(Email email);
	
}
